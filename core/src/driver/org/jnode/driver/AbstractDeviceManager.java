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
 
package org.jnode.driver;

import gnu.java.security.action.GetPropertyAction;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginException;
import org.jnode.system.BootLog;
import org.jnode.util.StopWatch;

/**
 * Default device manager.
 *
 * @author epr
 */
public abstract class AbstractDeviceManager implements DeviceManager {

    /**
     * All registered devices
     */
    private final Map<String, Device> devices = new HashMap<String, Device>();

    /**
     * All registered device to driver mappers
     */
    private final List<DeviceToDriverMapper> mappers = new ArrayList<DeviceToDriverMapper>();

    /**
     * All registered device finders
     */
    private final List<DeviceFinder> finders = new ArrayList<DeviceFinder>();

    /**
     * All listeners to my events
     */
    private final List<DeviceManagerListener> listeners = new LinkedList<DeviceManagerListener>();

    /**
     * All listeners to device events
     */
    private final List<DeviceListener> deviceListeners = new LinkedList<DeviceListener>();

    /**
     * The system bus
     */
    private final Bus systemBus;

    /**
     * The JNode command line
     */
    private final String cmdLine;

    private boolean extensionsLoaded = false;

    private long defaultStartTimeout = 10000;

    private long fastStartTimeout = 1000;

    /**
     * Create a new instance
     */
    public AbstractDeviceManager() {
        cmdLine = (String) AccessController.doPrivileged(new GetPropertyAction(
            "jnode.cmdline", ""));
        this.systemBus = new SystemBus();
    }

    /**
     * Returns a collection of all known devices. The collection is not
     * modifiable, but the underlying collection can change, so be aware of
     * exceptions in iterators.
     *
     * @return All known devices.
     */
    public final Collection<Device> getDevices() {
        return Collections.unmodifiableCollection(devices.values());
    }

    /**
     * Returns a collection of all known devices that implement the given api..
     * The collection is not modifiable, but the underlying collection can
     * change, so be aware of exceptions in iterators.
     *
     * @param apiClass
     * @return All known devices the implement the given api.
     */
    public final Collection<Device> getDevicesByAPI(Class<? extends DeviceAPI> apiClass) {
        final ArrayList<Device> result = new ArrayList<Device>();
        for (Device dev : devices.values()) {
            if (dev.implementsAPI(apiClass)) {
                result.add(dev);
            }
        }
        return result;
    }

    /**
     * Gets the device with the given ID.
     *
     * @param id
     * @return The device with the given id
     * @throws DeviceNotFoundException No device with the given id was found.
     */
    public final Device getDevice(String id) throws DeviceNotFoundException {
        final Device device = devices.get(id);
        if (device == null) {
            throw new DeviceNotFoundException(id);
        }
        return device;
    }

    /**
     * Register a new device. This involves the following steps:
     * <ul>
     * <li>Search for a suitable driver for the device. If not found the driver
     * startup is delayed.
     * <li>Connect the driver to the device, if a driver is found
     * <li>Attempt to start the device. If this fails an exception is printed
     * in the log. You can test if the device was started succesfully, by read
     * the <code>isStarted</code> status.
     * </ul>
     * Note that if the device already has a driver connected to it, the first
     * two steps are ignored.
     *
     * @param device
     * @throws DeviceAlreadyRegisteredException
     *
     * @throws DriverException
     */
    public final void register(Device device)
        throws DeviceAlreadyRegisteredException, DriverException {

        boolean shouldStart;

        // Perform the actual registration.
        shouldStart = doRegister(device);

        // Test for no<id> on the command line
        if (cmdLine.indexOf("no" + device.getId()) >= 0) {
            BootLog.info("Blocking the start of " + device.getId());
            shouldStart = false;
        }

        // Notify my listeners
        fireRegisteredEvent(device);

        // Should we start the device?
        if (shouldStart) {
            // Try to start the device
            try {
                start(device);
            } catch (DeviceNotFoundException ex) {
                // Should not happen
                BootLog.error("Device removed before being started", ex);
            }
        }
    }

    /**
     * Actually register the device. The device is not started, nor is the
     * registered event fired.
     *
     * @param device
     * @return true if the device should be tried to start.
     * @throws DeviceAlreadyRegisteredException
     *
     * @throws DriverException
     */
    private synchronized boolean doRegister(Device device)
        throws DeviceAlreadyRegisteredException, DriverException {
        final String devID = device.getId();
        if (devices.containsKey(devID)) {
            throw new DeviceAlreadyRegisteredException(
                devID);
        }
        // Set a link to me
        device.setManager(this);
        // Find a driver if needed
        boolean shouldStart = true;
        if (device.getDriver() == null) {
            final Driver drv = findDriver(device);
            if (drv == null) {
                shouldStart = false;
            } else {
                // Connect the device to the driver
                device.setDriver(drv);
            }
        }
        // Add the device to my list
        devices.put(device.getId(), device);

        // We're done
        return shouldStart;
    }

    /**
     * Unregister a device. The device will be stopped and removed from the
     * namespace.
     *
     * @param device
     * @throws DriverException
     */
    public final void unregister(Device device) throws DriverException {
        // First stop the device if it is running
        try {
            stop(device);
            // Notify my listeners
            fireUnregisterEvent(device);
            // Actually remove it
            synchronized (this) {
                devices.remove(device.getId());
            }
        } catch (DeviceNotFoundException ex) {
            // Not found, so stop
            BootLog.debug("Device not found in unregister");
        }
    }

    /**
     * Start a given device. The device must have been registered.
     *
     * @param device
     * @throws DeviceNotFoundException The device has not been registered.
     * @throws DriverException
     */
    public final void start(Device device) throws DeviceNotFoundException,
        DriverException {
        // Make sure the device exists.
        getDevice(device.getId());
        // Start it (if needed)
        if (!device.isStarted()) {
            try {
                BootLog.debug("Starting " + device.getId());
                //new DeviceStarter(device).start(getDefaultStartTimeout());
                final StopWatch sw = new StopWatch();
                device.start();
                sw.stop();
                if (sw.isElapsedLongerThen(defaultStartTimeout)) {
                    BootLog.error("Device startup took " + sw + ": "
                        + device.getId());
                } else if (sw.isElapsedLongerThen(fastStartTimeout)) {
                    BootLog.info("Device startup took " + sw + ": "
                        + device.getId());
                }
                BootLog.debug("Started " + device.getId());
            } catch (DriverException ex) {
                BootLog.error("Cannot start " + device.getId(), ex);
                //} catch (TimeoutException ex) {
                //    BootLog.warn("Timeout in start of " + device.getId());
            } catch (Throwable ex) {
                BootLog.error("Cannot start " + device.getId(), ex);
            }
        }
    }

    /**
     * Stop a given device. The device must have been registered.
     *
     * @param device
     * @throws DeviceNotFoundException The device has not been registered.
     * @throws DriverException
     */
    public final void stop(Device device) throws DeviceNotFoundException,
        DriverException {
        // Make sure the device exists.
        getDevice(device.getId());
        // Stop it
        if (device.isStarted()) {
            BootLog.debug("Starting " + device.getId());
            device.stop(false);
            BootLog.debug("Stopped " + device.getId());
        }
    }

    /**
     * Rename a device, optionally using an autonumber postfix
     *
     * @param device
     * @param name
     * @param autonumber
     * @throws DeviceAlreadyRegisteredException
     *
     */
    public final synchronized void rename(Device device, String name,
                                          boolean autonumber) throws DeviceAlreadyRegisteredException {
        if (!device.getId().startsWith(name)) {
            String newId;
            if (autonumber) {
                int cnt = 0;
                newId = name + cnt;
                while (devices.containsKey(newId)) {
                    cnt++;
                    newId = name + cnt;
                }
            } else {
                newId = name;
            }

            if (devices.containsKey(newId)) {
                throw new DeviceAlreadyRegisteredException(
                    newId);
            }
            // Remove the old id
            if (devices.remove(device.getId()) != null) {
                // Add the new id
                devices.put(newId, device);
            }
            // Change the device id
            device.setId(newId);
        }
    }

    /**
     * Add a listener
     *
     * @param listener
     */
    public final void addListener(DeviceManagerListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Add a listener
     *
     * @param listener
     */
    public final void removeListener(DeviceManagerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Add a device listener
     *
     * @param listener
     */
    public final void addListener(DeviceListener listener) {
        synchronized (deviceListeners) {
            deviceListeners.add(listener);
        }
    }

    /**
     * Add a device listener
     *
     * @param listener
     */
    public final void removeListener(DeviceListener listener) {
        synchronized (deviceListeners) {
            deviceListeners.remove(listener);
        }
    }

    /**
     * Stop all devices
     */
    public final void stopDevices() {
        while (!devices.isEmpty()) {
            final Device dev = (Device) devices.values().iterator().next();
            try {
                BootLog.debug("Stopping device " + dev.getId());
                unregister(dev);
            } catch (DriverException ex) {
                BootLog.error("Failed to stop device " + dev.getId(), ex);
            }
        }

    }

    /**
     * Gets the system bus. The system bus is the root of all hardware busses
     * and devices connected to these busses.
     *
     * @return The system bus
     */
    public final Bus getSystemBus() {
        return systemBus;
    }

    /**
     * Find a driver from each device that has not yet has a driver connected to
     * it.
     */
    protected final void findDeviceDrivers() {
        final List<Device> devices;
        synchronized (this) {
            devices = new ArrayList<Device>(this.devices.values());
        }
        for (Device dev : devices) {
            if (dev.getDriver() == null) {
                final Driver drv = findDriver(dev);
                if (drv != null) {
                    try {
                        dev.setDriver(drv);
                        start(dev);
                    } catch (DriverException ex) {
                        BootLog.error("Cannot start " + dev.getId(), ex);
                    } catch (DeviceNotFoundException ex) {
                        // Should not happen
                        BootLog.error("Device is gone before is can be started " + dev.getId(), ex);
                    }
                }
            }
        }
    }

    /**
     * Use all device finders to find all system devices.
     */
    protected final void findDevices() throws InterruptedException {
        waitUntilExtensionsLoaded();
        final ArrayList<DeviceFinder> finders;
        synchronized (this) {
            finders = new ArrayList<DeviceFinder>(this.finders);
        }
        for (DeviceFinder finder : finders) {
            try {
                finder.findDevices(this, systemBus);
            } catch (DeviceException ex) {
                BootLog.error("Error while trying to find system devices", ex);
            } catch (RuntimeException ex) {
                BootLog
                    .error(
                        "Runtime exception while trying to find system devices",
                        ex);
            }
        }
    }

    /**
     * Search for a suitable driver for the given device.
     *
     * @param device
     * @return The first suitable driver for the given device, or a NullDriver
     *         if no suitable driver has been found.
     */
    protected final Driver findDriver(Device device) {
        synchronized (mappers) {
            for (DeviceToDriverMapper mapper : mappers) {
                final Driver drv = mapper.findDriver(device);
                if (drv != null) {
                    //Syslog.debug("Found driver for " + device);
                    return drv;
                }
            }
        }
        BootLog.debug("No driver found for " + device
            + " delaying device startup");
        return null;
    }

    /**
     * Start this manager
     *
     * @throws PluginException
     */
    public abstract void start() throws PluginException;

    protected final synchronized void loadExtensions() {
        refreshFinders(finders);
        refreshMappers(mappers);
        extensionsLoaded = true;
        notifyAll();
    }

    private synchronized void waitUntilExtensionsLoaded()
        throws IllegalMonitorStateException, InterruptedException {
        while (!extensionsLoaded) {
            wait();
        }
    }

    /**
     * Stop this manager
     *
     * @throws PluginException
     */
    public final void stop() throws PluginException {
        stopDevices();
        InitialNaming.unbind(NAME);
    }

    /**
     * Fire a deviceRegistered event to all my listeners
     *
     * @param device
     */
    protected final void fireRegisteredEvent(Device device) {
        final List<DeviceManagerListener> list;
        synchronized (this.listeners) {
            list = new ArrayList<DeviceManagerListener>(this.listeners);
        }
        final StopWatch sw = new StopWatch();
        for (DeviceManagerListener l : list) {
            sw.start();
            l.deviceRegistered(device);
            if (sw.isElapsedLongerThen(100)) {
                BootLog.error("DeviceManagerListener took " + sw
                    + " in deviceRegistered: " + l.getClass().getName());
            }
        }
    }

    /**
     * Fire a deviceUnregister event to all my listeners
     *
     * @param device
     */
    protected final void fireUnregisterEvent(Device device) {
        final List<DeviceManagerListener> list;
        synchronized (this.listeners) {
            list = new ArrayList<DeviceManagerListener>(this.listeners);
        }
        final StopWatch sw = new StopWatch();
        for (DeviceManagerListener l : list) {
            sw.start();
            l.deviceUnregister(device);
            if (sw.isElapsedLongerThen(100)) {
                BootLog.error("DeviceManagerListener took " + sw
                    + " in deviceUnregister: " + l.getClass().getName());
            }
        }
    }

    /**
     * Fire a device started event to all the device listeners
     *
     * @param device
     */
    public final void fireStartedEvent(Device device) {
        final List<DeviceListener> list;
        synchronized (this.deviceListeners) {
            list = new ArrayList<DeviceListener>(this.deviceListeners);
        }
        final StopWatch sw = new StopWatch();
        for (DeviceListener l : list) {
            sw.start();
            l.deviceStarted(device);
            if (sw.isElapsedLongerThen(100)) {
                BootLog.error("DeviceListener (in manager) took " + sw
                    + " in deviceStarted: " + l.getClass().getName());
            }
        }
    }

    /**
     * Fire a device stop event to all the device listeners
     *
     * @param device
     */
    public final void fireStopEvent(Device device) {
        final List<DeviceListener> list;
        synchronized (this.deviceListeners) {
            list = new ArrayList<DeviceListener>(this.deviceListeners);
        }
        final StopWatch sw = new StopWatch();
        for (DeviceListener l : list) {
            sw.start();
            l.deviceStop(device);
            if (sw.isElapsedLongerThen(100)) {
                BootLog.error("DeviceListener (in manager) took " + sw
                    + " in deviceStop: " + l.getClass().getName());
            }
        }
    }

    /**
     * Refresh the list of finders, based on the mappers extension-point.
     *
     * @param finders
     */
    protected abstract void refreshFinders(List<DeviceFinder> finders);

    /**
     * Refresh the list of mappers, based on the mappers extension-point.
     *
     * @param mappers
     */
    protected abstract void refreshMappers(List<DeviceToDriverMapper> mappers);

    /**
     * The root bus of every system.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    static class SystemBus extends Bus {

    }

    /**
     * Comparator used to sort DeviceToDriverMapper's.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    protected static class MapperComparator implements Comparator<DeviceToDriverMapper> {

        public static final MapperComparator INSTANCE = new MapperComparator();

        /**
         * @param o1
         * @param o2
         * @return int
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(DeviceToDriverMapper o1, DeviceToDriverMapper o2) {
            final DeviceToDriverMapper m1 = (DeviceToDriverMapper) o1;
            final DeviceToDriverMapper m2 = (DeviceToDriverMapper) o2;
            final int ml1 = m1.getMatchLevel();
            final int ml2 = m2.getMatchLevel();
            if (ml1 < ml2) {
                return -1;
            } else if (ml1 == ml2) {
                return 0;
            } else {
                return 1;
            }
        }

    }

    /**
     * Gets the default timeout for device startup.
     *
     * @return Returns the defaultStartTimeout.
     */
    public final long getDefaultStartTimeout() {
        return this.defaultStartTimeout;
    }

    /**
     * Sets the default timeout for device startup.
     *
     * @param defaultStartTimeout The defaultStartTimeout to set.
     */
    public final void setDefaultStartTimeout(long defaultStartTimeout) {
        this.defaultStartTimeout = defaultStartTimeout;
    }
}
