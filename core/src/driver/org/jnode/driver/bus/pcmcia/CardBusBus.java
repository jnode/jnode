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
 
package org.jnode.driver.bus.pcmcia;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.driver.Bus;
import org.jnode.driver.bus.pci.PCIConstants;

/**
 * @author markhale
 */
public class CardBusBus extends Bus implements PCIConstants {
    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(CardBusBus.class);
    /**
     * The CardBus controller
     */
    private final CardBusDriver controller;
    /**
     * My numeric index
     */
    private final int bus;
    /**
     * All devices connected to this bus.
     * A multifunction card may contain up to 8 functions/devices.
     */
    private final ArrayList<CardBusDevice> list = new ArrayList<CardBusDevice>(2);

    /**
     * @param controller
     */
    public CardBusBus(CardBusDriver controller, int bus) {
        super(controller.getDevice());
        this.controller = controller;
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
    protected void probeDevices(List<CardBusDevice> result) {
        log.debug("Probing CardBus " + bus);
        list.clear();
        /*
        CardBusDevice dev = createDevice(0);
        if (dev != null) {
            list.add(dev);
            if (dev.getConfig().isMultiFunctional()) {
                for (int f = 1; f < MAX_FUNCTIONS; f++) {
                    dev = createDevice(f);
                    if (dev != null) {
                        list.add(dev);
                    }
                }
            }
        }
        */
        // Add every found device to the result list
        result.addAll(list);
    }

    /**
     * @param func
     * @return A new CardBusDevice for the given bus and function or
     * <code>null</code> if no device is present at the given bus and function.
     */
    /*
    private final CardBusDevice createDevice(int func) {
        if (PCIDeviceConfig.isPresent(controller, bus, func)) {
            return new CardBusDevice(this, func);
        } else {
            return null;
        }
    }
    */
}
