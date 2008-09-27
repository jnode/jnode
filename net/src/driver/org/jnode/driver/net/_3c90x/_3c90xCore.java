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

package org.jnode.driver.net._3c90x;

import java.util.ArrayList;
import java.util.Collection;

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
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public class _3c90xCore extends AbstractDeviceCore implements _3c90xConstants, IRQHandler, EthernetConstants {

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
    private EthernetAddress hwAddress;
    /**
     * The driver i'm a part of
     */
    private final _3c90xDriver driver;
    /**
     * My flags
     */
    private final _3c90xFlags flags;
    /**
     * Is a transmission active?
     */
    private boolean tx_active;
    /**
     * Active register window
     */
    private int reg_window = 0xff;
    /**
     * The receive buffer ring
     */
    private final _3c90xRxRing rxRing;
    /**
     * The transmit buffer
     */
    private final _3c90xTxBuffer txBuffer;
    /**
     * Is the device a B revision
     */
    private final boolean Brev;

    /**
     * Create a new instance
     *
     * @param flags
     */
    public _3c90xCore(_3c90xDriver driver, ResourceOwner owner, PCIDevice device, _3c90xFlags flags)
        throws DriverException, ResourceNotFreeException {
        final int irq = getIRQ(device, flags);
        this.driver = driver;
        this.flags = flags;
        this.tx_active = false;

        // Get the start of the IO address space
        this.iobase = getIOBase(device, flags);
        final int iolength = getIOLength(device, flags);
        final ResourceManager rm;
        try {
            rm = InitialNaming.lookup(ResourceManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find ResourceManager");
        }
        this.irq = rm.claimIRQ(owner, irq, this, true);
        try {
            io = rm.claimIOResource(owner, iobase, iolength);
        } catch (ResourceNotFreeException ex) {
            this.irq.release();
            throw ex;
        }
        this.rxRing = new _3c90xRxRing(RX_FRAMES, rm);
        this.txBuffer = new _3c90xTxBuffer(rm);

        // Reset the device
        reset();

        // Determine Brev flag
        switch (readEEProm(0x03)) {
            case 0x9000:/** 10 Base TPO             **/
            case 0x9001:/** 10/100 T4               **/
            case 0x9050:/** 10/100 TPO              **/
            case 0x9051:/** 10 Base Combo           **/
                //case 0x9200: /** 3Com905C-TXM            **/
                Brev = false;
                break;

            case 0x9004:/** 10 Base TPO             **/
            case 0x9005:/** 10 Base Combo           **/
            case 0x9006:/** 10 Base TPO and Base2   **/
            case 0x900A:/** 10 Base FL              **/
            case 0x9055:/** 10/100 TPO              **/
            case 0x9056:/** 10/100 T4               **/
            case 0x905A:/** 10 Base FX              **/
            default:
                Brev = true;
                break;
        }

        // Read the eeprom
        final int[] eeprom = new int[0x21];
        for (int i = 0; i < 0x17; i++) {
            eeprom[i] = readEEProm(i);
            //Syslog.debug("eeprom[" + NumberUtils.hex(i, 2) + "]=" + NumberUtils.hex(eeprom[i], 4));
        }
        final byte[] hwAddrArr = new byte[ETH_ALEN];
        hwAddrArr[0] = (byte) (eeprom[0x0a] >> 8);
        hwAddrArr[1] = (byte) (eeprom[0x0a] & 0xFF);
        hwAddrArr[2] = (byte) (eeprom[0x0b] >> 8);
        hwAddrArr[3] = (byte) (eeprom[0x0b] & 0xFF);
        hwAddrArr[4] = (byte) (eeprom[0x0c] >> 8);
        hwAddrArr[5] = (byte) (eeprom[0x0c] & 0xFF);
        this.hwAddress = new EthernetAddress(hwAddrArr, 0);

        log.debug("Found " + flags.getName() + " IRQ=" + irq + ", IOBase=0x" +
                NumberUtils.hex(iobase) + ", MAC Address=" + hwAddress);
    }

    /**
     * Gets the ethernet address of this device
     */
    public HardwareAddress getHwAddress() {
        return hwAddress;
    }

    /**
     * Initialize the device
     */
    public synchronized void initialize() {
        // First reset the device
        reset();

        // Now initialize our buffers
        rxRing.initialize();

        /** Program the MAC address into the station address registers **/
        setWindow(winAddressing2);
        final int a0 = ((hwAddress.get(1) & 0xFF) << 8) | (hwAddress.get(0) & 0xFF);
        final int a1 = ((hwAddress.get(3) & 0xFF) << 8) | (hwAddress.get(2) & 0xFF);
        final int a2 = ((hwAddress.get(5) & 0xFF) << 8) | (hwAddress.get(4) & 0xFF);
        setReg16(regStationAddress_2_3w + 0, a0);
        setReg16(regStationAddress_2_3w + 2, a1);
        setReg16(regStationAddress_2_3w + 4, a2);
        setReg16(regStationMask_2_3w + 0, 0);
        setReg16(regStationMask_2_3w + 2, 0);
        setReg16(regStationMask_2_3w + 4, 0);

        // Determine the link type
        final ArrayList<String> connectors = new ArrayList<String>();
        final int linktype = determineLinkType(connectors);
        log.debug("Found connectors " + connectors);

        /** enable DC converter for 10-Base-T **/
        if (linktype == 0x0003) {
            issueCommand(cmdEnableDcConverter, 0, 0);
        }

        /** Set the link to the type we just determined. **/
        setWindow(winTxRxOptions3);
        int cfg = getReg32(regInternalConfig_3_l);
        cfg &= ~(0xF << 20);
        cfg |= (linktype << 20);
        setReg32(regInternalConfig_3_l, cfg);
        //log.debug("Setting linktype to 0x" + NumberUtils.hex(linktype));

        /** Now that we set the xcvr type, reset the Tx and Rx, re-enable. **/
        issueCommand(cmdTxReset, 0x00, 10);
        while ((getReg16(regCommandIntStatus_w) & INT_CMDINPROGRESS) != 0) {
            /* loop */
        }

        if (!Brev) {
            setReg8(regTxFreeThresh_b, 0x01);
        }

        issueCommand(cmdTxEnable, 0, 0);

        /**
         ** reset of the receiver on B-revision cards re-negotiates the link
         ** takes several seconds (a computer eternity)
         **/
        if (Brev) {
            issueCommand(cmdRxReset, 0x04, 10);
        } else {
            issueCommand(cmdRxReset, 0x00, 10);
        }
        while ((getReg16(regCommandIntStatus_w) & INT_CMDINPROGRESS) != 0) {
            /* loop */
        }

        /** Set the RX filter = receive only individual pkts & bcast. **/
        issueCommand(cmdSetRxFilter, 0x01 + 0x04, 0);
        //issueCommand(cmdSetRxFilter, 0x1F, 0);
        issueCommand(cmdRxEnable, 0, 0);
        setReg32(regUpListPtr_l, rxRing.getFirstUPDAddress().toInt());

        /**
         ** set Indication and Interrupt flags , acknowledge any IRQ's
         **/
        final int intMask =
                INT_HOSTERROR | INT_TXCOMPLETE | INT_RXCOMPLETE | INT_UPDATESTATS | INT_LINKEVENT |
                        INT_UPCOMPLETE | INT_INTREQUESTED;
        issueCommand(cmdSetInterruptEnable, /*0x7FF*/intMask, 0);
        issueCommand(cmdSetIndicationEnable, 0x7FF, 0);
        issueCommand(cmdAcknowledgeInterrupt, 0x661, 0);

        log.debug("initialize done");
    }

    /**
     * Disable the device
     */
    public synchronized void disable() {
        reset();
    }

    /**
     * Release all resources
     */
    public void release() {
        io.release();
        log.debug("irq.release");
        irq.release();
        log.debug("end of release");
    }

    /**
     * Transmit the given buffer
     *
     * @param buf
     * @param timeout
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public synchronized void transmit(SocketBuffer buf, HardwareAddress destination, long timeout)
        throws InterruptedException, TimeoutException {
        // Set the source address
        hwAddress.writeTo(buf, 6);

        //final int txStatus = getReg8(regTxStatus_b);
        //log.debug("Waiting for transmit txStatus=0x" + NumberUtils.hex(txStatus, 2));

        // Wait until we can start transmitting
        final long start = System.currentTimeMillis();
        while (tx_active) {
            final long now = System.currentTimeMillis();
            if (now - start > timeout) {
                throw new TimeoutException("Timeout in claiming transmitter");
            }
            wait(timeout);
        }
        tx_active = true;
        //log.debug("Going for transmit");

        txBuffer.initialize(buf);

        // Stall the download engine
        issueCommand(cmdStallCtl, 2, 1);

        // Set the address of the txBuffer
        setReg32(regDnListPtr_l, txBuffer.getFirstDPDAddress().toInt());

        // UnStall the download engine
        issueCommand(cmdStallCtl, 3, 1);
        //log.debug("Leaving transmit txStatus=0x" + NumberUtils.hex(getReg8(regTxStatus_b), 2));
    }

    /**
     * @see org.jnode.system.IRQHandler#handleInterrupt(int)
     */
    public synchronized void handleInterrupt(int irq) {

        int intStatus = getReg16(regCommandIntStatus_w);
        int loops = 0;
        while ((intStatus & ~INT_WINDOWNUMBER) != 0) {
            //log.debug("IntStatus flags on " + flags.getName() + ": 0x" + NumberUtils.hex(intStatus, 4));
            loops++;
            if (loops > MAX_SERVICE) {
                log.error("Too much work in intterupt, IntStatus=0x" + NumberUtils.hex(intStatus));
                //issueCommand(cmdAcknowledgeInterrupt, intStatus & ~INT_WINDOWNUMBER, 0);
                return;
            }
            if ((intStatus & INT_TXCOMPLETE) != 0) {
                //log.debug("TxComplete on " + flags.getName());
                processTxComplete();
            } else if ((intStatus & INT_UPDATESTATS) != 0) {
                log.debug("UpdateStats on " + flags.getName());
                processUpdateStats();
            } else if ((intStatus & INT_LINKEVENT) != 0) {
                //log.debug("LinkEvent on " + flags.getName());
                processLinkEvent();
            } else if ((intStatus & INT_UPCOMPLETE) != 0) {
                //log.debug("UpComplete on " + flags.getName());
                processUpComplete();
            } else if ((intStatus & INT_INTERRUPTLATCH) != 0) {
                issueCommand(cmdAcknowledgeInterrupt, INT_INTERRUPTLATCH, 0);
            } else {
                log.debug("Unknown IntStatus flags set on " + flags.getName() + ": IntStatus=0x" +
                        NumberUtils.hex(intStatus, 4));
                issueCommand(cmdAcknowledgeInterrupt, intStatus & ~INT_WINDOWNUMBER, 0);
            }
            intStatus = getReg16(regCommandIntStatus_w);
        }

        //issueCommand(cmdAcknowledgeInterrupt, INT_INTERRUPTLATCH, 0);

        //log.debug("Done IRQ on " + flags.getName() + ": 0x" + NumberUtils.hex(intStatus, 4));
    }

    /**
     * Process a TxComplete interrupt
     */
    private final void processTxComplete() {
        tx_active = false;
        notifyAll();
        setReg8(regTxStatus_b, 0xFF); // Ack TxComplete, by writing any value
    }

    /**
     * Process an UpdateStats interrupt
     */
    private final void processUpdateStats() {
        // TODO Implement update stats
    }

    /**
     * Process a LinkEvent interrupt
     */
    private final void processLinkEvent() {
        // Read IntStatusAuto ack. the interrupt
        //getReg16(regIntStatusAuto_w);
        issueCommand(cmdAcknowledgeInterrupt, 0x02, 0);
        //issueCommand(cmdRxEnable, 0, 0);
    }

    /**
     * Process an UpComplete interrupt
     */
    private final void processUpComplete() {
        // Read all packets
        final int nrFrames = rxRing.getNrFrames();

        // Stall uploading first
        issueCommand(cmdStallCtl, 0x00, 1);

        for (int i = 0; i < nrFrames; i++) {
            final int pktStatus = rxRing.getPktStatus(i);
            if (pktStatus != 0) {
                //log.debug("PktStatus[" + NumberUtils.hex(i, 2) + "]=0x" + NumberUtils.hex(pktStatus));
                if ((pktStatus & upComplete) != 0) {
                    final SocketBuffer skbuf = rxRing.getPacket(i);
                    try {
                        //log.debug("Read packet at index 0x" + NumberUtils.hex(i));
                        driver.onReceive(skbuf);
                    } catch (NetworkException ex) {
                        log.debug("Error in onReceive", ex);
                    } finally {
                        rxRing.setPktStatus(i, 0);
                    }
                }
            }
        }

        // UnStall uploading
        issueCommand(cmdStallCtl, 0x01, 0);

        // Ack interrupt
        issueCommand(cmdAcknowledgeInterrupt, INT_UPCOMPLETE, 0);
    }

    /**
     * Reset this device
     */
    private void reset() {
        issueCommand(cmdGlobalReset, 0xff, 10);

        /** global reset command resets station mask, non-B revision cards
         ** require explicit reset of values
         **/
        setWindow(winAddressing2);
        setReg16(regStationAddress_2_3w + 0, 0);
        setReg16(regStationAddress_2_3w + 2, 0);
        setReg16(regStationAddress_2_3w + 4, 0);

        /** Issue transmit reset, wait for command completion **/
        issueCommand(cmdTxReset, 0, 10);
        issueCommand(cmdRxReset, 0, 10);
        issueCommand(cmdSetInterruptEnable, 0, 0);
        /** enable rxComplete and txComplete **/
        issueCommand(cmdSetIndicationEnable, 0x0014, 0);
        /** acknowledge any pending status flags **/
        issueCommand(cmdAcknowledgeInterrupt, 0x661, 0);
    }

    /**
     * Gets the first IO-Address used by the given device
     *
     * @param device
     * @param flags
     */
    protected int getIOBase(Device device, _3c90xFlags flags) throws DriverException {
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
    protected int getIOLength(Device device, _3c90xFlags flags) throws DriverException {
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
    protected int getIRQ(Device device, _3c90xFlags flags) throws DriverException {
        final PCIHeaderType0 config = ((PCIDevice) device).getConfig().asHeaderType0();
        return config.getInterruptLine();
    }

    /**
     * Determine which connectors are physically available on the device.
     * Determine the link type based on that.
     *
     * @param connectors
     */
    private final int determineLinkType(Collection<String> connectors) {
        /** Read the media options register, print a message and set default
         ** xcvr.
         **
         ** Uses Media Option command on B revision, Reset Option on non-B
         ** revision cards -- same register address
         **/
        setWindow(winTxRxOptions3);
        int mopt = getReg16(regResetMediaOptions_3_w);

        /* mask out VCO bit that is defined as 10baseFL bit on B-rev cards */
        if (!Brev) {
            mopt &= 0x7F;
        }
        //log.debug("mopt=0x" + NumberUtils.hex(mopt));

        int linktype = 0x0008;
        if ((mopt & 0x01) != 0) {
            connectors.add("100Base-T4");
            linktype = 0x0006;
        }
        if ((mopt & 0x04) != 0) {
            connectors.add("100Base-FX");
            linktype = 0x0005;
        }
        if ((mopt & 0x10) != 0) {
            connectors.add("10Base-2");
            linktype = 0x0003;
        }
        if ((mopt & 0x20) != 0) {
            connectors.add("AUI");
            linktype = 0x0001;
        }
        if ((mopt & 0x40) != 0) {
            connectors.add("MII");
            linktype = 0x0006;
        }
        if ((mopt & 0xA) == 0xA) {
            connectors.add("10Base-T / 100Base-TX");
            linktype = 0x0008;
        } else if ((mopt & 0xA) == 0x2) {
            connectors.add("100Base-TX");
            linktype = 0x0008;
        } else if ((mopt & 0xA) == 0x8) {
            connectors.add("10Base-T");
            linktype = 0x0008;
        }

        return linktype;
    }

    /**
     * Execute a command on the NIC
     *
     * @param command
     * @param param
     */
    private final void issueCommand(int command, int param, long sleep) {
        // Calculate the complete command + param value
        final int v = (command << 11) | (param & 0x7FF);
        // Wait for the previous command to complete
        while ((getReg16(regCommandIntStatus_w) & INT_CMDINPROGRESS) != 0) {
            /* loop */
        }
        // Send the command
        setReg16(regCommandIntStatus_w, v);
        if (sleep > 0) {
            // Sleep for long commands
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ex) {
                // Ignore
            }
        }
        // Wait for the command to complete
        int loops = 0;
        while ((getReg16(regCommandIntStatus_w) & INT_CMDINPROGRESS) != 0) {
            /* loop */
            loops++;
        }
        //log.debug("Loops=" + loops + ", cmd=0x" + NumberUtils.hex(v));
    }

    /**
     * Read data from the serial eeprom
     *
     * @param address
     */
    private int readEEProm(int address) {
        /** Select correct window **/
        setWindow(winEepromBios0);

        /** Make sure the eeprom isn't busy **/
        while (((1 << 15) & getReg16(regEepromCommand_0_w)) != 0) {
            /* Loop */
        }

        /** Read the value. **/
        setReg16(regEepromCommand_0_w, address + ((0x02) << 6));
        try {
            Thread.sleep(2);
        } catch (InterruptedException ex) {
            // Ignore
        }
        while (((1 << 15) & getReg16(regEepromCommand_0_w)) != 0) {
            /* Loop */
        }

        return getReg16(regEepromData_0_w) & 0xFFFF;
    }

    /**
     * Sets the current register window
     *
     * @param w
     */
    private final void setWindow(int w) {
        if (reg_window != w) {
            issueCommand(cmdSelectRegisterWindow, w, 0);
            reg_window = w;
        }
    }

    /**
     * Reads a 8-bit NIC register
     * @param reg
     */
    /*private final int getReg8(int reg) {
         return io.inPortByte(iobase + reg);
     }*/

    /**
     * Reads a 16-bit NIC register
     *
     * @param reg
     */
    private final int getReg16(int reg) {
        return io.inPortWord(iobase + reg);
    }

    /**
     * Reads a 32-bit NIC register
     *
     * @param reg
     */
    private final int getReg32(int reg) {
        return io.inPortDword(iobase + reg);
    }

    /**
     * Writes a 8-bit NIC register
     *
     * @param reg
     * @param value
     */
    private final void setReg8(int reg, int value) {
        io.outPortByte(iobase + reg, value);
    }

    /**
     * Writes a 16-bit NIC register
     *
     * @param reg
     * @param value
     */
    private final void setReg16(int reg, int value) {
        io.outPortWord(iobase + reg, value);
    }

    /**
     * Writes a 32-bit NIC register
     *
     * @param reg
     * @param value
     */
    private final void setReg32(int reg, int value) {
        io.outPortDword(iobase + reg, value);
    }
}
