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

package org.jnode.driver.bus.pci;

import java.io.PrintWriter;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;
import javax.naming.NameNotFoundException;
import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceInfoAPI;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.naming.InitialNaming;
import org.jnode.system.IOResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.util.NumberUtils;
import org.jnode.vm.VirtualMemoryRegion;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.work.Work;
import org.jnode.work.WorkUtils;
import org.vmmagic.unboxed.Address;

/**
 * Driver for the PCI bus itself.
 *
 * @author epr
 */
final class PCIDriver extends Driver implements DeviceInfoAPI, PCIBusAPI, PCIConstants {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(PCIDriver.class);

    /**
     * IO space of the PCI configuration registers
     */
    private IOResource pciConfigIO;

    /**
     * All pci devices
     */
    private List<PCIDevice> devices;

    /**
     * Global lock used to protected access to the configuration space
     */
    private static final Object CONFIG_LOCK = new Object();

    private final PCIBus rootBus;

    /**
     * Create a new instance
     */
    protected PCIDriver(Device pciDevice) throws DriverException {
        this.rootBus = new PCIBus(pciDevice.getBus(), this);
    }

    /**
     * Register all PCI devices with the device manager.
     */
    public void startDevice() throws DriverException {
        try {
            final Device pciBusDevice = getDevice();
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            // Claim the resources
            pciConfigIO = claimPorts(rm, pciBusDevice);
            // Register the API's
            pciBusDevice.registerAPI(PCIBusAPI.class, this);
            pciBusDevice.registerAPI(DeviceInfoAPI.class, this);
            // Find the PCI devices
            devices = probeDevices();
            // Start the PCI devices
            WorkUtils.add(new Work("Starting PCI devices") {

                public void execute() {
                    startDevices(devices);
                }
            });
        } catch (ResourceNotFreeException ex) {
            throw new DriverException("Cannot claim IO ports", ex);
        } catch (DriverException ex) {
            throw new DriverException("Driver exception during register", ex);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find resource or device manager",
                ex);
        }
    }

    /**
     * Unregister all PCI devices from the device manager.
     */
    public void stopDevice() throws DriverException {
        // Stop & unregister all PCI devices
        DeviceManager devMan;
        try {
            devMan = InitialNaming.lookup(DeviceManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find device manager", ex);
        }
        for (PCIDevice dev : devices) {
            log.debug("Stopping and unregistering device " + dev.getId());
            try {
                devMan.unregister(dev);
            } catch (DriverException ex) {
                throw new DriverException("Driver exception during unregister",
                    ex);
            }
        }
        // Remove the API
        final Device pciBusDevice = getDevice();
        pciBusDevice.unregisterAPI(DeviceInfoAPI.class);
        pciBusDevice.unregisterAPI(PCIBusAPI.class);
        // Release the resources
        pciConfigIO.release();
        pciConfigIO = null;
    }

    /**
     * Return the list of connection PCI devices.
     *
     * @return A List containing all connected devices as instanceof PCIDevice.
     */
    public List getDevices() {
        return devices;
    }

    /**
     * Find a device with a given vendor and device id.
     *
     * @param vendorId
     * @param deviceId
     * @return The found device, of null if not found.
     */
    public PCIDevice findDevice(int vendorId, int deviceId) {
        for (PCIDevice dev : devices) {
            final PCIDeviceConfig cfg = dev.getConfig();
            if (cfg.getVendorID() == vendorId) {
                if (cfg.getDeviceID() == deviceId) {
                    return dev;
                }
            }
        }
        return null;
    }

    /**
     * Probe the PCI bus for a list of all connected devices.
     *
     * @return A List containing all connected devices as instanceof PCIDevice.
     */
    protected List<PCIDevice> probeDevices() {
        final ArrayList<PCIDevice> result = new ArrayList<PCIDevice>();
        rootBus.probeDevices(result);
        return result;
    }

    /**
     * Start all PCI devices. All bridges are started in a sequence. After that
     * all non-bridges are started in parallel (goal, not implemented yet).
     */
    final void startDevices(List<PCIDevice> devices) {
        // List all devices
        for (PCIDevice dev : devices) {
            final PCIDeviceConfig cfg = dev.getConfig();
            log.debug("PCI " + dev.getPCIName() + "\t"
                + NumberUtils.hex(cfg.getVendorID(), 4) + ":"
                + NumberUtils.hex(cfg.getDeviceID(), 4) + ":"
                + NumberUtils.hex(cfg.getRevision(), 2) + " "
                + NumberUtils.hex(cfg.getBaseClass(), 2) + ":"
                + NumberUtils.hex(cfg.getSubClass(), 2) + ":"
                + NumberUtils.hex(cfg.getMinorClass(), 2)
//                    + "\tIRQ" + cfg.getInterruptLine() + ":" + cfg.getInterruptPin()
                + "\tCMD " + NumberUtils.hex(cfg.getCommand(), 4));
        }

        // Remap all devices
        remapDeviceAddresses(devices);

        // Register all bridges
        final DeviceManager devMan = getDevice().getManager();
        for (PCIDevice dev : devices) {
            if (dev.isBridge()) {
                try {
                    devMan.register(dev);
                } catch (DeviceAlreadyRegisteredException ex) {
                    log.error("Cannot start " + dev.getId(), ex);
                } catch (DriverException ex) {
                    log.error("Cannot start " + dev.getId(), ex);
                }
            }
        }
        // Register all non-bridges
        for (final PCIDevice dev : devices) {
            if (!dev.isBridge()) {
                WorkUtils.add(new Work(dev.getId()) {
                    public void execute() {
                        try {
                            devMan.register(dev);
                        } catch (DeviceAlreadyRegisteredException ex) {
                            log.error("Cannot start " + dev.getId(), ex);
                        } catch (DriverException ex) {
                            log.error("Cannot start " + dev.getId(), ex);
                        }
                    }
                });
            }
        }
    }

    /**
     * Remap the addresses of the given pci devices.
     * Currently we only verify if the memory addresses are in the
     * DEVICE space.
     *
     * @param devices
     */
    protected void remapDeviceAddresses(List<PCIDevice> devices) {
        log.debug("Remapping pci devices");
        final VmArchitecture arch = Vm.getArch();
        final Address start = arch.getStart(VirtualMemoryRegion.DEVICE);
        final Address end = arch.getEnd(VirtualMemoryRegion.DEVICE);
        for (PCIDevice dev : devices) {
            final PCIDeviceConfig cfg = dev.getConfig();
            if (cfg.isHeaderType0()) {
                int addrIdx = 0;
                for (PCIBaseAddress addr : cfg.asHeaderType0().getBaseAddresses()) {
                    if (addr.isBelow1Mb()) {
                        // Ignore
                    } else if (addr.isMemorySpace()) {
                        final Address memStart = Address.fromLong(addr
                            .getMemoryBase());
                        final Address memEnd = memStart.add(addr.getSize());
                        if (memStart.LT(start) || memEnd.GE(end)) {
                            log.error("Base address[" + addrIdx + "] of " + dev
                                + " out of device space");
                        } else {
                            log.debug("Base address[" + addrIdx + "] of " + dev
                                + " in device space");
                        }
                    } else if (addr.isIOSpace()) {
                        // Ignore for now
                    }
                    addrIdx++;
                }
            }
        }
    }

    /**
     * Read an 8-bit int from the PCI configuration space of the given device.
     *
     * @param bus    0..255
     * @param unit   0..31
     * @param func   0..7
     * @param offset 0..255 (byte offset)
     */
    protected int readConfigByte(int bus, int unit, int func, int offset) {
        if ((bus < 0) || (bus > 255)) {
            throw new IllegalArgumentException(
                "Invalid bus value");
        }
        if ((unit < 0) || (unit > 31)) {
            throw new IllegalArgumentException(
                "Invalid unit value");
        }
        if ((func < 0) || (func > 7)) {
            throw new IllegalArgumentException(
                "Invalid func value");
        }
        if ((offset < 0) || (offset > 255)) {
            throw new IllegalArgumentException(
                "Invalid offset value");
        }
        int address = 0x80000000 | (bus << 16) | (unit << 11) | (func << 8)
            | (offset & ~3);
        synchronized (CONFIG_LOCK) {
            pciConfigIO.outPortDword(PW32_CONFIG_ADDRESS, address);
            return pciConfigIO.inPortByte(PRW32_CONFIG_DATA + (offset & 3)) & 0xFF;
        }
    }

    /**
     * Read a 32-bit int from the PCI configuration space of the given device.
     *
     * @param bus    0..255
     * @param unit   0..31
     * @param func   0..7
     * @param offset 0..255 (byte offset)
     */
    protected int readConfigDword(int bus, int unit, int func, int offset) {
        if ((bus < 0) || (bus > 255)) {
            throw new IllegalArgumentException(
                "Invalid bus value");
        }
        if ((unit < 0) || (unit > 31)) {
            throw new IllegalArgumentException(
                "Invalid unit value");
        }
        if ((func < 0) || (func > 7)) {
            throw new IllegalArgumentException(
                "Invalid func value");
        }
        if ((offset < 0) || (offset > 255)) {
            throw new IllegalArgumentException(
                "Invalid offset value");
        }
        int address = 0x80000000 | (bus << 16) | (unit << 11) | (func << 8)
            | offset;
        synchronized (CONFIG_LOCK) {
            pciConfigIO.outPortDword(PW32_CONFIG_ADDRESS, address);
            return pciConfigIO.inPortDword(PRW32_CONFIG_DATA);
        }
    }

    /**
     * Read a 16-bit int from the PCI configuration space of the given device.
     *
     * @param bus    0..255
     * @param unit   0..31
     * @param func   0..7
     * @param offset 0..255 (byte offset)
     */
    protected int readConfigWord(int bus, int unit, int func, int offset) {
        if ((bus < 0) || (bus > 255)) {
            throw new IllegalArgumentException(
                "Invalid bus value");
        }
        if ((unit < 0) || (unit > 31)) {
            throw new IllegalArgumentException(
                "Invalid unit value");
        }
        if ((func < 0) || (func > 7)) {
            throw new IllegalArgumentException(
                "Invalid func value");
        }
        if ((offset < 0) || (offset > 255)) {
            throw new IllegalArgumentException(
                "Invalid offset value");
        }
        int address = 0x80000000 | (bus << 16) | (unit << 11) | (func << 8)
            | (offset & ~3);
        synchronized (CONFIG_LOCK) {
            pciConfigIO.outPortDword(PW32_CONFIG_ADDRESS, address);
            return pciConfigIO.inPortWord(PRW32_CONFIG_DATA + (offset & 2));
        }
    }

    /**
     * Write a 32-bit int into the PCI configuration space of the given device.
     *
     * @param bus    0..255
     * @param unit   0..31
     * @param func   0..7
     * @param offset 0..255 (byte offset)
     * @param value
     */
    protected void writeConfigDword(int bus, int unit, int func, int offset,
                                    int value) {
        if ((bus < 0) || (bus > 255)) {
            throw new IllegalArgumentException(
                "Invalid bus value");
        }
        if ((unit < 0) || (unit > 31)) {
            throw new IllegalArgumentException(
                "Invalid unit value");
        }
        if ((func < 0) || (func > 7)) {
            throw new IllegalArgumentException(
                "Invalid func value");
        }
        if ((offset < 0) || (offset > 255)) {
            throw new IllegalArgumentException(
                "Invalid register value");
        }
        int address = 0x80000000 | (bus << 16) | (unit << 11) | (func << 8)
            | (offset & ~3);
        synchronized (CONFIG_LOCK) {
            pciConfigIO.outPortDword(PW32_CONFIG_ADDRESS, address);
            pciConfigIO.outPortDword(PRW32_CONFIG_DATA, value);
        }
    }

    /**
     * Write a 16-bit int into the PCI configuration space of the given device.
     *
     * @param bus    0..255
     * @param unit   0..31
     * @param func   0..7
     * @param offset 0..255 (byte offset)
     * @param value
     */
    protected void writeConfigWord(int bus, int unit, int func, int offset,
                                   int value) {
        if ((bus < 0) || (bus > 255)) {
            throw new IllegalArgumentException(
                "Invalid bus value");
        }
        if ((unit < 0) || (unit > 31)) {
            throw new IllegalArgumentException(
                "Invalid unit value");
        }
        if ((func < 0) || (func > 7)) {
            throw new IllegalArgumentException(
                "Invalid func value");
        }
        if ((offset < 0) || (offset > 255)) {
            throw new IllegalArgumentException(
                "Invalid register value");
        }
        int address = 0x80000000 | (bus << 16) | (unit << 11) | (func << 8)
            | (offset & ~3);
        synchronized (CONFIG_LOCK) {
            pciConfigIO.outPortDword(PW32_CONFIG_ADDRESS, address);
            pciConfigIO.outPortWord(PRW32_CONFIG_DATA + (offset & 2), value);
        }
    }

    /**
     * Write a 8-bit int into the PCI configuration space of the given device.
     *
     * @param bus    0..255
     * @param unit   0..31
     * @param func   0..7
     * @param offset 0..255 (byte offset)
     * @param value
     */
    protected void writeConfigByte(int bus, int unit, int func, int offset,
                                   int value) {
        if ((bus < 0) || (bus > 255)) {
            throw new IllegalArgumentException(
                "Invalid bus value");
        }
        if ((unit < 0) || (unit > 31)) {
            throw new IllegalArgumentException(
                "Invalid unit value");
        }
        if ((func < 0) || (func > 7)) {
            throw new IllegalArgumentException(
                "Invalid func value");
        }
        if ((offset < 0) || (offset > 255)) {
            throw new IllegalArgumentException(
                "Invalid register value");
        }
        int address = 0x80000000 | (bus << 16) | (unit << 11) | (func << 8)
            | (offset & ~3);
        synchronized (CONFIG_LOCK) {
            pciConfigIO.outPortDword(PW32_CONFIG_ADDRESS, address);
            pciConfigIO.outPortByte(PRW32_CONFIG_DATA + (offset & 3), value);
        }
    }

    private IOResource claimPorts(final ResourceManager rm,
                                  final ResourceOwner owner) throws ResourceNotFreeException,
        DriverException {
        try {
            return (IOResource) AccessControllerUtils
                .doPrivileged(new PrivilegedExceptionAction() {

                    public Object run() throws ResourceNotFreeException {
                        return rm.claimIOResource(owner, PCI_FIRST_PORT,
                            PCI_LAST_PORT - PCI_FIRST_PORT + 1);
                    }
                });
        } catch (ResourceNotFreeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DriverException("Unknown exception", ex);
        }

    }

    /**
     * @see org.jnode.driver.DeviceInfoAPI#showInfo(java.io.PrintWriter)
     */
    public void showInfo(PrintWriter out) {
        final ArrayList<PCIDevice> devices = new ArrayList<PCIDevice>(this.devices);
        // List all devices
        for (PCIDevice dev : devices) {
            final PCIDeviceConfig cfg = dev.getConfig();
            out.println("PCI " + dev.getPCIName() + "\t"
                + NumberUtils.hex(cfg.getVendorID(), 4) + ":"
                + NumberUtils.hex(cfg.getDeviceID(), 4) + ":"
                + NumberUtils.hex(cfg.getRevision(), 2) + " "
                + NumberUtils.hex(cfg.getBaseClass(), 2) + ":"
                + NumberUtils.hex(cfg.getSubClass(), 2) + ":"
                + NumberUtils.hex(cfg.getMinorClass(), 2)
//                    + "\tIRQ"
//                    + cfg.getInterruptLine() + ":" + cfg.getInterruptPin()
                + "\tCMD " + NumberUtils.hex(cfg.getCommand(), 4));
        }
    }
}
