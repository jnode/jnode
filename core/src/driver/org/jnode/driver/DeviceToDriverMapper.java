/*
 * $Id$
 */
package org.jnode.driver;

/**
 * Interface used to search for a suitable driver for a given device.
 * 
 * Each DeviceToDriverMapper must be registered in the DeviceManager
 * and is called in order of registration to search for a suitable driver.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DeviceToDriverMapper {

	/** Match on exact device and exact revision, best possible match. */
	public static final int MATCH_DEVICE_REVISION = 0;
	
	/** Match on exact device, good match */
	public static final int MATCH_DEVICE = 1;
	
	/** Match on device class */
	public static final int MATCH_DEVCLASS = 2;
	
	/**
	 * Return a suitable driver for the given device, or if no suitable
	 * driver is found, return <code>null</code>.
	 * @param device
	 * @return A suitable driver of <code>null</code> if no suitable driver is found.
	 */
	public Driver findDriver(Device device);
	
	/**
	 * Gets the matching level of this mapper.
	 * The mappers are queried in order of match level. This will ensure
	 * the best available driver for a device.
	 * This method must return a constant value for the entire
	 * lifetime of this mapper.
	 * 
	 * @return One of the MATCH_xxx constants.
	 * @see #MATCH_DEVICE_REVISION
	 * @see #MATCH_DEVICE
	 * @see #MATCH_DEVCLASS
	 */
	public int getMatchLevel();
}
