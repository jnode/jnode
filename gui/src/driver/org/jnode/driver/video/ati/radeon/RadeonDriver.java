/*
 * $Id$
 */
package org.jnode.driver.video.ati.radeon;

import org.jnode.driver.DeviceException;
import org.jnode.driver.DriverException;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.video.AbstractFrameBufferDriver;
import org.jnode.driver.video.AlreadyOpenException;
import org.jnode.driver.video.FrameBufferConfiguration;
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
	private final int architecture;
	private final String model;
    
	private static final FrameBufferConfiguration[] CONFIGS = new FrameBufferConfiguration[] { 
		/*RadeonConfiguration.VESA_118, 
		RadeonConfiguration.VESA_115*/ };

    
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
        // TODO Auto-generated method stub
        return null;
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
            throws UnknownConfigurationException, AlreadyOpenException,
            DeviceException {
		for (int i = 0; i < CONFIGS.length; i++) {
			if (config.equals(CONFIGS[i])) {
				kernel.open((RadeonConfiguration) config);
				this.currentConfig = config;
				return kernel;
			}
		}
		throw new UnknownConfigurationException();
    }

	/**
	 * Notify of a close of the graphics object
	 * @param graphics
	 */
	final synchronized void close(RadeonCore graphics) {
		this.currentConfig = null;
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
		//final Device dev = getDevice();
		//dev.registerAPI(DisplayDataChannelAPI.class, kernel);
		//dev.registerAPI(HardwareCursorAPI.class, kernel.getHardwareCursor());
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
		//final Device dev = getDevice();
		//dev.unregisterAPI(DisplayDataChannelAPI.class);
		//dev.unregisterAPI(HardwareCursorAPI.class);
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
