/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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

package org.jnode.driver.bus.firewire;

import org.apache.log4j.Logger;
import org.jnode.driver.Bus;

/**
 * @author markhale
 */
public class FireWireBus extends Bus {
    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(FireWireBus.class);
    /**
     * The FireWire controller
     */
    private final FireWireDriver controller;
    /**
     * My numeric index
     */
    private final int bus;

    /**
     * @param controller
     */
    public FireWireBus(FireWireDriver controller, int bus) {
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
}
