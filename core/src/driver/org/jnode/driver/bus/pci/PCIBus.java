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

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.driver.Bus;

/**
 * @author epr
 */
public class PCIBus extends Bus implements PCIConstants {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(PCIBus.class);
    /**
     * The PCI system
     */
    private final PCIDriver pci;
    /**
     * My numeric index
     */
    private final int bus;
    /**
     * All devices connected to this bus
     */
    private final ArrayList<PCIDevice> list = new ArrayList<PCIDevice>();

    /**
     * Initialize a root bus (bus 0)
     *
     * @param pci
     */
    public PCIBus(Bus parent, PCIDriver pci) {
        super(parent);
        this.pci = pci;
        this.bus = 0;
    }

    public PCIBus(PCIBus parent, int bus) {
        super(parent);
        this.pci = parent.pci;
        this.bus = bus;
    }

    /**
     * Gets the numeric index of this bus.
     */
    public final int getBus() {
        return bus;
    }

    /**
     * Probe for all devices connected to this bus.
     *
     * @param result
     */
    protected void probeDevices(List<PCIDevice> result) {
        log.debug("Probing PCI bus " + bus);
        list.clear();
        for (int i = 0; i < MAX_UNITS; i++) {
            PCIDevice dev = createDevice(i, 0);
            if (dev != null) {
                list.add(dev);
                if (dev.getConfig().isMultiFunctional()) {
                    for (int f = 1; f < MAX_FUNCTIONS; f++) {
                        dev = createDevice(i, f);
                        if (dev != null) {
                            list.add(dev);
                        }
                    }
                }
            }
        }
        // Add every found device to the result list
        result.addAll(list);
        // Now probe all bridges
        for (PCIDevice dev : list) {
            final PCIDeviceConfig cfg = dev.getConfig();
            if ((cfg.getBaseClass() == CLASS_BRIDGE) && (cfg.getSubClass() == SUBCLASS_BR_PCI)) {
                log.debug("Found PCI-PCI bridge " + dev.getPCIName());
                final PCIBus newBus = new PCIBus(this, cfg.asHeaderType1().getSecondaryBus());
                newBus.probeDevices(result);
            }

        }
    }

    /**
     * Create a PCIDevice instance based on (1) the presence of the device
     * and (2) the class of the device.
     *
     * @param unit
     * @param func
     * @return A new PCIDevice for the given bus, unit and function or
     *         <code>null</code> if no device is present at the given bus, unit and function.
     */
    private final PCIDevice createDevice(int unit, int func) {
        if (PCIDeviceConfig.isPresent(pci, bus, unit, func)) {
            //final int[] pciClass = PCIDeviceConfig.getPCIClass(this, bus, unit, func);
            //final int major = pciClass[0];
            //final int sub = pciClass[1];

            // No specific device class, so just create the default
            return new PCIDevice(this, unit, func);
        } else {
            return null;
        }
    }

    /**
     * Return the list of PCI devices connected to this bus.
     *
     * @return A List containing all connected devices as instanceof PCIDevice.
     */
    public List getDevices() {
        return list;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "PCIBus: " + bus;
    }

    /**
     * @return
     */
    final PCIDriver getPCI() {
        return this.pci;
    }

}
