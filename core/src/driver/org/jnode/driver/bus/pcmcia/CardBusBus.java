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

package org.jnode.driver.bus.pcmcia;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.driver.Bus;

/**
 * @author markhale
 */
public class CardBusBus extends Bus {
    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(CardBusBus.class);
    /**
     * The CardBus controller
     */
    private final CardBusDriver controller;
    /**
     * All devices connected to this bus
     */
    private final ArrayList<CardBusDevice> list = new ArrayList<CardBusDevice>();

    /**
     * @param controller
     */
    public CardBusBus(CardBusDriver controller) {
        super(controller.getDevice());
        this.controller = controller;
    }

    /**
     * Probe for all devices connected to the this bus.
     *
     * @param result list to add devices to.
     */
    protected void probeDevices(List<CardBusDevice> result) {
        log.debug("Probing CardBus");
        list.clear();
/*
		for (int i = 0; i < 32; i++) {
			PCIDevice dev = createDevice(i, 0);
			if (dev != null) {
				list.add(dev);
				if (dev.getConfig().isMultiFunctional()) {
					for (int f = 1; f < 8; f++) {
						dev = createDevice(i, f);
						if (dev != null) {
							list.add(dev);
						}
					}
				}
			}
		}
*/
        // Add every found device to the result list
        result.addAll(list);
    }
}
