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

package org.jnode.driver.video;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.naming.InitialNaming;

/**
 * @author epr
 */
public abstract class AbstractFrameBufferDriver extends Driver implements FrameBufferAPI {

    /** My logger */
    private static final Logger log = Logger.getLogger(AbstractFrameBufferDriver.class);

    /** Device name prefix of framebuffer devices */
    public static final String FB_DEVICE_PREFIX = "fb";

    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected void startDevice() throws DriverException {
        final Device device = getDevice();
        try {
            final DeviceManager dm = (DeviceManager) InitialNaming.lookup(DeviceManager.NAME);
            dm.rename(device, getDevicePrefix() + "-" + device.getId(), false);
        } catch (DeviceAlreadyRegisteredException ex) {
            log.error("Cannot rename device", ex);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find DeviceManager", ex);
        }
        device.registerAPI(FrameBufferAPI.class, this);
    }

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected void stopDevice() throws DriverException {
        final Device dev = getDevice();
        dev.unregisterAPI(FrameBufferAPI.class);
    }

    /**
     * Gets the prefix for the device name
     * 
     * @see #FB_DEVICE_PREFIX
     */
    protected String getDevicePrefix() {
        return FB_DEVICE_PREFIX;
    }

}
