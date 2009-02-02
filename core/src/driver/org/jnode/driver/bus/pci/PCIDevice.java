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
 
package org.jnode.driver.bus.pci;

import java.io.PrintWriter;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceInfoAPI;

/**
 * @author epr
 */
public class PCIDevice extends Device implements DeviceInfoAPI {

    /**
     * The PCI system
     */
    private final PCIDriver pci;
    /**
     * The bus this device is on
     */
    private final int bus;
    /**
     * The unit number of this this
     */
    private final int unit;
    /**
     * The function number of this device
     */
    private final int function;
    /**
     * PCIConfig of this device
     */
    private final PCIDeviceConfig config;

    /**
     * Create a new instance
     *
     * @param bus
     * @param unit
     * @param function
     */
    public PCIDevice(PCIBus bus, int unit, int function) {
        super(bus, "pci(" + bus.getBus() + "," + unit + "," + function + ")");
        this.pci = bus.getPCI();
        this.bus = bus.getBus();
        this.unit = unit;
        this.function = function;
        this.config = PCIDeviceConfig.createConfig(this);
    }

    /**
     * Gets the bus this device is on
     */
    public int getPCIBus() {
        return bus;
    }

    /**
     * Gets the function number of this device
     */
    public int getFunction() {
        return function;
    }

    /**
     * Gets the unit number of this device
     */
    public int getUnit() {
        return unit;
    }

    /**
     * Gets the PCI configuration for this device
     */
    public PCIDeviceConfig getConfig() {
        return config;
    }

    /**
     * Is this a bridge device.
     */
    public final boolean isBridge() {
        return (config.getBaseClass() == PCIConstants.CLASS_BRIDGE);
    }

    /**
     * Read a configuration dword for this device at a given offset
     *
     * @param offset
     */
    public final int readConfigDword(int offset) {
        return pci.readConfigDword(bus, unit, function, offset);
    }

    /**
     * Read a configuration word for this device at a given offset
     *
     * @param offset
     */
    public final int readConfigWord(int offset) {
        return pci.readConfigWord(bus, unit, function, offset);
    }

    /**
     * Read a configuration byte for this device at a given offset
     *
     * @param offset
     */
    public final int readConfigByte(int offset) {
        return pci.readConfigByte(bus, unit, function, offset);
    }

    /**
     * Write a configuration dword for this device at a given offset
     *
     * @param offset
     * @param value
     */
    public final void writeConfigDword(int offset, int value) {
        pci.writeConfigDword(bus, unit, function, offset, value);
    }

    /**
     * Write a configuration word for this device at a given offset
     *
     * @param offset
     * @param value
     */
    public final void writeConfigWord(int offset, int value) {
        pci.writeConfigWord(bus, unit, function, offset, value);
    }

    /**
     * Write a configuration byte for this device at a given offset
     *
     * @param offset
     * @param value
     */
    public final void writeConfigByte(int offset, int value) {
        pci.writeConfigByte(bus, unit, function, offset, value);
    }

    /**
     * Gets the name containing bus,unit,function.
     */
    public final String getPCIName() {
        return "" + bus + "," + unit + "," + function;
    }

    /**
     * Gets the PCI bus API.
     */
    public final PCIBusAPI getPCIBusAPI() {
        return pci;
    }

    /**
     * Convert to a String representation
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String cname = getClass().getName();
        cname = cname.substring(cname.lastIndexOf('.') + 1);
        return cname + "[" + getId() + ": " + getConfig().toString() + "]";
    }

    /**
     * @see org.jnode.driver.DeviceInfoAPI#showInfo(java.io.PrintWriter)
     */
    public void showInfo(PrintWriter out) {
        out.println("Location      " + getPCIName());
        out.println("Configuration " + getConfig());
    }
}
