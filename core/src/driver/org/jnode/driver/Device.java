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
 
package org.jnode.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.jnode.system.BootLog;
import org.jnode.system.ResourceOwner;
import org.jnode.util.StopWatch;

/**
 * A software representation of a hardware device.
 * 
 * Every device is controlled by a Driver. These drivers are found by DeviceToDriverMapper
 * instances.
 * 
 * @see org.jnode.driver.Driver
 * @see org.jnode.driver.DeviceToDriverMapper
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Device implements ResourceOwner {

	/** The bus that i'm connected to */
	private final Bus bus;
	/** My driver */
	private Driver driver;
	/** My identifier */
	private String id;
	/** Has this device been started? */
	private boolean started = false;
	/** The API's implemented by this device */
	private final HashMap apis = new HashMap();
	/** My listeners */
	private final ArrayList listeners = new ArrayList();
	/** The manager */
	private DeviceManager manager;

	/**
	 * Create a new instance
	 * 
	 * @param bus
	 * @param id
	 */
	public Device(Bus bus, String id) {
		this.id = id;
		this.bus = bus;
	}

	/**
	 * Gets the driver of this device.
	 * @return My driver, can be null
	 */
	final Driver getDriver() {
		return driver;
	}

	/**
	 * Gets the classname of my driver.
	 * @return String can be null.
	 */
	public final String getDriverClassName() {
		final Driver driver = this.driver;
		if (driver != null) {
			return driver.getClass().getName();
		} else {
			return null;
		}
	}

	/**
	 * Gets the bus this device is connected to.
	 * 
	 * @return My parent bus
	 */
	public final Bus getBus() {
		return this.bus;
	}

	/**
	 * @param driver
	 * @see org.jnode.driver.Device#setDriver(org.jnode.driver.Driver)
	 * @throws DriverException
	 */
	public void setDriver(Driver driver) throws DriverException {
		try {
			driver.connect(this);
			this.driver = driver;
		} catch (DriverException ex) {
			this.driver = null;
			throw new DriverException("Cannot set driver", ex);
		}
	}

	/**
	 * @see org.jnode.driver.Device#getId()
	 * @return The id of this device
	 */
	public final String getId() {
		return id;
	}

	/**
	 * Change the id of this device, only called by devicemanager
	 * 
	 * @param newId
	 */
	final void setId(String newId) {
		this.id = newId;
	}

	/**
	 * Start this device.
	 * 
	 * @throws DriverException
	 */
	final void start() throws DriverException {
		if (driver == null) {
			throw new DriverException("Cannot start without a driver");
		} else if (manager == null) {
			throw new DriverException("Cannot start without being registered");
		} else if (!started) {
			// Let extensions do their start work
			onStartDevice();
			// Let the driver start me
			driver.startDevice();
			// I'm started
			started = true;
			// Notify my listeners
			fireStartedEvent();
		}
	}

	/**
	 * Start this device.
	 * 
	 * @throws DriverException
	 */
	final void stop(boolean unsetDriver) throws DriverException {
		if (driver == null) {
			throw new DriverException("Cannot stop without a driver");
		} else if (manager == null) {
			throw new DriverException("Cannot stop without being registered");
		} else if (started) {
			// Notify my listeners
			fireStopEvent();
			// Let the driver stop me
			driver.stopDevice();
			// Let extensions do their stop work
			onStopDevice();
			// Remove the driver connection if requested
			if (unsetDriver) {
			    this.driver = null;
			}
			// I'm stopped now.
			started = false;
		}
	}

	/**
	 * Has this device been started?
	 * 
	 * @return boolean
	 */
	public final boolean isStarted() {
		return started;
	}

	/**
	 * Add an API implementation to the list of API's implemented by this device.
	 * 
	 * @param apiInterface
	 * @param apiImplementation
	 */
	public final void registerAPI(Class apiInterface, DeviceAPI apiImplementation) {
		if (!apiInterface.isInstance(apiImplementation)) {
			throw new IllegalArgumentException("API implementation does not implement API interface");
		}
		if (!apiInterface.isInterface()) {
			throw new IllegalArgumentException("API interface must be an interface");
		}
		apis.put(apiInterface, apiImplementation);
		final Class[] interfaces = apiInterface.getInterfaces();
		if (interfaces != null) {
			for (int i = 0; i < interfaces.length; i++) {
				final Class intf = interfaces[i];
				if (!apis.containsKey(intf)) {
					apis.put(intf, apiImplementation);
				}
			}
		}
	}

	/**
	 * Remove an API implementation from the list of API's implemented by this device.
	 * 
	 * @param apiInterface
	 */
	public final void unregisterAPI(Class apiInterface) {
		apis.remove(apiInterface);
	}

	/**
	 * Does this device implement the given API?
	 * 
	 * @param apiInterface
	 * @return boolean
	 */
	public final boolean implementsAPI(Class apiInterface) {
		return apis.containsKey(apiInterface);
	}

	/**
	 * Gets all implemented API's?
	 * 
	 * @return A set of Class instances
	 */
	public final Set implementedAPIs() {
		return apis.keySet();
	}

	/**
	 * Gets the implementation of a given API.
	 * 
	 * @param apiInterface
	 * @return The api implementation
	 * @throws ApiNotFoundException
	 *             The given api has not been found
	 */
	public final DeviceAPI getAPI(Class apiInterface) throws ApiNotFoundException {
		DeviceAPI impl = (DeviceAPI) apis.get(apiInterface);
		if (impl == null) {
			throw new ApiNotFoundException(apiInterface.getName());
		}
		return impl;
	}

	/**
	 * Add a listener
	 * 
	 * @param listener
	 */
	public final void addListener(DeviceListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a listener
	 * 
	 * @param listener
	 */
	public final void removeListener(DeviceListener listener) {
		listeners.remove(listener);
	}

	/**
	 * This method is called during the start of the device. Just before the call to startDevice of
	 * the connected driver.
	 * 
	 * @throws DriverException
	 */
	protected void onStartDevice() throws DriverException {
	}

	/**
	 * This method is called during the stop of the device. Just after the call to stopDevice of
	 * the connected driver.
	 * 
	 * @throws DriverException
	 */
	protected void onStopDevice() throws DriverException {
	}

	/**
	 * Fire a deviceStarted event to all my listeners
	 */
	protected final void fireStartedEvent() {
		final StopWatch sw = new StopWatch();
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			final DeviceListener l = (DeviceListener) i.next();
			sw.start();
			l.deviceStarted(this);
			if (sw.isElapsedLongerThen(100)) {
			    BootLog.error("DeviceListener took " + sw + " in deviceStarted: " + l.getClass().getName());
			}
		}
		manager.fireStartedEvent(this);
	}

	/**
	 * Fire a deviceStop event to all my listeners
	 */
	protected final void fireStopEvent() {
		manager.fireStopEvent(this);
		final StopWatch sw = new StopWatch();
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			final DeviceListener l = (DeviceListener) i.next();
			sw.start();
			l.deviceStop(this);
			if (sw.isElapsedLongerThen(100)) {
			    BootLog.error("DeviceListener took " + sw + " in deviceStop: " + l.getClass().getName());
			}
		}
	}

	/**
	 * @see org.jnode.system.ResourceOwner#getShortDescription()
	 * @return The short description
	 */
	public String getShortDescription() {
		return getId();
	}

	/**
	 * @return Returns the manager.
	 */
	public final DeviceManager getManager() {
		return this.manager;
	}

	/**
	 * @param manager
	 *            The manager to set.
	 */
	final void setManager(DeviceManager manager) {
		if (this.manager != null) {
			throw new SecurityException("Cannot overwrite the device manager");
		} else {
			this.manager = manager;
		}
	}

}
