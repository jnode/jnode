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
 
package org.jnode.driver.video.vga;

import java.awt.image.IndexColorModel;

import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;
import org.jnode.driver.video.AbstractFrameBufferDriver;
import org.jnode.driver.video.AlreadyOpenException;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.NotOpenException;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.UnknownConfigurationException;
import org.jnode.driver.video.vgahw.VgaConstants;
import org.jnode.system.ResourceNotFreeException;

/**
 * @author epr
 */
public class VGADriver extends AbstractFrameBufferDriver implements VgaConstants {

	static final IndexColorModel COLOR_MODEL = new IndexColorModel(4, 16, REDS, GREENS, BLUES);

	private static final FrameBufferConfiguration[] CONFIGS = { new VGAConfiguration(640, 480, COLOR_MODEL)};

	private FrameBufferConfiguration currentConfig;
	private VGASurface vga;

	/**
	 * @see org.jnode.driver.Driver#stopDevice()
	 */
	protected synchronized void stopDevice() throws DriverException {
		if (vga != null) {
			vga.close();
		}
		super.stopDevice();
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
	public synchronized Surface open(FrameBufferConfiguration config) throws UnknownConfigurationException, AlreadyOpenException, DeviceException {
		if (currentConfig != null) {
			throw new AlreadyOpenException();
		} else if (config.equals(CONFIGS[0])) {
			try {
				vga = new VGASurface(this);
				currentConfig = config;
				vga.open(config);
				return vga;
			} catch (ResourceNotFreeException ex) {
				throw new DeviceException(ex);
			} catch (DriverException ex) {
				throw new DeviceException(ex);
            }
		} else {
			throw new UnknownConfigurationException();
		}
	}

    /**
     * @see org.jnode.driver.video.FrameBufferAPI#getCurrentSurface()
     */
    public synchronized Surface getCurrentSurface() throws NotOpenException {
        if (currentConfig != null) {
            return vga;
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
	 * The given surface is closed.
	 */
	synchronized void close(VGASurface vga) {
		vga = null;
		currentConfig = null;
	}
}
