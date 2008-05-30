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
    public static final Class<DeviceManager> NAME = DeviceManager.class;

    /**
     * Returns a collection of all known devices. The collection is not modifiable, but the
     * underlying collection can change, so be aware of exceptions in iterators.
     *
     * @return A collection of Device instances.
     */
    public Collection<Device> getDevices();

    /**
     * Returns a collection of all known devices that implement the given api. The collection is
     * not modifiable, but the underlying collection can change, so be aware of exceptions in
     * iterators.
     *
     * @param apiClass
     * @return A collection of Device instances.
     */
    public Collection<Device> getDevicesByAPI(Class<? extends DeviceAPI> apiClass);

    /**
     * Gets the device with the given ID.
     *
     * @param id
     * @return The device with the given id
     * @throws DeviceNotFoundException No device with the given id was found.
     */
    public Device getDevice(String id) throws DeviceNotFoundException;

    /**
     * Register a new device. A suitable driver will be search for this device and the given device
     * will be connected to this driver.
     *
     * @param device
     * @throws DeviceAlreadyRegisteredException
     *
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
     * Start a given device.
     * The device must have been registered.
     * This method blocks until the device has been started.
     *
     * @param device
     * @throws DeviceNotFoundException The device has not been registered.
     * @throws DriverException
     */
    public void start(Device device) throws DeviceNotFoundException, DriverException;

    /**
     * Stop a given device.
     * The device must have been registered.
     * This method blocks until the device has been stopped.
     *
     * @param device
     * @throws DeviceNotFoundException The device has not been registered.
     * @throws DriverException
     */
    public void stop(Device device) throws DeviceNotFoundException, DriverException;

    /**
     * Rename a registered device, optionally using an autonumber postfix
     *
     * @param device
     * @param name
     * @param autonumber
     * @throws DeviceAlreadyRegisteredException
     *
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

    /**
     * Gets the default timeout for device startup.
     *
     * @return Returns the defaultStartTimeout.
     */
    public long getDefaultStartTimeout();

    /**
     * Sets the default timeout for device startup.
     *
     * @param defaultStartTimeout The defaultStartTimeout to set.
     */
    public void setDefaultStartTimeout(long defaultStartTimeout);
}
