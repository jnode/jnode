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

package org.jnode.driver.net.prism2;

import static org.jnode.driver.net.prism2.Prism2Constants.Command.ACCESS;
import static org.jnode.driver.net.prism2.Prism2Constants.Command.ALLOC;
import static org.jnode.driver.net.prism2.Prism2Constants.Command.DISABLE;
import static org.jnode.driver.net.prism2.Prism2Constants.Command.ENABLE;
import static org.jnode.driver.net.prism2.Prism2Constants.Command.INIT;
import static org.jnode.driver.net.prism2.Prism2Constants.Command.TX;
import static org.jnode.driver.net.prism2.Prism2Constants.LinkStatus.CONNECTED;
import static org.jnode.driver.net.prism2.Prism2Constants.LinkStatus.NOTCONNECTED;
import static org.jnode.driver.net.prism2.Prism2Constants.RecordID.CNFAUTHENTICATION;
import static org.jnode.driver.net.prism2.Prism2Constants.RecordID.CNFMAXDATALEN;
import static org.jnode.driver.net.prism2.Prism2Constants.RecordID.CNFOWNMACADDR;
import static org.jnode.driver.net.prism2.Prism2Constants.RecordID.CNFPORTTYPE;
import static org.jnode.driver.net.prism2.Prism2Constants.RecordID.CURRENTBSSID;
import static org.jnode.driver.net.prism2.Prism2Constants.RecordID.CURRENTSSID;
import static org.jnode.driver.net.prism2.Prism2Constants.RecordID.TXRATECNTL;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.ALLOCFID;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.EVACK;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.EVSTAT;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.INFOFID;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.INTEN;
import static org.jnode.driver.net.prism2.Prism2Constants.Register.RXFID;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIBaseAddress;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIHeaderType0;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.event.LinkStatusEvent;
import org.jnode.driver.net.wireless.spi.WirelessDeviceCore;
import org.jnode.naming.InitialNaming;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.wireless.AuthenticationMode;
import org.jnode.net.wireless.WirelessConstants;
import org.jnode.system.IRQHandler;
import org.jnode.system.IRQResource;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.LittleEndian;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.MagicUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Prism2Core extends WirelessDeviceCore implements Prism2Constants,
    WirelessConstants, IRQHandler {

    /**
     * The driver I'm a part of
     */
    private final Prism2Driver driver;

    /**
     * The device flags
     */
    private final Prism2Flags flags;

    /**
     * My ethernet address
     */
    private EthernetAddress hwAddress = null;

    /**
     * Low level I/O helper
     */
    private Prism2IO io;

    /**
     * The IRQ resource
     */
    private IRQResource irq;

    /**
     * Info frame used in the irq handler. To avoid numerous allocations.
     */
    private final byte[] irqInfoFrame = new byte[Prism2InfoFrame.MAX_FRAME_LEN];

    /**
     * Receive frame used in the irq handler. To avoid numerous allocations.
     */
    private final byte[] irqReceiveFrame = new byte[Prism2CommFrame.MAX_FRAME_LEN];

    /**
     * Current link status
     */
    private LinkStatus linkStatus = NOTCONNECTED;

    /**
     * Address of connected BSS (only valid when link status is connected
     */
    private EthernetAddress bssid = null;

    /**
     * The device
     */
    private final Device device;

    /**
     * Initialize this instance.
     *
     * @param driver
     * @param owner
     * @param device
     * @param flags
     * @throws DriverException
     */
    public Prism2Core(Prism2Driver driver, ResourceOwner owner,
                      PCIDevice device, Flags flags) throws DriverException {
        if (!(flags instanceof Prism2Flags))
            throw new DriverException("Wrong flags to the Prism2 driver");

        this.driver = driver;
        this.device = device;
        this.flags = (Prism2Flags) flags;

        final ResourceManager rm;
        try {
            rm = InitialNaming.lookup(ResourceManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find ResourceManager");
        }

        final PCIHeaderType0 pciCfg = device.getConfig().asHeaderType0();
        final PCIBaseAddress[] baseAddrs = pciCfg.getBaseAddresses();
        if (baseAddrs.length < 1) {
            throw new DriverException(
                "No memory mapped I/O region in PCI config");
        }
        final PCIBaseAddress regsAddr = pciCfg.getBaseAddresses()[0];
        if (!regsAddr.isMemorySpace()) {
            throw new DriverException("Memory mapped I/O is not a memory space");
        }

        // Claim the memory mapped I/O region
        final Address regsAddrPtr = Address.fromLong(regsAddr.getMemoryBase());
        final Extent regsSize = Extent.fromIntZeroExtend(regsAddr.getSize());
        try {
            final MemoryResource regs;
            regs = rm.claimMemoryResource(device, regsAddrPtr, regsSize,
                ResourceManager.MEMMODE_NORMAL);
            this.io = new Prism2IO(regs);
        } catch (ResourceNotFreeException ex) {
            throw new DriverException("Cannot claim memory mapped I/O", ex);
        }

        // Claim IRQ
        final int irqNo = pciCfg.getInterruptLine();
        try {
            this.irq = rm.claimIRQ(device, irqNo, this, true);
        } catch (ResourceNotFreeException ex) {
            // Release IO
            io.release();
            io = null;
            // re-throw exception
            throw new DriverException("Cannot claim IRQ", ex);
        }

        log.info("Found " + flags.getName() + ", regs at "
            + MagicUtils.toString(regsAddrPtr));
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractDeviceCore#disable()
     */
    public void disable() {
        // Disable all interrupts
        io.setReg(INTEN, 0);

        // Disable the mac port
        try {
            executeDisableCmd(0);
        } catch (DriverException ex) {
            log.debug("Disable failed", ex);
        }
    }

    /**
     * Execute the Access command
     *
     * @throws DriverException
     */
    private final void executeAccessCmd(RecordID rid, boolean write)
        throws DriverException {
        try {
            final Result result;
            final int cmdFlags;
            if (write) {
                cmdFlags = CMD_WRITE;
            } else {
                cmdFlags = 0;
            }
            result = io.executeCommand(ACCESS, cmdFlags, rid.getId(), 0, 0, null);
            io.resultToException(result);
        } catch (TimeoutException ex) {
            throw new DriverException("Timeout in Access command", ex);
        }
    }

    /**
     * Execute the Alloc command
     *
     * @throws DriverException
     */
    private final void executeAllocCmd(int bufferLength)
        throws DriverException {
        try {
            final Result result;
            result = io.executeCommand(ALLOC, 0, bufferLength, 0, 0, null);
            io.resultToException(result);
        } catch (TimeoutException ex) {
            throw new DriverException("Timeout in Alloc command", ex);
        }
    }

    /**
     * Enable the given mac port of the device.
     *
     * @param macPort
     * @throws DriverException
     */
    private final void executeEnableCmd(int macPort) throws DriverException {
        try {
            final int cmdFlags = ((macPort & 7) << 8);
            final Result result;
            result = io.executeCommand(ENABLE, cmdFlags, 0, 0, 0, null);
            io.resultToException(result);
        } catch (TimeoutException ex) {
            throw new DriverException("Timeout in Enable command", ex);
        }
    }

    /**
     * Disable the given mac port of the device.
     *
     * @param macPort
     * @throws DriverException
     */
    private final void executeDisableCmd(int macPort) throws DriverException {
        try {
            final int cmdFlags = ((macPort & 7) << 8);
            final Result result;
            result = io.executeCommand(DISABLE, cmdFlags, 0, 0, 0, null);
            io.resultToException(result);
        } catch (TimeoutException ex) {
            throw new DriverException("Timeout in Disable command", ex);
        }
    }

    /**
     * Execute the Transmit command
     *
     * @throws DriverException
     */
    private final void executeTransmitCmd(int fid)
        throws DriverException {
        try {
            final Result result;
            result = io.executeCommand(TX, 0, fid, 0, 0, null);
            io.resultToException(result);
        } catch (TimeoutException ex) {
            throw new DriverException("Timeout in Tx command", ex);
        }
    }

    /**
     * Read a configuration record
     *
     * @param rid
     * @param dst
     * @param dstOffset
     * @param len
     * @throws DriverException
     */
    private final void getConfig(RecordID rid, byte[] dst, int dstOffset, int len)
        throws DriverException {
        // Request read of RID
        executeAccessCmd(rid, false);

        // Read record header
        final byte[] hdr = new byte[Prism2Record.HDR_LENGTH];
        io.copyFromBAP(rid.getId(), 0, hdr, 0, hdr.length);

        // Validate the record length
        if ((Prism2Record.getRecordLength(hdr, 0) - 1) * 2 != len) {
            throw new DriverException("Mismatch in record length. " + len + "/"
                + Prism2Record.getRecordLength(hdr, 0));
        }

        // Copy out record data
        io.copyFromBAP(rid.getId(), hdr.length, dst, dstOffset, len);
    }

    /**
     * Get a 16-bit configuration value.
     *
     * @param rid
     * @return The 16-bit value.
     * @throws DriverException
     */
    private final int getConfig16(RecordID rid) throws DriverException {
        final byte[] arr = new byte[2];
        getConfig(rid, arr, 0, 2);
        return LittleEndian.getInt16(arr, 0);
    }

    /**
     * Get a 32-bit configuration value.
     *
     * @param rid
     * @return The 32-bit value.
     * @throws DriverException
     */
    private final int getConfig32(RecordID rid) throws DriverException {
        final byte[] arr = new byte[4];
        getConfig(rid, arr, 0, 4);
        return LittleEndian.getInt32(arr, 0);
    }

    /**
     * Read the current ESSID
     *
     * @throws DriverException
     * @see org.jnode.driver.net.wireless.spi.WirelessDeviceCore#getESSID()
     */
    protected String getESSID() throws DriverException {
        final byte[] id = new byte[RID_CURRENTSSID_LEN];
        getConfig(CURRENTSSID, id, 0, id.length);
        return null;
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractDeviceCore#getHwAddress()
     */
    public HardwareAddress getHwAddress() {
        return hwAddress;
    }

    /**
     * Initialize the device so that it is ready to transmit and receive data.
     *
     * @see org.jnode.driver.net.spi.AbstractDeviceCore#initialize()
     */
    public void initialize() throws DriverException {

        // Initialize card
        try {
            io.executeCommand(INIT, 0, 0, 0, 0, null);
        } catch (TimeoutException ex) {
            throw new DriverException("Cannot initialize device in time", ex);
        }

        // Disable interrupts
        io.setReg(INTEN, 0);
        // Acknowledge any spurious events
        io.setReg(EVACK, 0xFFFF);

        // Read MAC address
        final byte[] macAddr = new byte[CNFOWNMACADDR.getRecordLength()];
        getConfig(CNFOWNMACADDR, macAddr, 0, CNFOWNMACADDR.getRecordLength());
        this.hwAddress = new EthernetAddress(macAddr, 0);
        log.info("MAC-address for " + flags.getName() + " " + hwAddress);

        // Set maximum data length
        setConfig16(CNFMAXDATALEN, WLAN_DATA_MAXLEN);
        // Set transmit rate control
        setConfig16(TXRATECNTL, 0x000f);
        // Set authentication to Open system
        setConfig16(CNFAUTHENTICATION, CNFAUTHENTICATION_OPENSYSTEM);

        // Maybe set desired ESSID here

        // Set port type to ESS port
        setConfig16(CNFPORTTYPE, 1);

        // Enable Rx & Info interrupts
        io.setReg(INTEN, INTEN_RX | INTEN_INFO);

        // Enable card
        executeEnableCmd(0);
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractDeviceCore#release()
     */
    public void release() {
        final Prism2IO io = this.io;
        if (io != null) {
            this.io = null;
            io.release();
        }

        final IRQResource irq = this.irq;
        if (irq != null) {
            this.irq = null;
            irq.release();
        }
    }

    /**
     * Write a configuration record
     *
     * @param rid
     * @param src
     * @param srcOffset
     * @param len
     * @throws DriverException
     */
    private final void setConfig(RecordID rid, byte[] src, int srcOffset, int len)
        throws DriverException {
        // Create and write record header
        final byte[] hdr = new byte[Prism2Record.HDR_LENGTH];
        Prism2Record.setRecordLength(hdr, 0, (len / 2) + 1);
        Prism2Record.setRecordRID(hdr, 0, rid.getId());
        io.copyToBAP(rid.getId(), 0, hdr, 0, hdr.length);

        // Copy out record data (if any)
        if (len > 0) {
            io.copyToBAP(rid.getId(), hdr.length, src, srcOffset, len);
        }

        // Request write of RID
        executeAccessCmd(rid, true);
    }

    /**
     * Set a 16-bit configuration value.
     *
     * @param rid
     * @param value
     * @throws DriverException
     */
    private final void setConfig16(RecordID rid, int value) throws DriverException {
        final byte[] arr = new byte[2];
        LittleEndian.setInt16(arr, 0, value);
        setConfig(rid, arr, 0, 2);
    }

    /**
     * Set a 32-bit configuration value.
     *
     * @param rid
     * @param value
     * @throws DriverException
     */
    private final void setConfig32(RecordID rid, int value) throws DriverException {
        final byte[] arr = new byte[4];
        LittleEndian.setInt32(arr, 0, value);
        setConfig(rid, arr, 0, 4);
    }

    /**
     * @see org.jnode.driver.net.wireless.spi.WirelessDeviceCore#setESSID(java.lang.String)
     */
    protected void setESSID(String essid) throws DriverException {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.jnode.driver.net.wireless.spi.WirelessDeviceCore#startScan()
     */
    public void startScan() {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.jnode.driver.net.spi.AbstractDeviceCore#transmit(SocketBuffer buf, 
     * HardwareAddress destination, long timeout)
     */
    public void transmit(SocketBuffer buf, HardwareAddress destination, long timeout)
        throws InterruptedException, TimeoutException {

        // Request buffer allocation
        try {
            executeAllocCmd(Prism2CommFrame.MAX_TXBUF_LEN);
        } catch (DriverException ex) {
            log.debug("Alloc command failed in transmit", ex);
            return;
        }

        // Wait for the allocation to be ready
        final int evstat = io.waitForEvent(EVSTAT_ALLOC, EVACK_INFO, 10, 50);
        if ((evstat & EVSTAT_ALLOC) == 0) {
            log.debug("Allocation of transmit buffer failed");
            return;
        }
        final int fid = io.getReg(ALLOCFID);

        // Build the transmit header
        final int hdrLen = Prism2CommFrame.HDR_LENGTH;
        final byte[] hdr = new byte[hdrLen];

//        txdesc.tx_control = host2hfa384x_16( HFA384x_TX_MACPORT_SET(0) | HFA384x_TX_STRUCTYPE_SET(1) | 
//                   HFA384x_TX_TXEX_SET(1) | HFA384x_TX_TXOK_SET(1) );
//txdesc.frame_control =  host2ieee16( WLAN_SET_FC_FTYPE(WLAN_FTYPE_DATA) |
//                   WLAN_SET_FC_FSTYPE(WLAN_FSTYPE_DATAONLY) |
//                   WLAN_SET_FC_TODS(1) );
        Prism2CommFrame.setAddress1(hdr, 0, bssid);
        Prism2CommFrame.setAddress2(hdr, 0, hwAddress);
        Prism2CommFrame.setAddress3(hdr, 0, (EthernetAddress) destination);

        try {
            // Copy tx-descriptor to BAP
            io.copyToBAP(fid, 0, hdr, 0, hdrLen);
            // Copy 802.11 header to BAP

            // Copy data to BAP


        } catch (DriverException ex) {
            log.debug("Failed to copy data to BAP", ex);
            return;
        }

        // Execute Tx command
        try {
            executeTransmitCmd(fid);
        } catch (DriverException ex) {
            log.debug("Transmit command failed", ex);
            return;
        }

        // TODO Implement error handling
    }

    /**
     * @see org.jnode.system.IRQHandler#handleInterrupt(int)
     */
    public final void handleInterrupt(int irq) {
        for (int loop = 0; loop < 10; loop++) {
            final int evstat = io.getReg(EVSTAT);
            if (evstat == 0) {
                // No event for me
                return;
            }

            try {
                if ((evstat & EVSTAT_RX) != 0) {
                    // Received a frame
                    processReceiveEvent();
                } else if ((evstat & EVSTAT_INFO) != 0) {
                    // Get Info frame
                    processInfoEvent();
                } else {
                    log.debug("No suitable event 0x"
                        + NumberUtils.hex(evstat, 4));
                    return;
                }
            } catch (DriverException ex) {
                log.error("Error in IRQ handler", ex);
            }
        }
    }

    /**
     * A frame has been received.
     *
     * @throws DriverException
     */
    private final void processReceiveEvent() throws DriverException {
        // Read the FID of the received frame
        final int fid = io.getReg(RXFID);
        final byte[] frame = this.irqReceiveFrame;
        log.info("Receive, FID=0x" + NumberUtils.hex(fid, 4));

        // Read the frame header
        final int hdrLen = Prism2CommFrame.HDR_LENGTH;
        io.copyFromBAP(fid, 0, frame, 0, hdrLen);
        final int dataLength = Prism2CommFrame.getDataLength(frame, 0);

        // Create the SocketBuffer 
        final int ethHLEN = EthernetConstants.ETH_HLEN;
        final SocketBuffer skbuf = new SocketBuffer(ethHLEN + dataLength);
        skbuf.append(frame, Prism2CommFrame.p8023HDR_OFF, ethHLEN);

        // Read the actual data
        if (dataLength > 0) {
            io.copyFromBAP(fid, hdrLen, frame, hdrLen, dataLength);
            skbuf.append(frame, hdrLen, dataLength);
        }

        // Process the received frame
        try {
            driver.onReceive(skbuf);
        } catch (NetworkException ex) {
            log.debug("Error in onReceive", ex);
        }

        // Acknowledge the receive
        io.setReg(EVACK, EVACK_RX);
    }

    /**
     * An Info frame is ready
     *
     * @throws DriverException
     */
    private final void processInfoEvent() throws DriverException {
        // Read the FID of the info frame
        final int fid = io.getReg(INFOFID);
        final byte[] frame = this.irqInfoFrame;
        log.debug("Info, FID=0x" + NumberUtils.hex(fid, 4));

        // Read the frame header
        final int hdrLen = Prism2InfoFrame.HDR_LENGTH;
        io.copyFromBAP(fid, 0, frame, 0, hdrLen);
        final int frameLen = Prism2InfoFrame.getFrameLength(frame, 0);
        final InformationType infoType = Prism2InfoFrame.getInfoType(frame, 0);

        // Read the rest of the frame
        io.copyFromBAP(fid, hdrLen, frame, hdrLen, frameLen - hdrLen);

        switch (infoType) {
            case COMMTALLIES:
                // Communication statistics. Ignore for now
                break;
            case LINKSTATUS:
                final LinkStatus lstat = Prism2InfoFrame.getLinkStatus(frame, 0);
                switch (lstat) {
                    case CONNECTED:
                        getConfig(CURRENTBSSID, frame, 0, WLAN_BSSID_LEN);
                        bssid = new EthernetAddress(frame, 0);
                        log.info("Connected to " + bssid);
                        break;
                    case DISCONNECTED:
                        log.info("Disconnected");
                        bssid = null;
                        break;
                    default:
                        log.info("Link status 0x" + NumberUtils.hex(lstat.getValue(), 4));
                }
                this.linkStatus = lstat;
                // Post the event
                driver.postEvent(new LinkStatusEvent(device, lstat == CONNECTED));
                break;
            default:
                log.info("Got Info frame, Type=0x" + NumberUtils.hex(infoType.getValue(), 4));
        }

        // Acknowledge the info frame
        io.setReg(EVACK, EVACK_INFO);
    }

    /**
     * @see org.jnode.driver.net.wireless.spi.WirelessDeviceCore#getAuthenticationMode()
     */
    protected AuthenticationMode getAuthenticationMode() throws DriverException {
        final int mode = getConfig16(CNFAUTHENTICATION);
        switch (mode) {
            case CNFAUTHENTICATION_OPENSYSTEM:
                return AuthenticationMode.OPENSYSTEM;
            case CNFAUTHENTICATION_SHAREDKEY:
                return AuthenticationMode.SHAREDKEY;
            default:
                throw new DriverException("Unknown authentication mode 0x"
                    + NumberUtils.hex(mode, 4));
        }
    }

    /**
     * @see org.jnode.driver.net.wireless.spi.WirelessDeviceCore#setAuthenticationMode(
     * org.jnode.net.wireless.AuthenticationMode)
     */
    protected void setAuthenticationMode(AuthenticationMode mode)
        throws DriverException {
        final int modeVal;
        switch (mode) {
            case OPENSYSTEM:
                modeVal = CNFAUTHENTICATION_OPENSYSTEM;
                break;
            case SHAREDKEY:
                modeVal = CNFAUTHENTICATION_SHAREDKEY;
                break;
            default:
                throw new DriverException("Unknown authentication mode " + mode);
        }
        setConfig16(CNFAUTHENTICATION, modeVal);
    }
}
