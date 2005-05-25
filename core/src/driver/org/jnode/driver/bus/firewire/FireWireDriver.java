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

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.naming.InitialNaming;

/**
 * Driver for a FireWire controller.
 *
 * @author markhale
 */
public class FireWireDriver extends Driver {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(FireWireDriver.class);
    private FireWireBus bus;

    public FireWireDriver() {
    }

    protected void startDevice() throws DriverException {
        final Device device = getDevice();
        final DeviceManager dm;
        try {
            dm = (DeviceManager) InitialNaming.lookup(DeviceManager.NAME);
            dm.rename(device, getDevicePrefix(), true);
        } catch (DeviceAlreadyRegisteredException ex) {
            log.error("Cannot rename device", ex);
            throw new DriverException("Cannot rename device", ex);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find DeviceManager", ex);
        }
        bus = new FireWireBus(this);
    }

    protected void stopDevice() throws DriverException {
        /** @todo Implement this org.jnode.driver.Driver abstract method */
    }

    protected String getDevicePrefix() {
        return "firewire";
    }
}
