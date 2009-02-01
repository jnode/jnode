/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.driver.net.ne2000;

import java.security.PrivilegedExceptionAction;
import java.util.Arrays;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.ne2000.pci.Ne2000PCIDriver;
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
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public abstract class Ne2000Core extends AbstractDeviceCore implements IRQHandler, Ne2000Constants,
        EthernetConstants {

    private static final int TX_PAGES = 6;

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(Ne2000Core.class);
    /**
     * Start of IO address space
     */
    protected final int iobase;
    /**
     * IO address space
     */
    protected final IOResource io;
    /**
     * IRQ
     */
    protected final IRQResource irq;
    /**
     * My ethernet address
     */
    private final EthernetAddress hwAddress;
    /**
     * My device flags
     */
    protected final Ne2000Flags flags;
    /**
     * Start page of NIC memory
     */
    protected final int nic_start;
    /**
     * Start page of transmit buffer
     */
    protected final int tx_start;
    /**
     * Start page of receive buffer
     */
    protected final int rx_start;
    /**
     * End page of receive buffer (exclusive)
     */
    protected final int rx_end;
    /**
     * Is a transmit action in progress?
     */
    private boolean tx_active;
    private int rx_frame_errors;
    private int rx_crc_errors;
    private int rx_missed_errors;
    private int rx_overruns;
    /**
     * The driver we will deliver the receive packets to
     */
    private final Ne2000PCIDriver driver;

    /**
     * Create a new instance
     *
     * @param owner
     * @param device
     * @param flags
     */
    public Ne2000Core(Ne2000PCIDriver driver, ResourceOwner owner, Device device, Ne2000Flags flags)
        throws ResourceNotFreeException, DriverException {
        final int irq = getIRQ(device, flags);
        this.driver = driver;
        this.flags = flags;
        this.tx_active = false;

        // Get the start of the IO address space
        iobase = getIOBase(device, flags);
        final int iolength = getIOLength(device, flags);
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

        // Reset the device
        reset();

        // Load the hw address, todo this we must first do some initialization,
        // otherwise the hw address cannot be read.
        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STP); // Select page 0
        setReg(NE_P0_DCR, 0x49); // Set word-wide access
        setReg(NE_P0_RBCR0, 0x00); // Clear count regs
        setReg(NE_P0_RBCR1, 0x00);
        setReg(NE_P0_IMR, 0x00); // Mask completion irq
        setReg(NE_P0_ISR, 0xff);
        setReg(NE_P0_RCR, NE_RXOFF);
        setReg(NE_P0_TCR, NE_TXOFF);

        // Load the start page
        this.nic_start = probeNicMemoryStart();
        this.tx_start = nic_start;
        this.rx_start = tx_start + TX_PAGES;
        this.rx_end = nic_start + (flags.getMemSize() / NE_PAGESIZE);

        final byte[] saprom = new byte[32];
        getNicData(0, saprom, 0, 32);

        if (flags.is16bit()) {
            this.hwAddress =
                    new EthernetAddress(saprom[0], saprom[2], saprom[4], saprom[6], saprom[8],
                            saprom[10]);
        } else {
            this.hwAddress = new EthernetAddress(saprom, 0);
        }

        log.debug("Found " + flags.getName() + " IRQ=" + irq + ", IOBase=0x" +
                NumberUtils.hex(iobase) + ", MAC Address=" + hwAddress);
    }

    /**
     * Gets the first IO-Address used by the given device
     *
     * @param device
     * @param flags
     */
    protected abstract int getIOBase(Device device, Ne2000Flags flags) throws DriverException;

    /**
     * Gets the number of IO-Addresses used by the given device
     *
     * @param device
     * @param flags
     */
    protected abstract int getIOLength(Device device, Ne2000Flags flags) throws DriverException;

    /**
     * Gets the IRQ used by the given device
     *
     * @param device
     * @param flags
     */
    protected abstract int getIRQ(Device device, Ne2000Flags flags) throws DriverException;

    /**
     * Initialize the device
     */
    public synchronized void initialize() {
        // Reset the device
        reset();

        // Load the hw address, todo this we must first do some initialization,
        // otherwise the hw address cannot be read.
        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STP); // Select page 0
        setReg(NE_P0_DCR, 0x49); // Set word-wide access
        setReg(NE_P0_RBCR0, 0x00); // Clear count regs
        setReg(NE_P0_RBCR1, 0x00);
        setReg(NE_P0_ISR, 0xff); // Clear all interrupt flags
        setReg(NE_P0_IMR, NE_ISRCONFIG);

        // Setup buffer ring
        setReg(NE_P0_PSTART, rx_start);
        setReg(NE_P0_PSTOP, rx_end);
        setReg(NE_P0_BOUND, rx_end - 1);
        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS1 | NE_CR_STP); // Select page 1
        setReg(NE_P1_CURR, rx_start);

        // Setup PAR0-5, MAR0-7
        for (int i = 0; i < ETH_ALEN; i++) {
            setReg(NE_P1_PAR0 + i, hwAddress.get(i));
        }
        for (int i = 0; i < ETH_ALEN; i++) {
            setReg(NE_P1_MAR0 + i, 0xff);
        }

        // Start the receive/transmit mode
        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STA); // Select page 0
        setReg(NE_P0_RCR, NE_RXCONFIG);
        setReg(NE_P0_TCR, NE_TXCONFIG);

        log.debug("Start receiving..rx_start=" + rx_start + ", rx_end=" + rx_end + ", tx_start=" +
                tx_start);
    }

    /**
     * Disable the device
     */
    public synchronized void disable() {
        // Start the receive/transmit mode
        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STP); // Select page 0
        setReg(NE_P0_RCR, NE_RXOFF);
        setReg(NE_P0_TCR, NE_TXOFF);
    }

    /**
     * Release all resources
     */
    public synchronized void release() {
        io.release();
        irq.release();
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

        // Wait until we can start transmitting
        while (tx_active) {
            wait(timeout);
        }
        tx_active = true;
        //log.debug("Going for transmit:\n" + NumberUtils.hex(buf.getBuffer(), buf.getBufferOffset(), buf.getSize()));

        final int length = buf.getSize();
        setNicData(buf, 0, (tx_start << 8), length);
        // Program the transmit
        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STA);
        setReg(NE_P0_TPSR, tx_start);
        setReg(NE_P0_TBCR0, length & 0xFF);
        setReg(NE_P0_TBCR1, (length >> 8) & 0xFF);
        setReg(NE_P0_CR, NE_CR_TXP | NE_CR_NODMA | NE_CR_PS0 | NE_CR_STA);
    }

    /**
     * @see org.jnode.system.IRQHandler#handleInterrupt(int)
     */
    public synchronized void handleInterrupt(int irq) {

        // Get the interrupt status
        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STA);

        int isr;
        int loops = 0;
        while (((isr = getReg(NE_P0_ISR)) != 0) && (loops < NE_MAX_ISR_LOOPS)) {
            loops++;
            // Receive
            if ((isr & NE_ISR_OVW) != 0) {
                processOverrunInterrupt();
                // Overrun interrupt is acknowledged in processOverrunInterrupt
            } else if ((isr & (NE_ISR_PRX | NE_ISR_RXE)) != 0) {
                processReceiveInterrupt();
                setReg(NE_P0_ISR, NE_ISR_PRX | NE_ISR_RXE);
            }

            // Transmit
            if ((isr & NE_ISR_TXE) != 0) {
                processTransmitErrorInterrupt();
                setReg(NE_P0_ISR, NE_ISR_PTX | NE_ISR_TXE);
            } else if ((isr & NE_ISR_PTX) != 0) {
                processTransmitInterrupt();
                setReg(NE_P0_ISR, NE_ISR_PTX | NE_ISR_TXE);
            }

            // Counters
            if ((isr & NE_ISR_CNT) != 0) {
                processCounterInterrupt();
                setReg(NE_P0_ISR, NE_ISR_CNT);
            }

            // Remote DMA completed, ignore
            if ((isr & NE_ISR_RDC) != 0) {
                setReg(NE_P0_ISR, NE_ISR_RDC);
            }

            // Reset start position
            setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STA);
        }

        if (isr != 0) {
            setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STA);
            if (loops >= NE_MAX_ISR_LOOPS) {
                log.error("Too much work in interrupt handler of " + flags.getName() + ", isr=0x" +
                        NumberUtils.hex(isr, 2));
                setReg(NE_P0_ISR, NE_ISRCONFIG);
            } else {
                log.error("Unknown interrupt of " + flags.getName() + ", isr=0x" +
                        NumberUtils.hex(isr, 2));
                setReg(NE_P0_ISR, 0xFF);
            }
        }
    }

    /**
     * Process an interrupt caused by an almost overrun of the NIC counters.
     */
    private void processCounterInterrupt() {
        rx_frame_errors += getReg(NE_P0_CNTR0);
        rx_crc_errors += getReg(NE_P0_CNTR1);
        rx_missed_errors += getReg(NE_P0_CNTR2);
    }

    /**
     * Process an interrupt caused by an overrun in the receive buffer
     */
    private void processOverrunInterrupt() {
        log.debug("Receive buffer overrun on " + flags.getName());

        // Was a transmit active?
        final boolean wasInTx = ((getReg(NE_P0_CR) & NE_CR_TXP) != 0);

        // Stop all activity
        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STP);
        rx_overruns++;

        // Wait a full Tx time (1.2ms) + some guard time, NS says 1.6ms total
        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            // Ignore
        }

        // Reset RBCR[01] back to zero as per magic incantation.
        setReg(NE_P0_RBCR0, 0);
        setReg(NE_P0_RBCR1, 0);

        // See if any Tx was interrupted or not. According to NS, this
        // step is vital, and skipping it will cause no end of havoc.
        final boolean mustResend;
        if (wasInTx) {
            final int isr = getReg(NE_P0_ISR);
            final boolean txCompleted = ((isr & (NE_ISR_TXE | NE_ISR_PTX)) != 0);
            mustResend = !txCompleted;
        } else {
            mustResend = false;
        }

        // Have to enter loopback mode and then restart the NIC before
        // you are allowed to slurp packets up off the ring.
        setReg(NE_P0_TCR, NE_TXOFF);
        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STA);

        // Clear the Rx ring of all the debris, and ack the interrupt.
        processReceiveInterrupt();
        setReg(NE_P0_ISR, NE_ISR_OVW);

        // Leave loopback mode, and resend any packet that got stopped
        setReg(NE_P0_TCR, NE_TXCONFIG);
        if (mustResend) {
            setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STA | NE_CR_TXP);
        }
    }

    /**
     * Process an interrupt caused by a received frame with no errors.
     * Page 0 is assumed!
     */
    private void processReceiveInterrupt() {
        //log.debug("Receive on " + flags.getName());

        // Get receive status
        boolean rc;
        do {
            rc = readPacket();
        } while (rc);
    }

    /**
     * Read a single packet from the receive buffer
     *
     * @return true on succesfull packet read, false otherwise
     */
    private boolean readPacket() {

        final int rsr = getReg(NE_P0_RSR);
        if ((rsr & (NE_RSR_PRX | NE_RSR_MPA)) == 0) {
            log.debug("No valid packet, rsr=0x" + NumberUtils.hex(rsr, 2));
            // Not valid packet
            return false;
        }

        // Where to start reading?
        int next = getReg(NE_P0_BOUND) + 1;
        if (next >= rx_end) {
            next = rx_start;
        }
        setReg(NE_P0_CR, NE_CR_PS1);
        int curr = getReg(NE_P1_CURR);
        setReg(NE_P0_CR, NE_CR_PS0);
        if (curr == next) {
            //log.debug("No valid packet, curr==next, rsr=0x" + NumberUtils.hex(rsr, 2) + ", 
            //curr=0x" + NumberUtils.hex(curr, 2));
            return false;
        }

        // Get the packet header
        final Ne2000PacketHeader hdr = getHeader(next);
        //log.debug("curr=0x" + NumberUtils.hex(curr, 2) + ", next=0x" + NumberUtils.hex(next, 2) + ",
        //hdr=" + hdr);
        final int len = hdr.getLength();
        final byte[] bbuf = new byte[len + 1]; // +1, to allow 16-bit transfer

        // Where should we start reading
        final int nicAddr = (next << 8) + 4;
        // First page also contains 4-byte header

        if ((nicAddr + len) > (rx_end << 8)) {
            // Packet is wrapper over the end of the receive buffer
            // Get first part
            final int len1 = (rx_end << 8) - nicAddr;
            getNicData(nicAddr, bbuf, 0, len1);
            // Get second part
            final int len2 = len - len1;
            final int nicAddr2 = (rx_start << 8);
            getNicData(nicAddr2, bbuf, len1, len2);
        } else {
            // We can get the buffer in a single action
            getNicData(nicAddr, bbuf, 0, len);
        }
        final SocketBuffer buf = new SocketBuffer(bbuf, 0, len);

        // Calculate the next bound value
        final int nextBound = hdr.getNextPacketPage() - 1;
        // Set the next bound value
        if (nextBound < rx_start) {
            setReg(NE_P0_BOUND, rx_end - 1);
        } else {
            setReg(NE_P0_BOUND, nextBound);
        }

        //log.debug("curr=" + curr + ", next=" + next + ", nextBound=" + nextBound + ", length=" + len + ",
        //hdr.next=" + hdr.getNextPacketPage());

        // Process the packet
        try {
            driver.onReceive(buf);
        } catch (NetworkException ex) {
            log.error("Error in onReceive", ex);
        }
        //log.debug("Received packet length:" + buf.getSize() + ", src:" + srcAddr + ", dst:" + dstAddr + ", 
        //data:\n" + NumberUtils.hex(buf.getBuffer(), buf.getBufferOffset(), buf.getSize()));
        return true;
    }

    /**
     * Process an interrupt caused by a transmitted frame with no errors.
     */
    private void processTransmitInterrupt() {
        tx_active = false;
        notifyAll();
        //log.debug("Transmit success on " + flags.getName());
    }

    /**
     * Process an interrupt caused by an error in transmitting a frame
     */
    private void processTransmitErrorInterrupt() {
        tx_active = false;
        notifyAll();
        log.debug("Transmit error on " + flags.getName());
    }

    /**
     * Reset the device
     */
    private void reset() {
        // Trigger reset
        io.outPortByte(iobase + NE_RESET, io.inPortByte(iobase + NE_RESET));
        while ((io.inPortByte(iobase + NE_P0_ISR) & NE_ISR_RST) == 0) {
            Thread.yield();
        }
        // Reset Interrupt flags
        io.outPortByte(iobase + NE_P0_ISR, 0xff);
    }

    /**
     * Read data from the NIC
     *
     * @param nicSrcAddress
     * @param dst
     * @param length
     */
    private void getNicData(int nicSrcAddress, byte[] dst, int dstOffset, int length) {

        if (flags.is16bit()) {
            length = (length + 1) & ~1;
        }

        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STA);
        setReg(NE_P0_RSAR0, nicSrcAddress & 0xFF);
        setReg(NE_P0_RSAR1, (nicSrcAddress >> 8) & 0xFF);
        setReg(NE_P0_RBCR0, length & 0xFF);
        setReg(NE_P0_RBCR1, (length >> 8) & 0xFF);
        setReg(NE_P0_CR, NE_CR_RREAD | NE_CR_PS0 | NE_CR_STA);

        if (flags.is16bit()) {
            for (int i = 0; i < length; i += 2) {
                final int v = io.inPortWord(iobase + NE_DATA);
                dst[dstOffset + i + 0] = (byte) (v & 0xFF);
                dst[dstOffset + i + 1] = (byte) ((v >> 8) & 0xFF);
            }
        } else {
            for (int i = 0; i < length; i += 2) {
                final int v = io.inPortByte(iobase + NE_DATA);
                dst[dstOffset + i] = (byte) v;
            }
        }
        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STA);
    }

    /**
     * Read data from the NIC
     *
     * @param skbuf
     * @param skbufOffset
     * @param nicDstAddress
     * @param length
     */
    protected void setNicData(SocketBuffer skbuf, int skbufOffset, int nicDstAddress, int length) {

        final int origLength = length;
        if (flags.is16bit()) {
            length = (length + 1) & ~1;
        }

        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STA);
        setReg(NE_P0_RSAR0, nicDstAddress & 0xFF);
        setReg(NE_P0_RSAR1, (nicDstAddress >> 8) & 0xFF);
        setReg(NE_P0_RBCR0, length & 0xFF);
        setReg(NE_P0_RBCR1, (length >> 8) & 0xFF);
        setReg(NE_P0_CR, NE_CR_RWRITE | NE_CR_PS0 | NE_CR_STA);

        if (flags.is16bit()) {
            int i;
            for (i = 0; (i + 1) < origLength; i += 2) {
                //final int v0 = src[srcOffset + i + 0] & 0xFF;
                //final int v1 = src[srcOffset + i + 1] & 0xFF;
                final int v0 = skbuf.get(skbufOffset + i + 0);
                final int v1 = skbuf.get(skbufOffset + i + 1);
                io.outPortWord(iobase + NE_DATA, (v1 << 8) | v0);
            }
            if (i < origLength) {
                // The last byte
                final int v0 = skbuf.get(skbufOffset + i + 0);
                io.outPortWord(iobase + NE_DATA, v0);
            }
        } else {
            for (int i = 0; i < length; i++) {
                io.outPortByte(iobase + NE_DATA, skbuf.get(skbufOffset + i));
            }
        }
        setReg(NE_P0_CR, NE_CR_NODMA | NE_CR_PS0 | NE_CR_STA);
    }

    /**
     * Read a packet header starting at a given page
     *
     * @param page
     */
    private Ne2000PacketHeader getHeader(int page) {
        final byte[] buf = new byte[4];
        getNicData(page << 8, buf, 0, buf.length);
        return new Ne2000PacketHeader(buf, 0);
    }

    /**
     * Gets the value of a register
     *
     * @param reg
     */
    private int getReg(int reg) {
        return io.inPortByte(iobase + reg);
    }

    /**
     * Gets the value of a register
     *
     * @param reg
     * @param value
     */
    private void setReg(int reg, int value) {
        io.outPortByte(iobase + reg, value);
    }

    /**
     * Gets the ethernet address of this device.
     */
    public final HardwareAddress getHwAddress() {
        return hwAddress;
    }

    /**
     * Figure out the start page of the NIC's memory and the size of this memory
     */
    private int probeNicMemoryStart() throws DriverException {
        final SocketBuffer testBuf = new SocketBuffer();
        final byte[] testData = new byte[] {
            (byte) 0x23, (byte) 0x34, (byte) 0x56, (byte) 0xf3, (byte) 0x72,
            (byte) 0xa6, (byte) 0xe2, (byte) 0xa1, (byte) 0x23, (byte) 0x34, (byte) 0x56,
            (byte) 0xf3, (byte) 0x72, (byte) 0xa6, (byte) 0xe2, (byte) 0xa1};
        final byte[] returnData = new byte[testData.length];
        testBuf.append(testData, 0, testData.length);

        for (int page = 0; page < 256; page += 16) {
            setNicData(testBuf, 0, (page << 8), testData.length);
            getNicData((page << 8), returnData, 0, testData.length);

            if (Arrays.equals(testData, returnData)) {
                //log.debug("Found start page " + page);
                return page;
            }

            //log.debug("Got (on page " + page + "): " + NumberUtils.hex(returnData, 0, returnData.length));

        }
        throw new DriverException("Cannot find NIC memory of " + flags.getName());
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
