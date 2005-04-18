/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.driver.net.eepro100;

import org.apache.log4j.Logger;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetHeader;
import org.jnode.system.ResourceManager;
import org.jnode.util.NumberUtils;

/**
 * @author flesire
 */
public class EEPRO100Buffer implements EEPRO100Constants {

	// --- Constants
	private final static int PKT_BUF_SZ = 1536;

	// static final public int RX_NR_FRAME = 32;
	private final static int DATA_BUFFER_SIZE = 1536;

	private final static int PacketReceived = 0xc000;

	// --- Rx Variables
	private int rxMode;

	private int curRx;

	private int dirtyRx;

	public EEPRO100RxFD[] rxRing = new EEPRO100RxFD[RX_RING_SIZE];

	private EEPRO100RxFD[] rxPackets = new EEPRO100RxFD[128];

	private int rx_packets;

	private int rxErrors;

	private EEPRO100RxFD last_rxf;

	private int rxPacketIndex;

	private int lastRxTime;

	// --- Tx Variables
	private int txThreshold = 0x01200000;

	private int curTx;

	private int dirtyTx;

	public EEPRO100TxFD[] txRing = new EEPRO100TxFD[TX_RING_SIZE];

	private EEPRO100TxFD lastCmd;

	private int lastCmdTime;

	// --- Others variables
	protected final Logger log = Logger.getLogger(getClass());

	EEPRO100Core core;

	ResourceManager rm;

	int jiffies;

	/**
	 *  
	 */
	public EEPRO100Buffer(EEPRO100Core core) {
		/* Set up the Tx queue early.. */
		curTx = 0;
		dirtyTx = 0;
		this.core = core;
		this.rm = this.core.getRm();
	}

	public final void initSingleRxRing() {
		EEPRO100Registers regs = this.core.getRegs();
		log.debug("Set TX base addr.");
		rxPackets[0] = new EEPRO100RxFD(rm);
		rxPackets[0].setStatus(0x0001);
		rxPackets[0].setCommand(0x0000);
		rxPackets[0].setLink(rxPackets[0].getBufferAddress());
		// TODO Set correct value
		rxPackets[0].setRxBufferAddress(0);
		// ---------------------------------
		rxPackets[0].setCount(0);
		rxPackets[0].setSize(1528);
		// Start the receiver.
		regs.setReg32(SCBPointer, rxPackets[0].getBufferAddress());
		regs.setReg16(SCBCmd, SCBMaskAll | RxStart);
		EEPRO100Utils.waitForCmdDone(regs);

		log.debug("Started rx process.");

		rxPackets[0].setStatus(0);
		rxPackets[0].setCommand(0xC000);

		regs.setReg32(SCBPointer, rxPackets[0].getBufferAddress());
		regs.setReg16(SCBCmd, SCBMaskAll | RxStart);
		EEPRO100Utils.waitForCmdDone(regs);

	}

	/* Initialize the Rx and Tx rings, along with various 'dev' bits. */
	public final void initRxRing() {
		EEPRO100RxFD rxf = null;
		int i;

		curRx = 0;
		rxPacketIndex = 0;

		for (i = 0; i < rxPackets.length; i++)
			rxPackets[i] = new EEPRO100RxFD(this.rm);

		log.debug("rxPacket 0: "
				+ Integer.toHexString(rxPackets[0].getBufferAddress()));

		for (i = 0; i < RX_RING_SIZE; i++) {
			rxf = rxPackets[rxPacketIndex++];
			rxPacketIndex &= (rxPackets.length - 1);
			rxRing[i] = rxf;
			if (last_rxf != null)
				last_rxf.setLink(rxf.getBufferAddress());
			last_rxf = rxf;
			rxf.setStatus(1); /* '1' is flag value only. */
			rxf.setLink(0); /* None yet. */
			/* This field unused by i82557, we use it as a consistency check. */
			rxf.setRxBufferAddress(0xffffffff);
			rxf.setCount(DATA_BUFFER_SIZE << 16);
			rxf.cleanHeader();
		}
		dirtyRx = i - RX_RING_SIZE;
		/* Mark the last entry as end-of-list. */
		last_rxf.setStatus(0xC0000002); /* '2' is flag value only. */
		// last_rxf.cleanHeader();
		int rxRingSize = rxRing.length;
	}

	public final void initSingleTxRing() {
		log.debug("Set TX base addr.");
		txRing[0] = new EEPRO100TxFD(rm);
		txRing[0].setCommand(CmdIASetup);
		txRing[0].setStatus(0x0000);
	}

	public final void initTxRing() {
		for (int i = 0; i < txRing.length; i++) {
			txRing[i] = new EEPRO100TxFD(rm);
		}
	}

	/**
	 * @return
	 */
	public final int rx(EEPRO100Driver driver) throws NetworkException {
		log.debug("*** Init Rx ***");
		int entry = curRx & RX_RING_SIZE - 1;
		int status;
		int rxWorkLimit = dirtyRx + RX_RING_SIZE - curRx;
		EEPRO100RxFD rxf;

		rxRing[entry].flushHeader();
		int count;
		while (rxRing[entry] != null
				&& ((count = rxRing[entry].getCount()) & PacketReceived) == PacketReceived) {
			int pkt_len = count & 0x07ff;

			if (--rxWorkLimit < 0)
				break;
			status = rxRing[entry].getStatus();
			log.debug(" rx() status " + Integer.toHexString(status) + " len "
					+ pkt_len);

			if ((status & (RxErrTooBig | RxOK | 0x0f90)) != RxOK) {
				if ((status & RxErrTooBig) != 0)
					log.debug(": Ethernet frame overran the Rx buffer, status "
							+ Integer.toHexString(status));
				else if (!((status & RxOK) != 0)) {
					/* There was a fatal error. This *should* be impossible. */
					rxErrors++;
					log.debug("Anomalous event in rx(), status "
							+ Integer.toHexString(status));
				}
			} else {
				/*
				 * if ((drv_flags & HasChksum)!=0) pkt_len -= 2;
				 */
				/* Pass up the already-filled skbuff. */
				byte[] buf = rxRing[entry].getDataBuffer();
				SocketBuffer skbuf = new SocketBuffer(buf, 0, buf.length);
				driver.onReceive(skbuf);
				rxRing[entry] = null;

				rx_packets++;
			}
			entry = (++curRx) & RX_RING_SIZE - 1;
			rxRing[entry].flushHeader();
		}

		for (; curRx - dirtyRx > 0; dirtyRx++) {
			entry = dirtyRx & RX_RING_SIZE - 1;

			rxRing[entry] = rxPackets[rxPacketIndex];
			rxf = rxRing[entry];
			rxPacketIndex++;
			rxPacketIndex &= (rxPackets.length - 1);

			rxf.setStatus(0xc0000001);
			rxf.setCount(PKT_BUF_SZ << 16);
			rxf.setLink(0);

			last_rxf.setLink(rxf.getBufferAddress());
			last_rxf.setStatus(last_rxf.getStatus() & ~0xc0000000);
			last_rxf.cleanHeader();
			last_rxf = rxf;
			rxf.cleanHeader();
		}

		lastRxTime = jiffies;
		log.debug("*** End Rx ***");
		return 0;
	}

	/*
	 * Set or clear the multicast filter for this adaptor. This is very ugly
	 * with Intel chips -- we usually have to execute an entire configuration
	 * command, plus process a multicast command. This is complicated. We must
	 * put a large configuration command and an arbitrarily-sized multicast
	 * command in the transmit list. To minimize the disruption -- the previous
	 * command might have already loaded the link -- we convert the current
	 * command block, normally a Tx command, into a no-op and link it to the new
	 * command.
	 */
	final void setRxMode() {
		EEPRO100TxFD lastCmd0;
		byte newRxMode = 0;
		int flags;
		int entry;
		byte[] configData = new byte[22];

		// if (flags & IFF_PROMISC) { /* Set promiscuous. */
		// new_rx_mode = 3;
		// } else if ((flags & IFF_ALLMULTI) ||
		// >mc_count > multicast_filter_limit) {
		// new_rx_mode = 1;
		// } else
		// new_rx_mode = 0;

		if (curTx - dirtyTx >= TX_RING_SIZE - 1) {
			/*
			 * The Tx ring is full -- don't add anything! Presumably the new
			 * mode is in config_cmd_data and will be added anyway, otherwise we
			 * wait for a timer tick or the mode to change again.
			 */
			rxMode = -1;
			return;
		}

		// if (new_rx_mode != rx_mode) {
		// int mask=CpuControl.maskCPUInterrupts();
		entry = curTx & TX_RING_SIZE - 1;
		lastCmd0 = lastCmd;
		lastCmd = txRing[entry];

		txRing[entry].setStatus(CmdSuspend | CmdConfigure);
		curTx++;
		txRing[entry].setLink(txRing[(entry + 1) & TX_RING_SIZE - 1]
				.getBufferAddress());

		/* Construct a full CmdConfig frame. */
		System.arraycopy(i82558ConfigCmd, 0, configData, 0, configData.length);
		// configData[1] = (byte)((txfifo << 4) | rxfifo);
		// configData[4] = rxdmacount;
		// configData[5] = (byte)(txdmacount + 0x80);
		/*
		 * if ((drv_flags & HasChksum)!=0) configData[9] |= 1;
		 */
		// configData[15] |= (new_rx_mode & 2)!=0 ? 1 : 0;
		// configData[19] = (byte)(flow_ctrl ? 0xBD : 0x80);
		// configData[19] |= full_duplex ? 0x40 : 0;
		// configData[21] = (byte)((new_rx_mode & 1)!=0 ? 0x0D : 0x05);
		/* if ((phy[0] & 0x8000)!=0) { /* Use the AUI port instead. */
		/*
		 * configData[15] |= 0x80; configData[8] = 0; }
		 */
		for (int i = 0; i < configData.length; i++) {
			log.debug(i + ':' + Integer.toHexString(configData[i]) + ' ');
		}
		txRing[entry].setParams(configData);
		/* Trigger the command unit resume. */
		EEPRO100Utils.waitForCmdDone(this.core.getRegs());
		// lastCmd0.clearSuspend();
		this.core.getRegs().setReg8(SCBCmd, CUResume);
		// CpuControl.umaskCPUInterrupts(mask);
		lastCmdTime = jiffies;
		// }

		rxMode = newRxMode;

		// set up multicast
		// mask = CpuControl.maskCPUInterrupts();
		// entry = cur_tx & TX_RING_SIZE-1;
		// lastCmd0 = lastCmd;
		// lastCmd = txRing[entry];
		// cur_tx++;
		// txRing[entry].status(CmdSuspend | CmdMulticastList);
		// txRing[entry].descriptorAddress(0);
		// txRing[entry].link(txRing[entry+1 & TX_RING_SIZE-1].bufferAddress);
		// waitForCmdDone();
		// lastCmd0.clearSuspend();
		// csr.write8(SCBCmd, CUResume);
		// CpuControl.umaskCPUInterrupts(mask);
		lastCmdTime = jiffies;
	}

	/**
	 * Start the chip hardware after a full reset.
	 */
	final void resume() {

		EEPRO100Registers regs = this.core.getRegs();

		log.debug(this.core.getFlags().getName() + " : Init resume");
		regs.setReg16(SCBCmd, SCBMaskAll);

		/* Set the segment registers to '0'. */
		EEPRO100Utils.waitForCmdDone(regs);
		regs.setReg32(SCBPointer, 0);
		// csr.read32(SCBPointer); /* Flush to PCI. */
		this.core.systemDelay(10); /* Bogus, but it avoids the bug. */
		/* Note: these next two operations can take a while. */
		regs.setReg8(SCBCmd, RxAddrLoad);
		EEPRO100Utils.waitForCmdDone(regs);
		regs.setReg8(SCBCmd, CUCmdBase);
		EEPRO100Utils.waitForCmdDone(regs);

		/* Load the statistics block and rx ring addresses. */
		this.core.getStats().loadBlock();
		EEPRO100Utils.waitForCmdDone(regs);

		int rxRingAddress = (rxRing[getCurRx() & RX_RING_SIZE - 1])
				.getBufferAddress();
		regs.setReg32(SCBPointer, rxRingAddress);
		regs.setReg8(SCBCmd, RxStart);
		EEPRO100Utils.waitForCmdDone(regs);
		regs.setReg8(SCBCmd, CUDumpStats);
		this.core.systemDelay(30);

		/* Fill the first command with our physical address. */
		int entry = curTx++ & TX_RING_SIZE - 1;
		EEPRO100TxFD curCmd = txRing[entry];
		/* Avoid a bug(?!) here by marking the command already completed. */
		curCmd.setStatus((CmdSuspend | CmdIASetup) | 0xa000);
		curCmd.setLink(txRing[curTx & TX_RING_SIZE - 1].getBufferAddress());
		// curCmd.setParams(deviceAddress);
		/*
		 * if (lastCmd != null) getLastCmd.clearSuspend();
		 */
		lastCmd = curCmd;

		EEPRO100Utils.waitForCmdDone(regs);

		/* Start the chip's Tx process and unmask interrupts. */
		int txRingAddress = (txRing[getDirtyTx() & TX_RING_SIZE - 1])
				.getBufferAddress();
		regs.setReg32(SCBPointer, txRingAddress);
		regs.setReg16(SCBCmd, CUStart | SCBMaskEarlyRx | SCBMaskFlowCtl);
		log.debug(this.core.getFlags().getName() + " : End resume");
	}

	/**
	 * @param buf
	 */
	public void transmit(SocketBuffer buf) {

		EthernetHeader hdr = (EthernetHeader) buf.getLinkLayerHeader();

		EEPRO100Registers regs = this.core.getRegs();

		int status;
		int s1;
		int s2;

		status = regs.getReg16(SCBStatus);
		regs.setReg16(SCBStatus, status & IntrAllNormal);

		/*log.debug("Transmitting type " + NumberUtils.hex(hdr.getLengthType())
				+ " packet(" + NumberUtils.hex(hdr.getLength())
				+ " bytes). Status=" + NumberUtils.hex(status) + " cmd="
				+ regs.getReg16(SCBCmd));*/

		txRing[0].setStatus(0);
		txRing[0].setCommand(CmdSuspend | CmdTx | CmdTxFlex);
		txRing[0].setLink(txRing[0].getBufferAddress());
		txRing[0].setCount(0x02208000);

		regs.setReg16(SCBPointer, txRing[0].getBufferAddress());
		regs.setReg16(SCBCmd, SCBMaskAll | CUStart);
		EEPRO100Utils.waitForCmdDone(regs);

		s1 = regs.getReg16(SCBStatus);
		// TODO wait 10 ms for transmiting;
		s2 = regs.getReg16(SCBStatus);

		log.debug("s1=" + NumberUtils.hex(s1) + " s2=" + NumberUtils.hex(s2));

	}
	/**
	 * 
	 * @param driver
	 */
	public void poll(EEPRO100Driver driver) throws NetworkException {

		if (rxPackets[0].getStatus() != 0) {
			EEPRO100Registers regs = this.core.getRegs();
			// Got a packet, restart the receiver
			rxPackets[0].setStatus(0);
			rxPackets[0].setCommand(0xc000);

			regs.setReg16(SCBPointer, rxPackets[0].getStatus());
			regs.setReg16(SCBCmd, SCBMaskAll | RxStart);
			EEPRO100Utils.waitForCmdDone(regs);

			log.debug("Got a packet: Len="
					+ NumberUtils.hex(rxPackets[0].getCount()));
			
			final SocketBuffer skbuf = rxRing[0].getPacket();
			driver.onReceive(skbuf);
		}
	}

	/**
	 * 
	 *  
	 */
	public void txProcess() {
		/*
		 * Caution: the write order is important here, set the base address with
		 * the "ownership" bits last.
		 */

		/* Prevent interrupts from changing the Tx ring from underneath us. */
		int flags;

		/* Calculate the Tx descriptor entry. */
		// int mask = CpuControl.maskCPUInterrupts();
		int txEntry = getCurTx() & TX_RING_SIZE - 1;

		/*
		 * if (debug > 6) { sb.append("start: ").append(txEntry).append('
		 * ').append( Integer.toHexString(txRing[txEntry].bufferAddress));
		 * System.out.println(sb.toString()); sb.setLength(0); }
		 */
		/* Todo: be a little more clever about setting the interrupt bit. */
		txRing[txEntry].setStatus(0);
		txRing[txEntry].setCommand(CmdSuspend | CmdTx | CmdTxFlex);

		getNextTx();
		txRing[txEntry].setLink(txRing[getCurTx() & TX_RING_SIZE - 1]
				.getBufferAddress());
		/* We may nominally release the lock here. */
		txRing[txEntry]
				.setDescriptorAddress(txRing[txEntry].getBufferAddress() + 16);
		/* The data region is always in one buffer descriptor. */
		txRing[txEntry].setCount(getTxThreshold());

		/*
		 * Todo: perhaps leave the interrupt bit set if the Tx queue is more
		 * than half full. Argument against: we should be receiving packets and
		 * scavenging the queue. Argument for: if so, it shouldn't matter.
		 */

		EEPRO100TxFD lastCmd0 = lastCmd;
		lastCmd = txRing[txEntry];
		// lastCmd0.clearSuspend();

		EEPRO100Utils.waitForCmdDone(this.core.getRegs());
		this.core.getRegs().setReg8(SCBCmd, CUResume);
		// trans_start = jiffies;
	}

	// --- Accessors ---

	/**
	 * @return Returns the curRx.
	 */
	public int getCurRx() {
		return curRx;
	}

	/**
	 * @param curRx
	 *            The curRx to set.
	 */
	public void setCurRx(int curRx) {
		this.curRx = curRx;
	}

	/**
	 * @return Returns the curTx.
	 */
	public int getCurTx() {
		return curTx;
	}

	/**
	 * @return Returns the next Tx.
	 */
	public int getNextTx() {
		return curTx++;
	}

	/**
	 * @param curTx
	 *            The curTx to set.
	 */
	public void setCurTx(int curTx) {
		this.curTx = curTx;
	}

	/**
	 * @return Returns the dirtyRx.
	 */
	public int getDirtyRx() {
		return dirtyRx;
	}

	/**
	 * @param dirtyRx
	 *            The dirtyRx to set.
	 */
	public void setDirtyRx(int dirtyRx) {
		this.dirtyRx = dirtyRx;
	}

	/**
	 * @return Returns the dirtyTx.
	 */
	public int getDirtyTx() {
		return dirtyTx;
	}

	/**
	 * @param dirtyTx
	 *            The dirtyTx to set.
	 */
	public void setDirtyTx(int dirtyTx) {
		this.dirtyTx = dirtyTx;
	}

	/**
	 * @return Returns the txThreshold.
	 */
	public int getTxThreshold() {
		return txThreshold;
	}

	/**
	 * @param txThreshold
	 *            The txThreshold to set.
	 */
	public void setTxThreshold(int txThreshold) {
		this.txThreshold = txThreshold;
	}

	/**
	 * @return Returns the rxMode.
	 */
	public int getRxMode() {
		return rxMode;
	}

	/**
	 * @param rxMode
	 *            The rxMode to set.
	 */
	public void setRxMode(int rxMode) {
		this.rxMode = rxMode;
	}
}
