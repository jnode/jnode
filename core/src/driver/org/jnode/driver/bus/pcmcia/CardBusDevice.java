/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.bus.pcmcia;

import java.io.PrintWriter;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceInfoAPI;

/**
 * @author markhale
 */
public class CardBusDevice extends Device implements DeviceInfoAPI {

    /**
     * The bus this device is on
     */
    private final int bus;
    /**
     * The function number of this device
     */
    private final int function;

    /**
     * Create a new instance
     *
     * @param bus
     * @param function
     */
    public CardBusDevice(CardBusBus bus, int function) {
        super(bus, "cardbus(" + bus.getBus() + "," + function + ")");
        this.bus = bus.getBus();
        this.function = function;
    }

    /**
     * @see org.jnode.driver.DeviceInfoAPI#showInfo(java.io.PrintWriter)
     */
    public void showInfo(PrintWriter out) {
    }
}
