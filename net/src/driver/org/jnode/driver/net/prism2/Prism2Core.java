/*
 * $Id$
 */
package org.jnode.driver.net.prism2;

import javax.naming.NameNotFoundException;

import org.jnode.driver.DriverException;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.driver.pci.PCIBaseAddress;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.pci.PCIDeviceConfig;
import org.jnode.naming.InitialNaming;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.TimeoutException;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.MagicUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Prism2Core extends AbstractDeviceCore implements Prism2Constants {

    /** The driver I'm a part of */
    private final Prism2Driver driver;

    /** The device flags */
    private final Prism2Flags flags;

    /** Low level I/O helper */
    private Prism2IO io;

    /** My ethernet address */
    private EthernetAddress hwAddress = null;

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

        log.info("Found " + flags.getName() + ", regs at "
                + MagicUtils.toString(regsAddrPtr));
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractDeviceCore#disable()
     */
    public void disable() {
        // TODO Auto-generated method stub

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
}
