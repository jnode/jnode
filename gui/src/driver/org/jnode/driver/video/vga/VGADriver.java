/*
 * $Id$
 */
package org.jnode.driver.video.vga;

import java.awt.image.IndexColorModel;

import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;
import org.jnode.driver.video.AbstractFrameBufferDriver;
import org.jnode.driver.video.AlreadyOpenException;
import org.jnode.driver.video.FrameBufferConfiguration;
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

	public VGADriver() {
	    new Throwable("Stacktrace").printStackTrace();
	}
	
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
	 * The given surface is closed.
	 */
	synchronized void close(VGASurface vga) {
		vga = null;
		currentConfig = null;
	}
}
