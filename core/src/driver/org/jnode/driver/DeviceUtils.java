/*
 * $Id$
 */
package org.jnode.driver;

import java.util.Collection;
import java.util.Collections;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;

/**
 * Class with utility methods for the device framework.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DeviceUtils {

	/** Cached devicemanager reference */
	private static DeviceManager dm;
	
	/**
	 * Gets the device manager
	 * @return The device manager
	 * @throws NameNotFoundException
	 */
	public static DeviceManager getDeviceManager() 
	throws NameNotFoundException {
		if (dm == null) {
			dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
		}
		return dm;
	}

	/**
	 * Gets a device by name
	 * @param deviceID
	 * @return The device
	 * @throws DeviceNotFoundException
	 */
	public static Device getDevice(String deviceID) 
	throws DeviceNotFoundException {
		try {
			return getDeviceManager().getDevice(deviceID);
		} catch (NameNotFoundException ex) {
			throw new DeviceNotFoundException("DeviceManager not found", ex);
		}
	}
	
	/**
	 * Gets a specific API from a device.
	 * @param deviceID the ame of the requested device
	 * @param api the API class to use
	 * @return The api implementation
	 * @throws DeviceNotFoundException
	 * @throws ApiNotFoundException
	 */
	public static DeviceAPI getAPI(String deviceID, Class api) 
	throws DeviceNotFoundException, ApiNotFoundException
	{
		try {
			return getDeviceManager().getDevice(deviceID).getAPI(api);
		} catch (NameNotFoundException ex) {
			throw new DeviceNotFoundException("DeviceManager not found", ex);
		}
	}
	

	/**
	 * Returns a collection of all known devices that implement the given api..
	 * The collection is not modifiable, but the underlying collection
	 * can change, so be aware of exceptions in iterators.
	 * @param apiClass
	 * @return All known devices the implement the given api.
	 */
	public static Collection getDevicesByAPI(Class apiClass) {
		try {
			return getDeviceManager().getDevicesByAPI(apiClass);
		} catch (NameNotFoundException ex) {
			return Collections.EMPTY_LIST;
		}
	}
}
