/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.jnode.driver.net.eepro100;

import java.security.PrivilegedExceptionAction;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIBaseAddress;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIHeaderType0;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.naming.InitialNaming;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.system.IOResource;
import org.jnode.system.IRQHandler;
import org.jnode.system.IRQResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.util.Counter;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * @author Fabien Lesire (galatnm at gmail dot com)
 */
public class EEPRO100Core extends AbstractDeviceCore implements IRQHandler, EEPRO100Constants,
        EthernetConstants {
    /**
     * Device Driver
     */
    private final EEPRO100Driver driver;

    /**
     * Start of IO address space
     */
    private final int iobase;

    /**
     * IO address space resource
     */
    private final IOResource io;

    /**
     * IRQ
     */
    private final IRQResource irq;
 
    private ResourceManager rm;

    /**
     * My ethernet address
     */
    private EthernetAddress hwAddress;
 
    private int[] eeprom;

    /**
     * Registers
     */
    private EEPRO100Registers regs;

    /**
     * Flags for the specific device found
     */
    private EEPRO100Flags flags;

    /**
     * Statistical counters
     */
    private EEPRO100Stats stats;

    /**
     * RX/TX
     */
    private EEPRO100Buffer buffers;
 
    private int phy[]; 
    private int eeReadCmd; 
    private int eeSize; 
    private int eeAddress;

    /**
     * Enable congestion control in the DP83840.
     */
    static final boolean congenb = false;

    boolean txFull;

    /**
     * Create a new instance and allocate all resources
     *
     * @throws ResourceNotFreeException
     */
    public EEPRO100Core(EEPRO100Driver driver, ResourceOwner owner, PCIDevice device,
            EEPRO100Flags flags) throws ResourceNotFreeException, DriverException {

        phy = new int[2];

        this.driver = driver;
        this.flags = flags;

        // Get the start of the IO address space
        this.iobase = getIOBase(device, flags);
        final int iolength = getIOLength(device, flags);

        log.debug("Found EEPRO100 IOBase: 0x" + NumberUtils.hex(iobase) + ", length: " + iolength);

        try {
            rm = InitialNaming.lookup(ResourceManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find ResourceManager");
        }
        final int irq = getIRQ(device, flags);
        this.irq = rm.claimIRQ(owner, irq, this, true);
        try {
            io = claimPorts(rm, owner, iobase, iolength);
        } catch (ResourceNotFreeException ex) {
            this.irq.release();
            throw ex;
        }

        // Initialize registers.
        regs = new EEPRO100Registers(iobase, io);
        // Initialize statistical counters.
        stats = new EEPRO100Stats(rm, regs);

        eeprom = new int[100];

        int eeSize;
        int eeReadCmd;

        if ((doEepromCmd(EE_READ_CMD << 24, 27) & 0xffe0000) == 0xffe0000) {
            eeSize = 0x100;
            eeReadCmd = EE_READ_CMD << 24;

        } else {
            eeSize = 0x40;
            eeReadCmd = EE_READ_CMD << 22;
        }

        log.debug("EEProm size: " + NumberUtils.hex(eeSize) + " read command: " + eeReadCmd);

        int x, y, sum;
        final byte[] hwAddrArr = new byte[ETH_ALEN];

        for (y = 0, x = 0, sum = 0; x < eeSize; x++) {
            int value = doEepromCmd((eeReadCmd | (x << 16)), 27);
            eeprom[x] = value;
            sum += new Integer(value).shortValue();
            if (x < 3) {
                hwAddrArr[y++] = (byte) value;
                hwAddrArr[y++] = (byte) (value >> 8);
            }
        }

        this.hwAddress = new EthernetAddress(hwAddrArr, 0);

        if (sum != 0xBABA) {
            log.debug(this.flags.getName() + ": Invalid EEPROM checksum " + NumberUtils.hex(sum) +
                    ", check settings before activating this device!");
        }

        regs.setReg32(SCBPort, PortReset);
        systemDelay(1000);

        /*
         * Reset the chip: stop Tx and Rx processes and clear counters. This
         * takes less than 10usec and will easily finish before the next action.
         */
        regs.setReg32(SCBPort, PortReset);
        eepromDelay(10000);

        log.debug("Found " + flags.getName() + " IRQ=" + irq + ", IOBase=0x" +
                NumberUtils.hex(iobase) + ", MAC Address=" + hwAddress);

    }

    public HardwareAddress getHwAddress() {
        return hwAddress;
    }

    public void initialize() {
        log.debug(flags.getName() + " : Init initialize");
        // Initialize RX/TX Buffers.
        buffers = new EEPRO100Buffer(this.regs, this.rm);
        buffers.initSingleRxRing();
        buffers.initSingleTxRing();

        int option = 0x00;

        if (((eeprom[6] >> 8) & 0x3f) == DP83840 || ((eeprom[6] >> 8) & 0x3f) == DP83840A) {
            int mdi_reg23 = mdioRead(eeprom[6] & 0x1f, 23) | 0x0422;
            if (congenb)
                mdi_reg23 |= 0x0100;
            log.debug("DP83840 specific setup, setting register 23 to " +
                    Integer.toHexString(mdi_reg23));
            mdioWrite(eeprom[6] & 0x1f, 23, mdi_reg23);
        }

        if (option != 0) {
            mdioWrite(eeprom[6] & 0x1f, 0, ((option & 0x20) != 0 ? 0x2000 : 0) | /* 100mbps? */
                      ((option & 0x10) != 0 ? 0x0100 : 0)); /* Full duplex? */
        }

        /* reset adapter to default state */
        regs.setReg32(SCBPort, PortReset);
        systemDelay(100);
        setupInterrupt();
        log.debug(this.flags.getName() + ": Done open(), status ");
        log.debug(flags.getName() + " : End initialize");
    }

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

    public void release() {
        log.debug(flags.getName() + " : release");
        io.release();
        irq.release();
    }

    public void transmit(SocketBuffer buf, HardwareAddress destination, long timeout)
        throws InterruptedException, TimeoutException {
        log.debug(flags.getName() + " : Init transmit with TIMEOUT=" + timeout);
        // Set the source address
        hwAddress.writeTo(buf, 6);
        buffers.transmit(buf);
        log.debug(flags.getName() + " : End transmit");
    }

    public void handleInterrupt(int irq) {
        log.debug(flags.getName() + " : Init handleInterrupt with IRQ=" + irq);
        try {
            buffers.poll(driver);
        } catch (NetworkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Gets the first IO-Address used by the given device
     *
     * @param device
     * @param flags
     */
    protected int getIOBase(Device device, EEPRO100Flags flags) throws DriverException {
        final PCIHeaderType0 config = ((PCIDevice) device).getConfig().asHeaderType0();
        final PCIBaseAddress[] addrs = config.getBaseAddresses();
        for (int i = 0; i < addrs.length; i++) {
            long addr;
            if (addrs[i].isIOSpace()) {
                addr = addrs[i].getIOBase();
            } else {
                addr = addrs[i].getMemoryBase();
            }
            log.debug("PCIBaseAddress[" + i + "]: " + addrs[i].isIOSpace() + " addr: " +
                    NumberUtils.hex(addr));
        }
        if (addrs.length < 1) {
            throw new DriverException("Cannot find iobase: not base addresses");
        }
        if (!addrs[1].isIOSpace()) {
            throw new DriverException("Cannot find iobase: first address is not I/O");
        }
        return addrs[1].getIOBase();
    }

    /**
     * Gets the number of IO-Addresses used by the given device
     *
     * @param device
     * @param flags
     */
    protected int getIOLength(Device device, EEPRO100Flags flags) throws DriverException {
        final PCIHeaderType0 config = ((PCIDevice) device).getConfig().asHeaderType0();
        final PCIBaseAddress[] addrs = config.getBaseAddresses();
        if (addrs.length < 1) {
            throw new DriverException("Cannot find iobase: not base addresses");
        }
        if (!addrs[1].isIOSpace()) {
            throw new DriverException("Cannot find iobase: first address is not I/O");
        }
        return addrs[1].getSize();
    }

    /**
     * Gets the IRQ used by the given device
     *
     * @param device
     * @param flags
     */
    protected int getIRQ(Device device, EEPRO100Flags flags) throws DriverException {
        final PCIHeaderType0 config = ((PCIDevice) device).getConfig().asHeaderType0();
        return config.getInterruptLine();
    }

    // --- PRIVATE METHODS ---
    /**
     * @param rm
     * @param owner
     * @param low
     * @param length
     * @return
     */
    private IOResource claimPorts(final ResourceManager rm, final ResourceOwner owner,
            final int low, final int length) throws ResourceNotFreeException, DriverException {
        try {
            return AccessControllerUtils.doPrivileged(new PrivilegedExceptionAction<IOResource>() {
                public IOResource run() throws ResourceNotFreeException {
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
        int i = ticks;
        while (i-- > 0)
            ;
    }

    /**
     * @param cmd
     * @param cmdLength
     * @return
     */
    final int doEepromCmd(int cmd, int cmdLength) {
        int retVal = 0;

        regs.setReg16(SCBeeprom, EE_ENB);
        eepromDelay(2);
        regs.setReg16(SCBeeprom, EE_ENB | EE_SHIFT_CLK);
        eepromDelay(2);
        do {
            // FIXME ... what is this craziness??
            short dataVal =
                    new Integer(((cmd & (1 << cmdLength)) == 0) ? EE_WRITE_0 : EE_WRITE_1)
                            .shortValue();
            regs.setReg16(SCBeeprom, dataVal);
            eepromDelay(2);
            regs.setReg16(SCBeeprom, dataVal | EE_SHIFT_CLK);
            eepromDelay(2);
            retVal = (retVal << 1) | (((regs.getReg16(SCBeeprom) & EE_DATA_READ) != 0) ? 1 : 0);
        } while (--cmdLength >= 0);
        regs.setReg16(SCBeeprom, EE_ENB);
        eepromDelay(2);
        regs.setReg16(SCBeeprom, (EE_ENB & ~EE_CS));
        return NumberUtils.toUnsigned(new Integer(retVal).shortValue());
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
    public final int mdioRead(int phy_id, int location) {
        int val;
        int boguscnt = 64 * 4;
        regs.setReg32(SCBCtrlMDI, 0x08000000 | (location << 16) | (phy_id << 21));
        do {
            systemDelay(16);
            val = regs.getReg32(SCBCtrlMDI);
            if (--boguscnt < 0) {
                log.debug(this.flags.getName() + ": mdioRead() timed out with val = " +
                        Integer.toHexString(val));
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
    public final int mdioWrite(int phy_id, int location, int value) {
        int val;
        int boguscnt = 64 * 4;
        regs.setReg32(SCBCtrlMDI, 0x04000000 | (location << 16) | (phy_id << 21) | value);
        do {
            systemDelay(16);
            val = regs.getReg32(SCBCtrlMDI);
            if (--boguscnt < 0) {
                // StringBuffer sb = new StringBuffer();
                log.debug("eepro100: mdioWrite() timed out with val =" + Integer.toHexString(val));
                break;
            }
        } while ((val & 0x10000000) == 0);
        return val & 0xffff;
    }
 
    public void setupInterrupt() {
        int bogusCount = 20;
        int status;

        if ((buffers.getCurRx() - buffers.getDirtyRx()) > 15) {
            log.debug("curRx > dirtyRx " + buffers.getCurRx() + " " + buffers.getDirtyRx()); 
        }

        do {
            status = regs.getReg16(SCBStatus);
            regs.setReg16(SCBStatus, status & IntrAllNormal);
            if ((status & IntrAllNormal) == 0)
                break;
            if ((status & (IntrRxDone | IntrRxSuspend)) != 0)
                // buffers.rx(driver);

                if ((status & (IntrCmdDone | IntrCmdIdle | IntrDrvrIntr)) != 0) {
                    int dirtyTx0;
                    dirtyTx0 = buffers.getDirtyTx();
                    while ((buffers.getCurTx() - dirtyTx0) > 0) {
                        int entry = dirtyTx0 & (TX_RING_SIZE - 1);
                        status = buffers.txRing[entry].getStatus();
                        if ((status & StatusComplete) == 0) {
                            if ((buffers.getCurTx() - dirtyTx0) > 0 &&
                                    (buffers.txRing[(dirtyTx0 + 1) & TX_RING_SIZE - 1].
                                            getStatus() & StatusComplete) != 0) {
                                log.debug("Command unit failed to mark command." +
                                        NumberUtils.hex(status) + "as complete at " +
                                        buffers.getDirtyTx());
                            } else {
                                break;
                            }

                        }

                        if ((status & TxUnderrun) != 0) {
                            if (buffers.getTxThreshold() < 0x01e00000) {
                                buffers.setTxThreshold(buffers.getTxThreshold() + 0x00040000);
                            }
                        }
                        if ((status & 0x70000) == CmdNOp) {
                            // mc_setup_busy = 0;
                        }
                        dirtyTx0++;
                    }
                    if (buffers.getCurTx() - dirtyTx0 > TX_RING_SIZE) {
                        log.debug("out-of-sync dirty pointer, " + dirtyTx0 + " vs. " +
                                buffers.getCurTx() + " full=" + txFull);
                        dirtyTx0 += TX_RING_SIZE;
                    }

                    buffers.setDirtyTx(dirtyTx0);
                    if (txFull && buffers.getCurTx() - buffers.getDirtyTx() < TX_QUEUE_UNFULL) {
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
                 * Clear all interrupt sources.
                 */
                regs.setReg16(SCBStatus, 0xfc00);
                break;
            }

        } while (true);
        log.debug(flags.getName() + " : End handleInterrupt");
        return;

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
