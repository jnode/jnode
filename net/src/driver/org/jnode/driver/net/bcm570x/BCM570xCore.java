/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.driver.net.bcm570x;

import java.security.PrivilegedExceptionAction;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIBaseAddress;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIHeaderType0;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.ethernet.spi.Flags;
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
import org.jnode.util.TimeoutException;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class BCM570xCore extends AbstractDeviceCore implements BCM570xConstants, IRQHandler,
        EthernetConstants {
    /**
     * Start of IO address space
     */
    private final int iobase;
    /**
     * IO address space
     */
    private final IOResource io;
    /**
     * IRQ
     */
    private final IRQResource irq;
    /**
     * My ethernet address
     */
    private EthernetAddress hwAddress = null;
    /**
     * flags needed to setup device
     */
    private final BCM570xFlags flags;
    /**
     * main driver this belongs to
     */
    private final BCM570xDriver driver;
    /** The receive buffer ring */
    // private final RTL8139RxRing rxRing = null;
    /** The transmit buffer */
    // private final RTL8139TxBuffer[] txBuffers = new RTL8139TxBuffer[4];
    /**
     * Is a transmission active?
     */
    // private boolean tx_active;
    private int txIndex;
    private int txAborted;
    private int txNumberOfPackets;
    private int txPending;

    /**
     * Create a new instance
     * 
     * @param flags
     */
    public BCM570xCore(BCM570xDriver driver, ResourceOwner owner, PCIDevice device, Flags flags)
        throws DriverException, ResourceNotFreeException {
        if (!(flags instanceof BCM570xFlags))
            throw new DriverException("Wrong flags to the BCM570x driver");

        this.driver = driver;
        log.info(driver);
        this.flags = (BCM570xFlags) flags;

        final int irq = getIRQ(device, this.flags);
        log.info("BCM570x driver irq " + irq);

        // Get the start of the IO address space
        this.iobase = getIOBase(device, this.flags);

        final int iolength = getIOLength(device, this.flags);
        log.info("BCM570x driver iobase " + iobase + " irq " + irq);
        log.debug("BCM570x driver iobase " + iobase + " irq " + irq);

        final ResourceManager rm;

        try {
            rm = InitialNaming.lookup(ResourceManager.NAME);
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

        /*
         * this.rxRing = new RTL8139RxRing(RX_FRAMES, rm);
         * 
         * for (int i = 0; i < txBuffers.length; i++) { txBuffers[i] = new
         * RTL8139TxBuffer(rm); setReg32(REG_TX_ADDR0 + (4 * i),
         * txBuffers[i].getFirstDPDAddress().toInt()); }
         * 
         * powerUpDevice(); reset();
         * 
         * byte[] adr1 = i2bsLoHi(getReg32(REG_MAC0)); byte[] adr2 =
         * i2bsLoHi(getReg32(REG_MAC0 + 4));
         * 
         * final byte[] hwAddrArr = new byte[ETH_ALEN];
         * 
         * hwAddrArr[0] = adr1[0]; hwAddrArr[1] = adr1[1]; hwAddrArr[2] =
         * adr1[2]; hwAddrArr[3] = adr1[3]; hwAddrArr[4] = adr2[0]; hwAddrArr[5] =
         * adr2[1];
         * 
         * this.hwAddress = new EthernetAddress(hwAddrArr, 0);
         *  // disable multicast setReg32(REG_MAR0, 0); setReg32(REG_MAR0 + 4,
         * 0);
         * 
         * log.debug("Found " + flags.getName() + " IRQ=" + irq + ", IOBase=0x" +
         * NumberUtils.hex(iobase) + ", MAC Address=" + hwAddress);
         */
    }

    private void powerUpDevice() {
        setReg8(REG_CFG9346, CFG9346_WE);
        setReg8(REG_CONFIG1, 0);
        setReg8(REG_CFG9346, 0);
    }

    private void reset() {
        // FIXME ... what is with all the fixed delays???
        txIndex = 0;
        int i;
        setReg8(REG_CHIPCMD, CMD_RESET);
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            // ignore
        }

        for (i = 0; i < REPEAT_TIMEOUT_COUNT; i++) {
            try {
                Thread.sleep(GENERIC_WAIT_TIME);
            } catch (InterruptedException ex) {
                // ignore
            }

            if ((getReg8(REG_CHIPCMD) & CMD_RESET) == 0)
                break;
        }

        if (i == REPEAT_TIMEOUT_COUNT)
            log.debug("Ethernet card: Chip Reset incomplete");

        setReg16(BMCR, BMCR_RESET);

        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            // ignore
        }

        for (i = 0; i < REPEAT_TIMEOUT_COUNT; i++) {
            try {
                Thread.sleep(GENERIC_WAIT_TIME);
            } catch (InterruptedException ex) {
                // ignore
            }

            if ((getReg16(BMCR) & BMCR_RESET) == 0)
                break;
        }

        if (i == REPEAT_TIMEOUT_COUNT)
            log.debug("Ethernet card: BMCR Reset incomplete");

        // Autoload from the eeprom
        setReg8(REG_CFG9346, CFG9346_AUTOLOAD);

        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            // ignore
        }

        for (i = 0; i < REPEAT_TIMEOUT_COUNT; i++) {
            try {
                Thread.sleep(GENERIC_WAIT_TIME);
            } catch (InterruptedException ex) {
                // ignore
            }

            if ((getReg8(REG_CFG9346) & 0xc0) == 0)
                break;
        }
        if (i == REPEAT_TIMEOUT_COUNT)
            log.debug("Ethernet card: Autoload incomplete");
    }

    private byte[] i2bsLoHi(int _i) {
        int shiftL = 24, shiftH = shiftL;

        byte[] bs = new byte[4];

        for (int i = 0; i < 4; i++) {
            bs[i] = (byte) ((_i << shiftH) >>> shiftL);
            shiftH = shiftH - 8;
        }
        return bs;
    }

    private void autoNegotiate() {

        // boolean fullDuplex = false;

        // start auto negotiating
        setReg16(REG_INTR_MASK, INTR_MASK);

        int status = getReg16(REG_INTR_STATUS);

        if ((status & INTR_LNKCHG) != 0) {
            log.debug("AN: link changed! " + Integer.toHexString(status));

            setReg16(REG_INTR_STATUS, status);
        }

        setReg8(REG_CFG9346, 0xC0);
        setReg16(BMCR, 0x1200);

        status = getReg16(BMSR);

        int bogusCount = 0;
        while ((status & 0x30) == 0) {

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                // ignore
            }

            bogusCount++;
            if (bogusCount >= AUTO_NEGOTIATE_TIMEOUT) {
                log.debug("Bogus count: autonegotiating taking too long: " +
                        Integer.toHexString(status));
                break;
            }
            status = getReg16(BMSR);
        }

        log.debug("autonegotiating status: " + Integer.toHexString(status));

        if ((status & 0x20) != 0)
            log.debug("autonegotiating complete");
        if ((status & 0x10) != 0)
            log.debug("remote fault detected");
        if ((status & 0x4) != 0)
            log.debug("link valid");

        /* int lpar = */
        getReg16(NWAY_LPAR);

        log.debug("MSR: " + Integer.toHexString(getReg8(MSR)) + " BMCR: " +
                Integer.toHexString(getReg16(BMCR)) + " LPAR: " +
                Integer.toHexString(getReg16(NWAY_LPAR)));

        // if (lpar == 0xffff) {
        // } else if (((lpar & 0x0100) == 0x0100) || ((lpar & 0x00C0) ==
        // 0x0040)) {
        // fullDuplex = true;
        // }

        // if (fullDuplex) rtl8139.write8(REG_CONFIG1, 0x60); // check
        // else rtl8139.write8(REG_CONFIG1, 0x20);

        setReg8(REG_CFG9346, 0x00);

        // if (fullDuplex) System.out.print("AutoNegotiation: Full Duplex ");
        // else System.out.print("AutoNegotiation: Half Duplex ");

        // if ((lpar & 0x0180) != 0) System.out.println("100 Mbps Mode");
        // else System.out.println("10 Mbps Mode");

        return;
    }

    /**
     * Gets the hardware address of this device
     */
    public HardwareAddress getHwAddress() {
        return hwAddress;
    }

    /**
     * Initialize the device
     */
    public void initialize() {
        // reset the device
        reset();

        // initialize our buffer
        // rxRing.initialize();

        // setReg32(REG_RX_BUF, rxRing.getFirstUPDAddress().toInt());

        autoNegotiate();

        // enable tx/rx
        enableTxRx();

        // setup tx configuration
        setReg32(REG_TX_CONFIG, txConfig);
        // setup rx configuration
        setReg32(REG_RX_CONFIG, rxConfig);

        // unlock config registers
        setReg8(REG_CFG9346, 0xc0);

        // setups up LED pin defs, vital product data, and power management
        int reg = getReg8(REG_CONFIG1);

        // disable power management
        reg &= 0xfe;
        reg |= 0xc0;
        setReg8(REG_CONFIG1, reg);

        // setup config3
        setReg8(REG_CONFIG3, 0x40);
        // Setup config4
        setReg8(REG_CONFIG4, 0x2);
        setReg8(REG_CFG9346, 0);

        // clear rx missed
        setReg16(REG_RX_MISSED, 0x00000000);

        // enable tx/rx
        enableTxRx();
        // Enable interrupts
        setReg16(REG_INTR_MASK, INTR_MASK);
    }

    private void enableTxRx() {
        setReg8(REG_CHIPCMD, CMD_TX_ENABLE | CMD_RX_ENABLE);
    }

    /**
     * Disable the device
     */
    public void disable() {
        reset();
    }

    /**
     * Release all resources
     */
    public void release() {
        io.release();
        irq.release();
    }

    /**
     * Transmit the given buffer
     * 
     * @param buf
     * @param timeout
     * @throws InterruptedException
     * @throws org.jnode.util.TimeoutException
     * 
     */
    public void transmit(SocketBuffer buf, HardwareAddress destination, long timeout)
        throws InterruptedException, TimeoutException {
        // Set the source address
        hwAddress.writeTo(buf, 6);
        // tx_active = true;

        txNumberOfPackets++;
        // Set the address of the txBuffer

        setReg32(REG_CFG9346, CFG9346_WE);

        // this should be a bug fix, but looking (in Ethereal) at the packes
        // send
        // indicate on my card that this is not true
        // Martin
        /*
         * if (txIndex == 0) { txBuffers[0].initialize(buf);
         * setReg32(REG_TX_STATUS0, txFlag | buf.getSize());
         * 
         * txBuffers[1].initialize(buf); setReg32(REG_TX_STATUS0 + 4, txFlag |
         * buf.getSize());
         * 
         * txIndex = 2; } else {
         */
        // txBuffers[txIndex].initialize(buf);
        setReg32(REG_TX_STATUS0 + 4 * txIndex, txFlag | buf.getSize());

        // Point to the next empty descriptor
        txIndex++;
        txIndex &= 3;
        // }

        setReg32(REG_CFG9346, CFG9346_NORMAL);
    }

    /**
     * Handle a given hardware interrupt. This method is called from the kernel
     * with interrupts disabled. So keep and handling here as short as possible!
     */
    public void handleInterrupt(int irq) {
        int bogusCount = 20;
        // Keep processing interrupts until there are none to process
        boolean linkChanged = false;

        while (true) {
            int status = getReg16(REG_INTR_STATUS);

            if (status == 0xffff) {
                break;
            }

            // See if anything needs servicing
            if ((status & (INTR_RX_OK | INTR_RX_ERR | INTR_TX_OK | INTR_TX_ERR |
                    INTR_RX_BUF_OVRFLO | INTR_RX_FIFO_OVRFLO | INTR_TIMEOUT | INTR_SYS_ERR |
                    INTR_RX_UNDERRUN | INTR_LEN_CHG)) == 0) {
                break;
            }

            if ((status & INTR_RX_UNDERRUN) == INTR_RX_UNDERRUN) {
                linkChanged = (getReg16(REG_CSCR) & CSCR_LINKCHANGE) == CSCR_LINKCHANGE;
                if (linkChanged) {
                    log.debug("Link changed");
                }
            }

            if ((status & INTR_RX_FIFO_OVRFLO) == INTR_RX_FIFO_OVRFLO) {
                setReg16(REG_INTR_STATUS, status | INTR_RX_BUF_OVRFLO);
            } else {
                setReg16(REG_INTR_STATUS, status);
            }

            // Process tx interrupts
            if ((status & (INTR_TX_OK | INTR_TX_ERR)) != 0) {
                txProcess(status);
            }

            // Process rx interrupts
            if ((status & (INTR_RX_OK | INTR_RX_FIFO_OVRFLO | INTR_RX_BUF_OVRFLO)) != 0) {
                rxProcess(status);
            }

            // Process the other errors
            if ((status & (INTR_RX_ERR | INTR_TX_ERR | INTR_RX_BUF_OVRFLO | INTR_RX_FIFO_OVRFLO |
                    INTR_TIMEOUT | INTR_SYS_ERR | INTR_RX_UNDERRUN)) != 0) {
                errorInterrupt(status);

                // log.debug(" error Interupt "+status);
            }
            if (--bogusCount < 0) {
                setReg16(REG_INTR_STATUS, 0xffff);

                break;
            }
        }
    }

    private void errorInterrupt(int status) {
        // Tally the missed packets and zero the counter
        setReg32(REG_RX_MISSED, 0);

        if ((status & (INTR_RX_UNDERRUN | INTR_RX_BUF_OVRFLO | INTR_RX_FIFO_OVRFLO | INTR_RX_ERR)) != 0) {
            // rxErrors++;
        }
        if ((status & INTR_RX_FIFO_OVRFLO) != 0) {
            // rxFifoOverflow++;
        }
        if ((status & INTR_RX_UNDERRUN) != 0) {
            // rxUnderRun++;
        }
        if ((status & INTR_RX_BUF_OVRFLO) != 0) {
            // rxBufferOverflow++;
            // rxBufferOverflow++;
            // following is needed to clear this interrupt
            // rxIndex = getReg16(REG_RX_BUF_CNT) % RX_BUF_SIZE;

            /*
             * rxRing.setIndex(getReg16(REG_RX_BUF_CNT) % RX_BUF_SIZE);
             * setReg16(REG_RX_BUF_PTR, rxRing.getIndex() - 16);
             */

        }
        if ((status & INTR_SYS_ERR) != 0) {
            // ???
        }
        if ((status & INTR_TIMEOUT) != 0) {
            // timeoutError++;
        }
    }

    private void rxProcess(int status) {
        // Read all packets
        setReg32(REG_CFG9346, CFG9346_WE);

        while ((getReg8(REG_CHIPCMD) & CMD_BUFFER_EMPTY) == 0) {
            final int pktStatus = 0; // rxRing.getPktStatus();
            final int pktLen = (pktStatus >> 16);

            if (pktLen == 0xfff0) {
                break;
            }

            if ((pktStatus & (RX_ISE | RX_RUNT | RX_LONG | RX_CRC | RX_FAE)) != 0) {
                setReg8(REG_CHIPCMD, CMD_TX_ENABLE);

                // set up rx mode/configuration
                setReg32(REG_RX_CONFIG, rxConfig);
                // rxRing.setIndex(getReg16(CBR));
                setReg16(CAPR, CBR);
                enableTxRx();
                setReg32(REG_RX_CONFIG, rxConfig);

                // Enable interrupts
                setReg16(REG_INTR_MASK, INTR_MASK);
                return;
            } else {
                final SocketBuffer skbuf = null; // rxRing.getPacket(pktLen);

                try {
                    if (skbuf.getSize() > 0)
                        driver.onReceive(skbuf);
                } catch (NetworkException e) {
                    e.printStackTrace(); // To change body of catch statement
                                            // use Options | File
                    // Templates.
                } finally {
                    // FIXME
                }
            }
            // setReg16(CAPR, rxRing.getIndex() - 16);
        }
        setReg32(REG_CFG9346, CFG9346_NORMAL);
    }

    public void txProcess(int status) {
        while (txNumberOfPackets > 0) {
            int txStatus = getReg32(REG_TX_STATUS0 + (txPending * 4));
            // Make sure something has been transmitted
            if ((txStatus & (TX_TUN | TX_TOK | TX_TABT)) == 0) {
                return;
            }
            if ((txStatus & (TX_OWC | TX_TABT)) != 0) {
                // txErrors++;
                if ((txStatus & TX_TABT) != 0) {
                    txAborted++;
                    // Setting clear abort bit will make the 8139
                    // retransmit the aborted packet
                    setReg32(REG_TX_CONFIG, txConfig | TCR_CLRABT);
                    return;
                }
                if ((txStatus & TX_CRS) != 0) {
                    // txCarrierErrors++;
                }
                if ((txStatus & TX_OWC) != 0) {
                    // System.out.println("TX window error");
                    // txWindowErrors++;
                }
            } else {
                if ((txStatus & TX_TUN) != 0) {
                    // txFifoErrors++;
                }
                // txCollisions += ((txStatus & TX_NCC) >> 24);
                // txBytes += txStatus & 0x7ff;
                // txPackets++;
            }
            // txPendingQueue[txPending].free();
            txNumberOfPackets--;
            txPending++;
            txPending &= 3;
        }
    }

    /**
     * Gets the first IO-Address used by the given device
     * 
     * @param device
     * @param flags
     */
    protected int getIOBase(Device device, Flags flags) throws DriverException {
        final PCIHeaderType0 config = ((PCIDevice) device).getConfig().asHeaderType0();
        final PCIBaseAddress[] addrs = config.getBaseAddresses();

        if (addrs.length < 1) {
            throw new DriverException("Cannot find iobase: not base addresses");
        }
        if (!addrs[0].isIOSpace()) {
            throw new DriverException("Cannot find iobase: first address is not I/O");
        }
        return addrs[0].getIOBase();
    }

    /**
     * Gets the number of IO-Addresses used by the given device
     * 
     * @param device
     * @param flags
     */
    protected int getIOLength(Device device, Flags flags) throws DriverException {
        final PCIHeaderType0 config = ((PCIDevice) device).getConfig().asHeaderType0();
        final PCIBaseAddress[] addrs = config.getBaseAddresses();

        if (addrs.length < 1) {
            throw new DriverException("Cannot find iobase: not base addresses");
        }

        if (!addrs[0].isIOSpace()) {
            throw new DriverException("Cannot find iobase: first address is not I/O");
        }

        return addrs[0].getSize();
    }

    /**
     * Gets the IRQ used by the given device
     * 
     * @param device
     * @param flags
     */
    protected int getIRQ(Device device, Flags flags) throws DriverException {
        final PCIHeaderType0 config = ((PCIDevice) device).getConfig().asHeaderType0();
        return config.getInterruptLine();
    }

    /**
     * Reads a 8-bit NIC register
     * 
     * @param reg
     */
    protected final int getReg8(int reg) {
        return io.inPortByte(iobase + reg);
    }

    /**
     * Reads a 16-bit NIC register
     * 
     * @param reg
     */
    protected final int getReg16(int reg) {
        return io.inPortWord(iobase + reg);
    }

    /**
     * Reads a 32-bit NIC register
     * 
     * @param reg
     */
    protected final int getReg32(int reg) {
        return io.inPortDword(iobase + reg);
    }

    /**
     * Writes a 8-bit NIC register
     * 
     * @param reg
     * @param value
     */
    protected final void setReg8(int reg, int value) {
        io.outPortByte(iobase + reg, value);
    }

    /**
     * Writes a 16-bit NIC register
     * 
     * @param reg
     * @param value
     */
    protected final void setReg16(int reg, int value) {
        io.outPortWord(iobase + reg, value);
    }

    /**
     * Writes a 32-bit NIC register
     * 
     * @param reg
     * @param value
     */
    public final void setReg32(int reg, int value) {
        io.outPortDword(iobase + reg, value);
    }

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
}
