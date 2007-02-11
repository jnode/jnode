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
 
package org.jnode.test.fs.filesystem.config;

import java.io.IOException;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.naming.InitialNaming;

/**
 * @author Fabien DUMINY
 *  
 */
public class JNodeDeviceParam extends DeviceParam {
    /**
     * 
     *  
     */
    public JNodeDeviceParam() {

    }

    /**
     *  
     */
    public Device createDevice() throws Exception {
        return lookupDevice();
    }

    /**
     * @param deviceName
     *            The name to set.
     */
    public void setName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     * 
     * @return @throws
     *         IOException
     */
    protected Device lookupDevice() throws IOException {
        Device device = null;
        try {
            device = InitialNaming.lookup(DeviceManager.NAME)
                    .getDevice(deviceName);
        } catch (DeviceNotFoundException ex) {
            final IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        } catch (NameNotFoundException ex) {
			final IOException ioe = new IOException();
			ioe.initCause(ex);
			throw ioe;
        }
        return device;
    }

    /**
     *  
     */
    public void tearDown(Device device) throws Exception {
        // nothing to do
    }

    public String toString() {
        return "Device[" + deviceName + "]";
    }

    /**
     * 
     * @return
     */
    public String getDeviceName() {
        return deviceName;
    }

    private String deviceName;
}
