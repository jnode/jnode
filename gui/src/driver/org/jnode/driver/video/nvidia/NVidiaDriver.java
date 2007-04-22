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
 
package org.jnode.driver.video.nvidia;

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
import org.jnode.driver.video.ddc.DisplayDataChannelAPI;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.system.ResourceNotFreeException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NVidiaDriver extends AbstractFrameBufferDriver implements NVidiaConstants {

	private FrameBufferConfiguration currentConfig;
	private NVidiaCore kernel;
	private final int architecture;
	private final String model;

	private static final FrameBufferConfiguration[] CONFIGS = new FrameBufferConfiguration[] { 
		NVidiaConfiguration.VESA_118, 
		NVidiaConfiguration.VESA_115 };

	/**
	 * Create a new instance
	 */
	public NVidiaDriver(ConfigurationElement config) throws DriverException {
	    this(parseArchitecture(config), config.getAttribute("name"));
	}

	/**
	 * Create a new instance
	 */
	public NVidiaDriver(int architecture, String model) {
		this.architecture = architecture;
		this.model = model;
	}

	/**
	 * @see org.jnode.driver.video.FrameBufferAPI#getConfigurations()
	 */
	public final FrameBufferConfiguration[] getConfigurations() {
		return CONFIGS;
	}

	/**
	 * @see org.jnode.driver.video.FrameBufferAPI#getCurrentConfiguration()
	 */
	public final FrameBufferConfiguration getCurrentConfiguration() {
		return currentConfig;
	}

	/**
	 * @see org.jnode.driver.video.FrameBufferAPI#open(org.jnode.driver.video.FrameBufferConfiguration)
	 */
	public synchronized Surface open(FrameBufferConfiguration config) throws UnknownConfigurationException, AlreadyOpenException, DeviceException {
		for (int i = 0; i < CONFIGS.length; i++) {
			if (config.equals(CONFIGS[i])) {
				kernel.open((NVidiaConfiguration) config);
				this.currentConfig = config;
				return kernel;
			}
		}
		throw new UnknownConfigurationException();
	}
    
	/**
     * @see org.jnode.driver.video.FrameBufferAPI#getCurrentSurface()
     */
    public synchronized Surface getCurrentSurface() throws NotOpenException {
        if (currentConfig != null) {
            return kernel;
        } else {
            throw new NotOpenException();
        }
    }

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#isOpen()
     */
    public synchronized final boolean isOpen() {
        return (currentConfig != null);
    }

    /**
	 * Notify of a close of the graphics object
	 * @param graphics
	 */
	final synchronized void close(NVidiaCore graphics) {
		this.currentConfig = null;
	}
	/**
	 * @see org.jnode.driver.Driver#startDevice()
	 */
	protected void startDevice() throws DriverException {
		try {
			kernel = new NVidiaCore(this, architecture, model, (PCIDevice) getDevice());
		} catch (ResourceNotFreeException ex) {
			throw new DriverException(ex);
		}
		super.startDevice();
		final Device dev = getDevice();
		dev.registerAPI(DisplayDataChannelAPI.class, kernel);
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
		dev.unregisterAPI(DisplayDataChannelAPI.class);
		dev.unregisterAPI(HardwareCursorAPI.class);
		super.stopDevice();
	}
	
	private static final int parseArchitecture(ConfigurationElement config) throws DriverException {
	    final String arch = config.getAttribute("architecture");
	    if (arch == null) {
	        throw new DriverException("Architecture must be set");
	    } else if (arch.equals("NV04A")) {
	        return NV04A;
	    } else if (arch.equals("NV10A")) {
	        return NV10A;
	    } else if (arch.equals("NV11")) {
	        return NV10A;
	    } else if (arch.equals("NV11DDR")) {
	        return NV10A;
	    } else if (arch.equals("NV20A")) {
	        return NV20A;
	    } else if (arch.equals("NV28M")) {
	        return NV28M;
	    } else if (arch.equals("NV30A")) {
	        return NV30A;
	    } else {
	        throw new DriverException("Unknown architecture " + arch);
	    }
	}

}
