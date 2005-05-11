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
import org.jnode.net.HardwareAddress;
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
	/** Logger */
	protected final Logger log = Logger.getLogger(getClass());
	/** Resource manager */
	private ResourceManager rm;
	/** registers */
	private EEPRO100Registers regs;
	
	private EEPRO100RxFD rxPacket;
	private EEPRO100TxFD txFD;
	
	
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
	

	

	

	int jiffies;

	/**
	 *  
	 */
	public EEPRO100Buffer(EEPRO100Registers regs, ResourceManager rm) {
		this.regs = regs;
		this.rm = rm;
		/* Set up the Tx queue early.. */
		curTx = 0;
		dirtyTx = 0;
	}
	/**
	 * 
	 *
	 */
	public final void initSingleRxRing() {
		log.debug("Set RX base addr.");
		
		rxPacket = new EEPRO100RxFD(rm);
		rxPacket.setStatus(0x0001);
		rxPacket.setCommand(0x0000);
		rxPacket.setLink(rxPacket.getStatus());
		// TODO Set correct value
		rxPacket.setRxBufferAddress(0);
		// ---------------------------------
		rxPacket.setCount(0);
		rxPacket.setSize(1528);
		// Start the receiver.
		regs.setReg32(SCBPointer, rxPacket.getBufferAddress());
		regs.setReg16(SCBCmd, SCBMaskAll | RxStart);
		EEPRO100Utils.waitForCmdDone(regs);

		log.debug("Started rx process.");

		rxPacket.setStatus(0);
		rxPacket.setCommand(0xC000);

		regs.setReg32(SCBPointer, rxPacket.getBufferAddress());
		regs.setReg16(SCBCmd, SCBMaskAll | RxStart);
	}
	/**
	 * 
	 *
	 */
	public final void initSingleTxRing() {
		EEPRO100Utils.waitForCmdDone(regs);
		log.debug("Set TX base addr.");
		txFD = new EEPRO100TxFD(rm);
		txFD.setCommand(CmdIASetup);
		txFD.setStatus(0x0000);
		//txFD.setLink();
		//txFD.setDescriptorAddress();
	}

	/**
	 * @param buf
	 */
	public void transmit(SocketBuffer buf) {

		EthernetHeader hdr = (EthernetHeader) buf.getLinkLayerHeader();

		log.debug("HDR =" + hdr);

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
		txRing[0].setLink(txFD.getBufferAddress());
		txRing[0].setCount(0x02208000);

		regs.setReg16(SCBPointer, txRing[0].getBufferAddress());
		regs.setReg16(SCBCmd, SCBMaskAll | CUStart);
		EEPRO100Utils.waitForCmdDone(regs);

		s1 = regs.getReg16(SCBStatus);
		// TODO wait 10 ms for transmiting;
		long start =  System.currentTimeMillis();
		while((System.currentTimeMillis() <= start + 10) && (txRing[0].getStatus() != 0)){
		}
		s2 = regs.getReg16(SCBStatus);

		log.debug("s1=" + NumberUtils.hex(s1) + " s2=" + NumberUtils.hex(s2));

	}
	/**
	 * 
	 * @param driver
	 */
	public void poll(EEPRO100Driver driver) throws NetworkException {
		if (rxPacket.getStatus() != 0) {
			// Got a packet, restart the receiver
			rxPacket.setStatus(0);
			rxPacket.setCommand(0xc000);

			regs.setReg16(SCBPointer, rxPacket.getStatus());
			regs.setReg16(SCBCmd, SCBMaskAll | RxStart);
			EEPRO100Utils.waitForCmdDone(regs);

			log.debug("Got a packet: Len="
					+ NumberUtils.hex(rxPacket.getCount()));
			
			final SocketBuffer skbuf = rxPacket.getPacket();
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

		EEPRO100Utils.waitForCmdDone(regs);
		regs.setReg8(SCBCmd, CUResume);
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
