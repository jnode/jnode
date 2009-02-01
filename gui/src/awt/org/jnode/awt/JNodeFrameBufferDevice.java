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
 
package org.jnode.awt;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.video.FrameBufferAPI;
import org.jnode.driver.video.FrameBufferConfiguration;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class JNodeFrameBufferDevice extends GraphicsDevice implements DeviceListener {

    private final FrameBufferAPI api;
    private final Device device;
    private final GraphicsConfiguration[] configs;
    private boolean stopped = false;
    private GraphicsConfiguration defaultConfig;

    public JNodeFrameBufferDevice(Device device) {
        this.device = device;
        device.addListener(this);
        try {
            this.api = device.getAPI(FrameBufferAPI.class);
        } catch (ApiNotFoundException ex) {
            throw new IllegalArgumentException("Not a FrameBuffer device " + device.getId());
        }
        final FrameBufferConfiguration[] fbConfigs = api.getConfigurations();
        configs = new JNodeGraphicsConfiguration[fbConfigs.length];
        for (int i = 0; i < fbConfigs.length; i++) {
            configs[i] = new JNodeGraphicsConfiguration(this, fbConfigs[i]);
        }
        defaultConfig = configs[0];
    }

    /**
     * @return The configurations
     * @see java.awt.GraphicsDevice#getConfigurations()
     */
    public GraphicsConfiguration[] getConfigurations() {
        return configs;
    }

    /**
     * @return The default configuration
     * @see java.awt.GraphicsDevice#getDefaultConfiguration()
     */
    public GraphicsConfiguration getDefaultConfiguration() {
        return defaultConfig;
    }

    /**
     * @param config the new configuration
     * @see java.awt.GraphicsDevice#getDefaultConfiguration()
     */
    public void setDefaultConfiguration(GraphicsConfiguration config) {
        if (config == null) throw new IllegalArgumentException("Default graphics configuration is null.");
        defaultConfig = config;
    }

    /**
     * @return The id string
     * @see java.awt.GraphicsDevice#getIDstring()
     */
    public String getIDstring() {
        return device.getId();
    }

    /**
     * @return The type
     * @see java.awt.GraphicsDevice#getType()
     */
    public int getType() {
        return TYPE_RASTER_SCREEN;
    }

    public FrameBufferAPI getAPI() {
        return api;
    }

    public Device getDevice() {
        return device;
    }

    /**
     * Is this device active?
     *
     * @return true if this device is active, false otherwise
     */
    public final boolean isActive() {
        return !stopped;
    }

    /**
     * @see org.jnode.driver.DeviceListener#deviceStarted(org.jnode.driver.Device)
     */
    public void deviceStarted(Device device) {
    }

    /**
     * @see org.jnode.driver.DeviceListener#deviceStop(org.jnode.driver.Device)
     */
    public void deviceStop(Device device) {
        stopped = true;
    }
}
