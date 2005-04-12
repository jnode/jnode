/*
 * $Id$
 */
package org.jnode.driver.net.prism2;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.event.LinkStatusEvent;
import org.jnode.driver.net.wireless.spi.WirelessDeviceCore;
import org.jnode.driver.pci.PCIBaseAddress;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.pci.PCIDeviceConfig;
import org.jnode.naming.InitialNaming;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
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

    /** The driver I'm a part of */
    private final Prism2Driver driver;

    /** The device flags */
    private final Prism2Flags flags;

    /** My ethernet address */
    private EthernetAddress hwAddress = null;

    /** Low level I/O helper */
    private Prism2IO io;

    /** The IRQ resource */
    private IRQResource irq;

    /** Info frame used in the irq handler. To avoid numerous allocations. */
    private final byte[] irqInfoFrame = new byte[Prism2InfoFrame.MAX_FRAME_LEN];

    /** Current link status */
    private int linkStatus = LINK_NOTCONNECTED;
    
    /** Address of connected BSS (only valid when link status is connected */
    private EthernetAddress bssid = null;
    
    /** The device */
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
            rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find ResourceManager");
        }

        final PCIDeviceConfig pciCfg = device.getConfig();
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
        io.setReg(REG_INTEN, 0);

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
    private final void executeAccessCmd(int rid, boolean write)
            throws DriverException {
        try {
            final int result;
            final int cmd;
            if (write) {
                cmd = CMDCODE_ACCESS | CMD_WRITE;
            } else {
                cmd = CMDCODE_ACCESS;
            }
            result = io.executeCommand(cmd, rid, 0, 0, null);
            io.resultToException(result);
        } catch (TimeoutException ex) {
            throw new DriverException("Timeout in Access command", ex);
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
            final int cmd = CMDCODE_ENABLE | ((macPort & 7) << 8);
            final int result;
            result = io.executeCommand(cmd, 0, 0, 0, null);
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
            final int cmd = CMDCODE_DISABLE | ((macPort & 7) << 8);
            final int result;
            result = io.executeCommand(cmd, 0, 0, 0, null);
            io.resultToException(result);
        } catch (TimeoutException ex) {
            throw new DriverException("Timeout in Disable command", ex);
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
    private final void getConfig(int rid, byte[] dst, int dstOffset, int len)
            throws DriverException {
        // Request read of RID
        executeAccessCmd(rid, false);

        // Read record header
        final byte[] hdr = new byte[Prism2Record.HDR_LENGTH];
        io.copyFromBAP(rid, 0, hdr, 0, hdr.length);

        // Validate the record length
        if ((Prism2Record.getRecordLength(hdr, 0) - 1) * 2 != len) {
            throw new DriverException("Mismatch in record length. " + len + "/"
                    + Prism2Record.getRecordLength(hdr, 0));
        }

        // Copy out record data
        io.copyFromBAP(rid, hdr.length, dst, dstOffset, len);
    }

    /**
     * Get a 16-bit configuration value.
     * 
     * @param rid
     * @return The 16-bit value.
     * @throws DriverException
     */
    private final int getConfig16(int rid) throws DriverException {
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
    private final int getConfig32(int rid) throws DriverException {
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
        getConfig(RID_CURRENTSSID, id, 0, id.length);
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
            io.executeCommand(CMDCODE_INIT, 0, 0, 0, null);
        } catch (TimeoutException ex) {
            throw new DriverException("Cannot initialize device in time", ex);
        }

        // Disable interrupts
        io.setReg(REG_INTEN, 0);
        // Acknowledge any spurious events
        io.setReg(REG_EVACK, 0xFFFF);

        // Read MAC address
        final byte[] macAddr = new byte[RID_CNFOWNMACADDR_LEN];
        getConfig(RID_CNFOWNMACADDR, macAddr, 0, RID_CNFOWNMACADDR_LEN);
        this.hwAddress = new EthernetAddress(macAddr, 0);
        log.info("MAC-address for " + flags.getName() + " " + hwAddress);

        // Set maximum data length
        setConfig16(RID_CNFMAXDATALEN, WLAN_DATA_MAXLEN);
        // Set transmit rate control
        setConfig16(RID_TXRATECNTL, 0x000f);
        // Set authentication to Open system
        setConfig16(RID_CNFAUTHENTICATION, CNFAUTHENTICATION_OPENSYSTEM);

        // Maybe set desired ESSID here

        // Set port type to ESS port
        setConfig16(RID_CNFPORTTYPE, 1);

        // Enable Rx & Info interrupts
        io.setReg(REG_INTEN, INTEN_RX | INTEN_INFO);

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
    private final void setConfig(int rid, byte[] src, int srcOffset, int len)
            throws DriverException {
        // Create and write record header
        final byte[] hdr = new byte[Prism2Record.HDR_LENGTH];
        Prism2Record.setRecordLength(hdr, 0, (len / 2) + 1);
        Prism2Record.setRecordRID(hdr, 0, rid);
        io.copyToBAP(rid, 0, hdr, 0, hdr.length);

        // Copy out record data (if any)
        if (len > 0) {
            io.copyToBAP(rid, hdr.length, src, srcOffset, len);
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
    private final void setConfig16(int rid, int value) throws DriverException {
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
    private final void setConfig32(int rid, int value) throws DriverException {
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
     * @see org.jnode.driver.net.spi.AbstractDeviceCore#transmit(org.jnode.net.SocketBuffer,
     *      long)
     */
    public void transmit(SocketBuffer buf, long timeout)
            throws InterruptedException, TimeoutException {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.jnode.system.IRQHandler#handleInterrupt(int)
     */
    public final void handleInterrupt(int irq) {
        for (int loop = 0; loop < 10; loop++) {
            final int evstat = io.getReg(REG_EVSTAT);
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
     */
    private final void processReceiveEvent() {
        // Read the FID of the received frame
        final int fid = io.getReg(REG_RXFID);
        log.info("Receive, FID=0x" + NumberUtils.hex(fid, 4));

        // Read the FID into my buffer.
        // TODO

        // Acknowledge the receive
        io.setReg(REG_EVACK, EVACK_RX);
    }

    /**
     * An Info frame is ready
     * 
     * @throws DriverException
     */
    private final void processInfoEvent() throws DriverException {
        // Read the FID of the info frame
        final int fid = io.getReg(REG_INFOFID);
        final byte[] frame = this.irqInfoFrame;
        log.debug("Info, FID=0x" + NumberUtils.hex(fid, 4));

        // Read the frame header
        final int hdrLen = Prism2InfoFrame.HDR_LENGTH;
        io.copyFromBAP(fid, 0, frame, 0, hdrLen);
        final int frameLen = Prism2InfoFrame.getFrameLength(frame, 0);
        final int infoType = Prism2InfoFrame.getInfoType(frame, 0);

        // Read the rest of the frame
        io.copyFromBAP(fid, hdrLen, frame, hdrLen, frameLen - hdrLen);

        switch (infoType) {
        case IT_COMMTALLIES:
            // Communication statistics. Ignore for now
            break;
        case IT_LINKSTATUS:
            final int lstat = Prism2InfoFrame.getLinkStatus(frame, 0);
            if (lstat == LINK_CONNECTED) {
                getConfig(RID_CURRENTBSSID, frame, 0, WLAN_BSSID_LEN);
                bssid = new EthernetAddress(frame, 0);
                log.info("Connected to " + bssid);
            } else if (lstat == LINK_DISCONNECTED) {
                log.info("Disconnected");
                bssid = null;
            } else {
                log.info("Link status 0x" + NumberUtils.hex(lstat, 4));                
            }
            this.linkStatus = lstat;
            // Post the event
            driver.postEvent(new LinkStatusEvent(device, lstat == LINK_CONNECTED));
            break;
        default:
            log.info("Got Info frame, Type=0x" + NumberUtils.hex(infoType, 4));
        }

        // Acknowledge the info frame
        io.setReg(REG_EVACK, EVACK_INFO);
    }

    /**
     * @see org.jnode.driver.net.wireless.spi.WirelessDeviceCore#getAuthenticationMode()
     */
    protected AuthenticationMode getAuthenticationMode() throws DriverException {
        final int mode = getConfig16(RID_CNFAUTHENTICATION);
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
     * @see org.jnode.driver.net.wireless.spi.WirelessDeviceCore#setAuthenticationMode(org.jnode.net.wireless.AuthenticationMode)
     */
    protected void setAuthenticationMode(AuthenticationMode mode)
            throws DriverException {
        final int modeVal;
        if (mode == AuthenticationMode.OPENSYSTEM) {
            modeVal = CNFAUTHENTICATION_OPENSYSTEM;
        } else if (mode == AuthenticationMode.SHAREDKEY) {
            modeVal = CNFAUTHENTICATION_SHAREDKEY;
        } else {
            throw new DriverException("Unknown authentication mode " + mode);
        }
        setConfig16(RID_CNFAUTHENTICATION, modeVal);
    }
}
