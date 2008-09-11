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

package org.jnode.debugger;

import java.util.ArrayList;
import java.util.Collection;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.SystemTriggerAPI;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DebuggerPlugin extends Plugin implements DeviceListener {

    private final Debugger debugger = new Debugger();

    /**
     * @param descriptor
     */
    public DebuggerPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected void startPlugin() throws PluginException {
        try {
            final DeviceManager dm = DeviceUtils.getDeviceManager();
            dm.addListener(this);
            final Collection<Device> devs = new ArrayList<Device>(dm.getDevices());
            for (Device dev : devs) {
                addListeners(dev);
            }
        } catch (NameNotFoundException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    protected void stopPlugin() throws PluginException {
        try {
            final DeviceManager dm = DeviceUtils.getDeviceManager();
            dm.removeListener(this);
            final Collection<Device> devs = dm.getDevices();
            for (Device dev : devs) {
                removeListeners(dev);
            }
        } catch (NameNotFoundException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * @see org.jnode.driver.DeviceListener#deviceStarted(org.jnode.driver.Device)
     */
    public void deviceStarted(Device device) {
        addListeners(device);
    }

    /**
     * @see org.jnode.driver.DeviceListener#deviceStop(org.jnode.driver.Device)
     */
    public void deviceStop(Device device) {
        removeListeners(device);
    }

    private void addListeners(Device device) {
        if (device.implementsAPI(SystemTriggerAPI.class)) {
            try {
                final SystemTriggerAPI api = (SystemTriggerAPI) device
                    .getAPI(SystemTriggerAPI.class);
                api.addSystemTriggerListener(debugger);
            } catch (ApiNotFoundException ex) {
                // Ignore
            }
        }
        if (device.implementsAPI(KeyboardAPI.class)) {
            try {
                final KeyboardAPI api = (KeyboardAPI) device
                    .getAPI(KeyboardAPI.class);
                api.addKeyboardListener(debugger);
            } catch (ApiNotFoundException ex) {
                // Ignore
            }
        }
    }

    private void removeListeners(Device device) {
        if (device.implementsAPI(SystemTriggerAPI.class)) {
            try {
                final SystemTriggerAPI api = (SystemTriggerAPI) device
                    .getAPI(SystemTriggerAPI.class);
                api.removeSystemTriggerListener(debugger);
            } catch (ApiNotFoundException ex) {
                // Ignore
            }
        }
        if (device.implementsAPI(KeyboardAPI.class)) {
            try {
                final KeyboardAPI api = (KeyboardAPI) device
                    .getAPI(KeyboardAPI.class);
                api.removeKeyboardListener(debugger);
            } catch (ApiNotFoundException ex) {
                // Ignore
            }
        }
    }
}
