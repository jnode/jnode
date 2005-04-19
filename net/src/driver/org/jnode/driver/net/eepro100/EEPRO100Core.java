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

import java.security.PrivilegedExceptionAction;

import javax.naming.NameNotFoundException;

import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.driver.pci.PCIBaseAddress;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.pci.PCIDeviceConfig;
import org.jnode.naming.InitialNaming;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.system.IOResource;
import org.jnode.system.IRQHandler;
import org.jnode.system.IRQResource;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.util.Counter;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * @author flesire
 */
public class EEPRO100Core extends AbstractDeviceCore implements IRQHandler,
		EEPRO100Constants, EthernetConstants {
	/** Device Driver */
	private final EEPRO100Driver driver;

	/** Start of IO address space */
	private final int iobase;

	/** IO address space resource */
	private final IOResource io;

	/** IRQ resource */
	private final IRQResource irq;

	/** */
	private ResourceManager rm;

	/** My ethernet address */
	private EthernetAddress hwAddress;

	/** Registers */
	private EEPRO100Registers regs;

	/** Flags for the specific device found */
	private EEPRO100Flags flags;

	/** Statistical counters */
	private EEPRO100Stats stats;

	/** RX/TX */
	private EEPRO100Buffer buffers;

	/** */
	private int phy[];

	/** */
	private int eeReadCmd;

	/** */
	private int eeSize;

	/** */
	private int eeAddress;

	/** Enable congestion control in the DP83840. */
	final static boolean congenb = false;

	/** */
	boolean txFull;

	/**
	 * Create a new instance and allocate all resources
	 * 
	 * @throws ResourceNotFreeException
	 */
	public EEPRO100Core(EEPRO100Driver driver, ResourceOwner owner,
			PCIDevice device, Flags flags) throws ResourceNotFreeException,
			DriverException {

		phy = new int[2];

		this.driver = driver;
		this.flags = (EEPRO100Flags) flags;

		final PCIDeviceConfig config = device.getConfig();
		final int irq = config.getInterruptLine();
		final PCIBaseAddress[] addrs = config.getBaseAddresses();
		if (addrs.length < 1) {
			throw new DriverException("Cannot find iobase: not base addresses");
		}
		if (!addrs[1].isIOSpace()) {
			throw new DriverException(
					"Cannot find iobase: first address is not I/O");
		}

		// Get the start of the IO address space
		iobase = addrs[1].getIOBase();
		final int iolength = addrs[1].getSize();
		log.debug("Found Lance IOBase: 0x" + NumberUtils.hex(iobase)
				+ ", length: " + iolength);

		try {
			rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
		} catch (NameNotFoundException ex) {
			throw new DriverException("Cannot find ResourceManager");
		}
		this.irq = rm.claimIRQ(owner, irq, this, true);
		try {
			io = claimPorts(rm, owner, iobase, iolength);
		} catch (ResourceNotFreeException ex) {
			this.irq.release();
			throw ex;
		}
		
		short[] eeprom = new short[16];
        
		int eeSize;
        int eeReadCmd;

		if ((doEepromCmd2(EE_READ_CMD << 24, 27) & 0xffe0000) == 0xffe0000) {
			eeSize = 0x100;
			eeReadCmd = EE_READ_CMD << 24;

		} else {
			eeSize = 0x40;
			eeReadCmd = EE_READ_CMD << 22;
		}

		short sum = 0;
		for (int x = 0; x < eeSize; x++) {
			int value = doEepromCmd2(eeReadCmd | (x << 16), 27);
			if (x < (int)(eeprom.length))
				eeprom[x] = new Integer(value).shortValue();
			sum += value;
		}

		final byte[] hwAddrArr = new byte[ETH_ALEN];

		for (int a = 0; a < ETH_ALEN; a++) {
			hwAddrArr[a] = new Integer((eeprom[a / 2] >> (8 * (a & 1))) & 0xff)
					.byteValue();

		}

		if (sum != 0xBABA) {
			log.debug(this.flags.getName() + ": Invalid EEPROM checksum "
					+ Integer.toHexString(sum)
					+ ", check settings before activating this device!");
		}

		this.hwAddress = new EthernetAddress(hwAddrArr, 0);

                // Initialize registers.
		regs = new EEPRO100Registers(iobase, io);
		// Initialize statistical counters.
		stats = new EEPRO100Stats(rm, regs);
		// Initialize RX/TX Buffers.
		buffers = new EEPRO100Buffer(this);
                
		int  option = 0;
		
		/*
		 * Reset the chip: stop Tx and Rx processes and clear counters. This
		 * takes less than 10usec and will easily finish before the next action.
		 */
		regs.setReg32(SCBPort, PortReset);
		eepromDelay(10000);

		log.debug("Found " + flags.getName() + " IRQ=" + irq + ", IOBase=0x"
				+ NumberUtils.hex(iobase) + ", MAC Address=" + hwAddress);

		// We have a cyclone PMC52 card, IQ80310 board
		/*
		 * if(((Integer)deviceIdTable[deviceId+2]).intValue() == 0x360113c ||
		 * ((Integer)deviceIdTable[deviceId+2]).intValue() == 0x700113c) { //
		 * set the phy address; for 82559 this defaults to 1; phy[0] = 1; //
		 * read and print out the id registers sb.append("phy id:
		 * ").append(Integer.toHexString(mdioRead(phy[0], 2))); sb.append('
		 * ').append(Integer.toHexString(mdioRead(phy[0], 3)));
		 * System.out.println(sb.toString()); sb.setLength(0); // assuming we
		 * are i82555 } else {
		 */
		String connectors[] = { " RJ45", " BNC", " AUI", " MII" };
		if ((eeprom[3] & 0x03) != 0)
			log.info("Receiver lock-up bug exists -- enabling work-around.");
		log.debug("Board assembly " + Integer.toHexString(eeprom[8]) + " "
				+ Integer.toHexString(eeprom[9] >> 8) + "  "
				+ (eeprom[9] & 0xff) + " connectors present: ");
		for (int i = 0; i < 4; i++) {
			if ((eeprom[5] & (1 << i)) != 0)
				log.debug(connectors[i]);
		}
		log.debug("Primary interface chip " + (phys[(eeprom[6] >> 8) & 15]));
		log.debug(" PHY #" + (eeprom[6] & 0x1f));
		if ((eeprom[7] & 0x0700) != 0)
			log.debug("Secondary interface chip "
					+ (phys[(eeprom[7] >> 8) & 7]));

		if (((eeprom[6] >> 8) & 0x3f) == DP83840
				|| ((eeprom[6] >> 8) & 0x3f) == DP83840A) {
			int mdi_reg23 = mdioRead(eeprom[6] & 0x1f, 23) | 0x0422;
			if (congenb)
				mdi_reg23 |= 0x0100;
			log.debug("DP83840 specific setup, setting register 23 to "
					+ Integer.toHexString(mdi_reg23));
			mdioWrite(eeprom[6] & 0x1f, 23, mdi_reg23);
		}
		if ((option >= 0) && (option & 0x330) != 0) {
			log.debug("  Forcing " + ((option & 0x300) != 0 ? 100 : 10)
					+ "Mbs " + ((option & 0x220) != 0 ? "full" : "half")
					+ "-duplex operation.");
			mdioWrite(eeprom[6] & 0x1f, 0, ((option & 0x300) != 0 ? 0x2000 : 0)
					| /* 100mbps? */
					((option & 0x220) != 0 ? 0x0100 : 0)); /* Full duplex? */
		} else {
			int mii_bmcrctrl = mdioRead(eeprom[6] & 0x1f, 0);
			log.debug("Reset out of a transceiver left in 10baseT-fixed mode.");
			if ((mii_bmcrctrl & 0x3100) == 0)
				mdioWrite(eeprom[6] & 0x1f, 0, 0x8000);
		}
		/* Perform a system self-test. */
		byte[] data = new byte[32];
		MemoryResource selfTest = rm.asMemoryResource(data);
		log.debug("self test: "
				+ Integer.toHexString(selfTest.getAddress().toInt()));
		regs.setReg32(SCBPort, selfTest.getAddress().toInt() | PortSelfTest);
		/* rom signature */
		selfTest.setShort(2, (short) 0);
		/* Status */
		selfTest.setShort(0, (short) -1);
		/* Timeout for self-test */
		int boguscnt = 16000;
		do {
			systemDelay(10);
		} while (selfTest.getInt(0) == -1 && --boguscnt >= 0);

		if (boguscnt < 0) {
			log.debug("Self test failed, status"
					+ Long.toHexString(selfTest.getLong(4))
					+ "Failure to initialize the i82557.");
			log.debug("Verify that the card is a bus-master capable slot.");
		} else {
			int results = selfTest.getInt(0);
			log.debug("General self-test:"
					+ ((results & 0x1000) == 0 ? "failed" : "passed"));
			log.debug("Serial sub-system self-test: "
					+ ((results & 0x0020) == 0 ? "failed" : "passed"));
			log.debug("Internal registers self-test:"
					+ ((results & 0x0008) == 0 ? "failed" : "passed"));
			log.debug(" ROM checksum self-test:"
					+ ((results & 0x0004) == 0 ? "failed" : "passed") + "("
					+ Integer.toHexString(selfTest.getInt(2)) + ")");
		}
		/* reset adapter to default state */
		regs.setReg32(SCBPort, PortReset);
		systemDelay(100);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnode.driver.net.AbstractDeviceCore#getHwAddress()
	 */
	public HardwareAddress getHwAddress() {
		return hwAddress;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnode.driver.net.AbstractDeviceCore#initialize()
	 */
	public void initialize() {
		log.debug(flags.getName() + " : Init initialize");
		buffers.initSingleRxRing();
		buffers.initSingleTxRing();

		/*
		 * We can safely take handler calls during init. Doing this after
		 * initRxRing() results in a memory leak.
		 */
		setupInterrupt();
		/* Fire up the hardware. */
		/*
		 * buffers.resume(); buffers.setRxMode(-1); buffers.setRxMode();
		 */
		log.debug(this.flags.getName() + ": Done open(), status ");
		log.debug(flags.getName() + " : End initialize");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnode.driver.net.AbstractDeviceCore#disable()
	 */
	public void disable() {
		log.debug(flags.getName() + " : Init disable");
		// reset
		regs.setReg32(SCBPort, 0);
		// disable
		regs.setReg32(SCBPort, 2);
		Counter count = new Counter("chrono");
		while (((Integer) count.getValue()).intValue() <= 20)
			count.inc();
		regs.setReg16(SCBCmd, SCBMaskAll);
		int intr_status = regs.getReg16(SCBStatus);
		regs.setReg16(SCBStatus, intr_status);
		regs.getReg16(SCBStatus);

		log.debug(flags.getName() + " : End disable");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnode.driver.net.AbstractDeviceCore#release()
	 */
	public void release() {
		log.debug(flags.getName() + " : release");
		io.release();
		irq.release();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnode.driver.net.AbstractDeviceCore#transmit(org.jnode.net.SocketBuffer,
	 *      long)
	 */
	public void transmit(SocketBuffer buf, HardwareAddress destination,
			long timeout) throws InterruptedException, TimeoutException {
		log.debug(flags.getName() + " : Init transmit with TIMEOUT=" + timeout);
		// Set the source address
		hwAddress.writeTo(buf, 6);
		buffers.transmit(buf);
		log.debug(flags.getName() + " : End transmit");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jnode.system.IRQHandler#handleInterrupt(int)
	 */
	public void handleInterrupt(int irq) {
		log.debug(flags.getName() + " : Init handleInterrupt with IRQ=" + irq);
		try {
			buffers.poll(driver);
		} catch (NetworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// --- PRIVATE METHODS ---
	/**
	 * @param rm
	 * @param owner
	 * @param low
	 * @param length
	 * @return
	 */
	private IOResource claimPorts(final ResourceManager rm,
			final ResourceOwner owner, final int low, final int length)
			throws ResourceNotFreeException, DriverException {
		try {
			return (IOResource) AccessControllerUtils
					.doPrivileged(new PrivilegedExceptionAction() {

						public Object run() throws ResourceNotFreeException {
							return rm.claimIOResource(owner, low, length);
						}
					});
		} catch (ResourceNotFreeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new DriverException("Unknown exception", ex);
		}

	}

	// --- EEPROM METHODS ---
	final void eepromDelay() {
		// SystemResource.getTimer().udelay(4);
		int i = 2;
		while (i-- > 0)
			;
	}
	/**
	 * Delay between EEPROM clock transitions. The code works with no delay on
	 * 33Mhz PCI.
	 */
	final void eepromDelay(int ticks) {
		// SystemResource.getTimer().udelay(4);
		int i = ticks;
		while (i-- > 0)
			;
	}

	/**
	 * 
	 *  
	 */
	final void sizeEeprom() {

		regs.setReg16(SCBeeprom, EE_CS);
		int cmd = EE_READ_CMD << 8;
		int addressBits = 0;
		for (int i = 10; i >= 0; i--, addressBits++) {
			int data = (cmd & 1 << i) == 0 ? EE_WRITE_0 : EE_WRITE_1;
			regs.setReg16(SCBeeprom, data);
			regs.setReg16(SCBeeprom, data | EE_SHIFT_CLK);
			eepromDelay();
			regs.setReg16(SCBeeprom, data);
			eepromDelay();

			int ee = regs.getReg16(SCBeeprom);
			if ((ee & EE_DATA_READ) == 0) {
				if (addressBits == 8) {
					// 64 registers
					eeSize = 0x40;
					eeReadCmd = EE_READ_CMD << 6;
					eeAddress = 8;
				} else {
					// 256 registers
					eeSize = 0x100;
					eeReadCmd = EE_READ_CMD << 8;
					eeAddress = 10;
				}
				break;
			}
		}
		// read but discard
		for (int i = 0; i < 16; i++) {
			regs.setReg16(SCBeeprom, EE_CS);
			regs.setReg16(SCBeeprom, EE_CS | EE_SHIFT_CLK);
			eepromDelay();
			regs.setReg16(SCBeeprom, EE_CS);
			eepromDelay();
		}
		// disable the eeprom
		regs.setReg16(SCBeeprom, 0);
	}

	/**
	 * @param cmd
	 * @return
	 */
	final int doEepromCmd(int cmd) {
		int data = 0;

		regs.setReg16(SCBeeprom, EE_CS);
		cmd |= eeReadCmd;

		for (int i = eeAddress; true; i--) {
			data = (cmd & 1 << i) == 0 ? EE_WRITE_0 : EE_WRITE_1;
			regs.setReg16(SCBeeprom, data);
			regs.setReg16(SCBeeprom, data | EE_SHIFT_CLK);
			eepromDelay();
			regs.setReg16(SCBeeprom, data);
			eepromDelay();
			if ((regs.getReg16(SCBeeprom) & EE_DATA_READ) == 0)
				break;
		}
		data = 0;
		// read value
		for (int i = 0; i < 16; i++) {
			regs.setReg16(SCBeeprom, EE_CS);
			regs.setReg16(SCBeeprom, EE_CS | EE_SHIFT_CLK);
			eepromDelay();
			data <<= 1;
			if ((regs.getReg16(SCBeeprom) & EE_DATA_READ) != 0)
				data |= 1;

			regs.setReg16(SCBeeprom, EE_CS);
			eepromDelay();
		}
		// disable the eeprom
		regs.setReg16(SCBeeprom, 0);
		return data;
	}

	final int doEepromCmd2(int cmd, int cmdLength) {
		int retVal = 0;

		regs.setReg16(SCBeeprom, EE_ENB);
		eepromDelay(2);
		regs.setReg16(SCBeeprom, EE_ENB | EE_SHIFT_CLK);
		eepromDelay(2);
		do {
			int dataVal = (cmd & (1 << cmdLength)) == 0 ? EE_WRITE_0
					: EE_WRITE_1;
			regs.setReg16(SCBeeprom, dataVal);
			eepromDelay(2);
			regs.setReg16(SCBeeprom, dataVal | EE_SHIFT_CLK);
			eepromDelay(2);
			retVal = (retVal << 1)
					| ((regs.getReg16(SCBeeprom) & EE_DATA_READ) == 0 ? 1 : 0);
		} while (--cmdLength >= 0);
		regs.setReg16(SCBeeprom, EE_ENB);
		eepromDelay(2);

		regs.setReg16(SCBeeprom, EE_ENB & ~EE_CS);

		return retVal;
	}

	// --- OTHER METHODS
	/**
	 * @param delay
	 */
	final void systemDelay(int delay) {
		// SystemResource.getTimer().udelay(4);
		int i = 100;
		while (i-- > 0)
			;
	}

	// --- MD IO METHODS
	/**
	 * @param phy_id
	 * @param location
	 * @return
	 */
	final int mdioRead(int phy_id, int location) {
		int val, boguscnt = 64; /* <64 usec. to complete, typ 27 ticks */
		regs.setReg32(SCBCtrlMDI, 0x08000000 | (location << 16)
				| (phy_id << 21));
		do {
			systemDelay(10);
			val = regs.getReg32(SCBCtrlMDI);
			if (--boguscnt < 0) {
				log.debug(this.flags.getName()
						+ ": mdioRead() timed out with val = "
						+ Integer.toHexString(val));
				break;
			}
		} while ((val & 0x10000000) == 0);
		return val & 0xffff;
	}

	/**
	 * @param phy_id
	 * @param location
	 * @param value
	 * @return
	 */
	final int mdioWrite(int phy_id, int location, int value) {
		int val;
		/* <64 usec. to complete, typ 27 ticks */
		int boguscnt = 64;
		regs.setReg32(SCBCtrlMDI, 0x04000000 | (location << 16)
				| (phy_id << 21) | value);
		do {
			systemDelay(10);
			val = regs.getReg32(SCBCtrlMDI);
			if (--boguscnt < 0) {
				// StringBuffer sb = new StringBuffer();
				log.debug("eepro100: mdioWrite() timed out with val ="
						+ Integer.toHexString(val));
				break;
			}
		} while ((val & 0x10000000) == 0);
		return val & 0xffff;
	}

	/**
	 * 
	 *  
	 */
	public void setupInterrupt() {
		try {

			int bogusCount = 20;
			int status;

			if ((buffers.getCurRx() - buffers.getDirtyRx()) > 15) {
				log.debug("curRx > dirtyRx " + buffers.getCurRx() + " "
						+ buffers.getDirtyRx());
				// showstate();
			}

			do {
				status = regs.getReg16(SCBStatus);
				regs.setReg16(SCBStatus, status & IntrAllNormal);
				if ((status & IntrAllNormal) == 0)
					break;
				if ((status & (IntrRxDone | IntrRxSuspend)) != 0)
					buffers.rx(driver);

				if ((status & (IntrCmdDone | IntrCmdIdle | IntrDrvrIntr)) != 0) {
					int dirtyTx0;
					dirtyTx0 = buffers.getDirtyTx();
					while ((buffers.getCurTx() - dirtyTx0) > 0) {
						int entry = dirtyTx0 & (TX_RING_SIZE - 1);
						status = buffers.txRing[entry].getStatus();
						if ((status & StatusComplete) == 0) {
							if ((buffers.getCurTx() - dirtyTx0) > 0
									&& (buffers.txRing[(dirtyTx0 + 1)
											& TX_RING_SIZE - 1].getStatus() & StatusComplete) != 0) {
								log
										.debug("Command unit failed to mark command."
												+ NumberUtils.hex(status)
												+ "as complete at "
												+ buffers.getDirtyTx());
							} else {
								break;
							}

						}

						if ((status & TxUnderrun) != 0) {
							if (buffers.getTxThreshold() < 0x01e00000) {
								buffers
										.setTxThreshold(buffers
												.getTxThreshold() + 0x00040000);
							}
						}
						if ((status & 0x70000) == CmdNOp) {
							// mc_setup_busy = 0;
						}
						dirtyTx0++;
					}
					if (buffers.getCurTx() - dirtyTx0 > TX_RING_SIZE) {
						log.debug("out-of-sync dirty pointer, " + dirtyTx0
								+ " vs. " + buffers.getCurTx() + " full="
								+ txFull);
						dirtyTx0 += TX_RING_SIZE;
					}

					buffers.setDirtyTx(dirtyTx0);
					if (txFull
							&& buffers.getCurTx() - buffers.getDirtyTx() < TX_QUEUE_UNFULL) {
						/*
						 * The ring is no longer full, clear tbusy.
						 */
						txFull = false;
						// netif_resume_tx_queue(dev);
					}
				}
				if ((status & IntrRxSuspend) != 0) {
					// interruptError(status);
				}

				if (--bogusCount < 0) {
					/*
					 * StringBuffer sb = new StringBuffer();
					 * sb.append(name).append(": Too much work at interrupt,
					 * status="); sb.append(Integer.toHexString(status));
					 * System.out.println(sb.toString());
					 */
					/*
					 * Clear all interrupt sources.
					 */
					regs.setReg16(SCBStatus, 0xfc00);
					break;
				}

			} while (true);
			log.debug(flags.getName() + " : End handleInterrupt");
			return;
		} catch (NetworkException e) {
			e.printStackTrace();
		}

	}

	// --- Accessors ---

	/**
	 * @return Returns the rm.
	 */
	public ResourceManager getRm() {
		return rm;
	}

	/**
	 * @return Returns the buffers.
	 */
	public EEPRO100Buffer getBuffers() {
		return buffers;
	}

	/**
	 * @return Returns the flags.
	 */
	public EEPRO100Flags getFlags() {
		return flags;
	}

	/**
	 * @return Returns the regs.
	 */
	public EEPRO100Registers getRegs() {
		return regs;
	}

	/**
	 * @return Returns the stats.
	 */
	public EEPRO100Stats getStats() {
		return stats;
	}
}