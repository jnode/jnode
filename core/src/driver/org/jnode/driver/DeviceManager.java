/*
 * $Id$
 */
package org.jnode.driver;

import java.util.Collection;

/**
 * Interface of Manager of all devices known to the system.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DeviceManager {

	/**
	 * The name used to lookup this service.
	 */
	public static final String NAME = "system/DeviceManager";

	/**
	 * Returns a collection of all known devices. The collection is not modifiable, but the
	 * underlying collection can change, so be aware of exceptions in iterators.
	 * 
	 * @return A collection of Device instances.
	 */
	public Collection getDevices();

	/**
	 * Returns a collection of all known devices that implement the given api. The collection is
	 * not modifiable, but the underlying collection can change, so be aware of exceptions in
	 * iterators.
	 * 
	 * @param apiClass
	 * @return A collection of Device instances.
	 */
	public Collection getDevicesByAPI(Class apiClass);

	/**
	 * Gets the device with the given ID.
	 * 
	 * @param id
	 * @return The device with the given id
	 * @throws DeviceNotFoundException
	 *             No device with the given id was found.
	 */
	public Device getDevice(String id) throws DeviceNotFoundException;

	/**
	 * Register a new device. A suitable driver will be search for this device and the given device
	 * will be connected to this driver.
	 * 
	 * @param device
	 * @throws DeviceAlreadyRegisteredException
	 * @throws DriverException
	 */
	public void register(Device device) throws DeviceAlreadyRegisteredException, DriverException;

	/**
	 * Unregister a device. The device will be disconnected from its driver.
	 * 
	 * @param device
	 * @throws DriverException
	 */
	public void unregister(Device device) throws DriverException;

	/**
	 * Rename a registered device, optionally using an autonumber postfix
	 * 
	 * @param device
	 * @param name
	 * @param autonumber
	 * @throws DeviceAlreadyRegisteredException
	 */
	public void rename(Device device, String name, boolean autonumber) throws DeviceAlreadyRegisteredException;

	/**
	 * Add a device manager listener
	 * 
	 * @param listener
	 */
	public void addListener(DeviceManagerListener listener);

	/**
	 * Add a device manager listener
	 * 
	 * @param listener
	 */
	public void removeListener(DeviceManagerListener listener);

	/**
	 * Add a device listener
	 * 
	 * @param listener
	 */
	public void addListener(DeviceListener listener);

	/**
	 * Add a device listener
	 * 
	 * @param listener
	 */
	public void removeListener(DeviceListener listener);

	/**
	 * Stop all devices
	 */
	public void stopDevices();

	/**
	 * Gets the system bus. The system bus is the root of all hardware busses and devices connected
	 * to these busses.
	 * 
	 * @return The system bus
	 */
	public Bus getSystemBus();
}