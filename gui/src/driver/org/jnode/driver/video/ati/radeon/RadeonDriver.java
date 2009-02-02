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
 
package org.jnode.driver.video.ati.radeon;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.video.AbstractFrameBufferDriver;
import org.jnode.driver.video.AlreadyOpenException;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.NotOpenException;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.UnknownConfigurationException;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.system.ResourceNotFreeException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RadeonDriver extends AbstractFrameBufferDriver implements RadeonConstants {

    private FrameBufferConfiguration currentConfig;
    private RadeonCore kernel;
    private RadeonSurface surface;
    private final int architecture;
    private final String model;

    private static final FrameBufferConfiguration[] CONFIGS =
            new FrameBufferConfiguration[] {RadeonConfiguration.VESA_118,
                RadeonConfiguration.VESA_115};

    /**
     * Create a new instance
     */
    public RadeonDriver(ConfigurationElement config) throws DriverException {
        this.architecture = parseArchitecture(config);
        this.model = config.getAttribute("name");
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#getConfigurations()
     */
    public FrameBufferConfiguration[] getConfigurations() {
        return CONFIGS;
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#getCurrentConfiguration()
     */
    public FrameBufferConfiguration getCurrentConfiguration() {
        return currentConfig;
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#open(org.jnode.driver.video.FrameBufferConfiguration)
     */
    public Surface open(FrameBufferConfiguration config)
        throws UnknownConfigurationException, AlreadyOpenException, DeviceException {
        for (int i = 0; i < CONFIGS.length; i++) {
            if (config.equals(CONFIGS[i])) {
                try {
                    this.surface = kernel.open((RadeonConfiguration) config);
                    this.currentConfig = config;
                    return surface;
                } catch (ResourceNotFreeException ex) {
                    throw new DeviceException(ex);
                }
            }
        }
        throw new UnknownConfigurationException();
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#getCurrentSurface()
     */
    public synchronized Surface getCurrentSurface() throws NotOpenException {
        if (currentConfig != null) {
            return surface;
        } else {
            throw new NotOpenException();
        }
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#isOpen()
     */
    public final synchronized boolean isOpen() {
        return (currentConfig != null);
    }

    /**
     * Notify of a close of the graphics object
     * 
     * @param graphics
     */
    final synchronized void close(RadeonCore graphics) {
        this.currentConfig = null;
        this.surface = null;
    }

    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected void startDevice() throws DriverException {
        try {
            kernel = new RadeonCore(this, architecture, model, (PCIDevice) getDevice());
        } catch (ResourceNotFreeException ex) {
            throw new DriverException(ex);
        }
        super.startDevice();
        final Device dev = getDevice();
        // dev.registerAPI(DisplayDataChannelAPI.class, kernel);
        dev.registerAPI(HardwareCursorAPI.class, kernel.getHardwareCursor());
    }

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected void stopDevice() throws DriverException {
        if (currentConfig != null) {
            kernel.close();
        }
        if (kernel != null) {
            kernel.release();
            kernel = null;
        }
        final Device dev = getDevice();
        // dev.unregisterAPI(DisplayDataChannelAPI.class);
        dev.unregisterAPI(HardwareCursorAPI.class);
        super.stopDevice();
    }

    private static final int parseArchitecture(ConfigurationElement config) throws DriverException {
        final String arch = config.getAttribute("architecture");
        if (arch == null) {
            throw new DriverException("Architecture must be set");
        } else if (arch.equals("R100")) {
            return Architecture.R100;
        } else if (arch.equals("RV100")) {
            return Architecture.RV100;
        } else if (arch.equals("R200")) {
            return Architecture.R200;
        } else if (arch.equals("RV200")) {
            return Architecture.RV200;
        } else if (arch.equals("RV250")) {
            return Architecture.RV250;
        } else if (arch.equals("R300")) {
            return Architecture.R300;
        } else if (arch.equals("M6")) {
            return Architecture.M6;
        } else if (arch.equals("M7")) {
            return Architecture.M7;
        } else if (arch.equals("M9")) {
            return Architecture.M9;
        } else {
            throw new DriverException("Unknown architecture " + arch);
        }
    }
}
