/*
 * $Id$
 */
package org.jnode.driver;

import gnu.java.security.actions.GetPropertyAction;

import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.ConfigurationElement;
import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.ExtensionPointListener;
import org.jnode.plugin.PluginException;
import org.jnode.system.BootLog;
import org.jnode.util.TimeoutException;

/**
 * Default device manager.
 * 
 * @author epr
 */
public class DefaultDeviceManager implements DeviceManager,
        ExtensionPointListener {

    /** All registered devices */
    private final Map devices = new HashMap();

    /** All registered device to driver mappers */
    private final List mappers = new ArrayList();

    /** All registered device finders */
    private final List finders = new ArrayList();

    /** All listeners to my events */
    private final List listeners = new LinkedList();

    /** All listeners to device events */
    private final List deviceListeners = new LinkedList();

    /** finder extension-point */
    private final ExtensionPoint findersEP;

    /** mappers extension-point */
    private final ExtensionPoint mappersEP;

    /** The system bus */
    private final Bus systemBus;

    /** The JNode command line */
    private final String cmdLine;

    private long defaultStartTimeout = 10000;

    /**
     * Create a new instance
     * 
     * @param findersEP
     * @param mappersEP
     */
    public DefaultDeviceManager(ExtensionPoint findersEP,
            ExtensionPoint mappersEP) {
        if (findersEP == null) { throw new IllegalArgumentException(
                "finders extension-point cannot be null"); }
        if (mappersEP == null) { throw new IllegalArgumentException(
                "mappers extension-point cannot be null"); }
        cmdLine = (String) AccessController.doPrivileged(new GetPropertyAction(
                "jnode.cmdline", ""));
        this.systemBus = new SystemBus();
        this.findersEP = findersEP;
        this.mappersEP = mappersEP;
        findersEP.addListener(this);
        mappersEP.addListener(this);
        refreshFinders();
        refreshMappers();
    }

    /**
     * Returns a collection of all known devices. The collection is not
     * modifiable, but the underlying collection can change, so be aware of
     * exceptions in iterators.
     * 
     * @return All known devices.
     */
    public Collection getDevices() {
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
    public Collection getDevicesByAPI(Class apiClass) {
        final Vector result = new Vector();
        for (Iterator i = devices.values().iterator(); i.hasNext();) {
            final Device dev = (Device) i.next();
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
     * @throws DeviceNotFoundException
     *             No device with the given id was found.
     */
    public Device getDevice(String id) throws DeviceNotFoundException {
        Device device;
        device = (Device) devices.get(id);
        if (device == null) { throw new DeviceNotFoundException(id); }
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
     * @throws DriverException
     */
    public synchronized void register(Device device)
            throws DeviceAlreadyRegisteredException, DriverException {
        final String devID = device.getId();
        if (devices.containsKey(devID)) { throw new DeviceAlreadyRegisteredException(
                devID); }
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
        // Test for no<id> on the command line
        if (cmdLine.indexOf("no" + device.getId()) >= 0) {
            BootLog.info("Blocking the start of " + device.getId());
            shouldStart = false;
        }
        // Add the device to my list
        devices.put(device.getId(), device);
        // Notify my listeners
        fireRegisteredEvent(device);
        if (shouldStart) {
            // Try to start the device
            try {
                BootLog.debug("Starting " + device.getId());
                //new DeviceStarter(device).start(getDefaultStartTimeout());
                device.start();
                BootLog.debug("Started " + device.getId());
            } catch (DriverException ex) {
                BootLog.error("Cannot start " + device.getId(), ex);
            } catch (Throwable ex) {
                BootLog.error("Cannot start " + device.getId(), ex);
            }
        }
    }

    /**
     * Unregister a device. The device will be stopped and removed from the
     * namespace.
     * 
     * @param device
     * @throws DriverException
     */
    public synchronized void unregister(Device device) throws DriverException {
        // First stop the device if it is running
        if (device.isStarted()) {
            device.stop();
        }
        // Notify my listeners
        fireUnregisterEvent(device);
        // Actually remove it
        devices.remove(device.getId());
    }

    /**
     * Rename a device, optionally using an autonumber postfix
     * 
     * @param device
     * @param name
     * @param autonumber
     * @throws DeviceAlreadyRegisteredException
     */
    public synchronized void rename(Device device, String name,
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

            if (devices.containsKey(newId)) { throw new DeviceAlreadyRegisteredException(
                    newId); }
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
    public void addListener(DeviceManagerListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Add a listener
     * 
     * @param listener
     */
    public void removeListener(DeviceManagerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Add a device listener
     * 
     * @param listener
     */
    public void addListener(DeviceListener listener) {
        synchronized (deviceListeners) {
            deviceListeners.add(listener);
        }
    }

    /**
     * Add a device listener
     * 
     * @param listener
     */
    public void removeListener(DeviceListener listener) {
        synchronized (deviceListeners) {
            deviceListeners.remove(listener);
        }
    }

    /**
     * Stop all devices
     */
    public void stopDevices() {
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
    public Bus getSystemBus() {
        return systemBus;
    }

    /**
     * Find a driver from each device that has not yet has a driver connected to
     * it.
     */
    protected void findDeviceDrivers() {
        for (Iterator i = devices.values().iterator(); i.hasNext();) {
            final Device dev = (Device) i.next();
            if (dev.getDriver() == null) {
                final Driver drv = findDriver(dev);
                if (drv != null) {
                    try {
                        dev.setDriver(drv);
                        final DeviceStarter starter = new DeviceStarter(dev);
                        starter.start(defaultStartTimeout);
                    } catch (DriverException ex) {
                        BootLog.error("Cannot start " + dev.getId(), ex);
                    } catch (TimeoutException ex) {
                        BootLog.error("Device " + dev.getId()
                                + " failed to startup in time");
                    }
                }
            }
        }
    }

    /**
     * Use all device finders to find all system devices.
     */
    protected void findDevices() {
        for (Iterator i = finders.iterator(); i.hasNext();) {
            final DeviceFinder finder = (DeviceFinder) i.next();
            try {
                finder.findDevices(this, systemBus);
            } catch (DeviceException ex) {
                BootLog.error("Error while trying to find system devices", ex);
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
    protected Driver findDriver(Device device) {
        synchronized (mappers) {
            for (Iterator i = mappers.iterator(); i.hasNext();) {
                final DeviceToDriverMapper mapper;
                mapper = (DeviceToDriverMapper) i.next();
                Driver drv = mapper.findDriver(device);
                if (drv != null) {
                //Syslog.debug("Found driver for " + device);
                return drv; }
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
    public void start() throws PluginException {
        try {
            InitialNaming.bind(NAME, this);
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * Stop this manager
     * 
     * @throws PluginException
     */
    public void stop() throws PluginException {
        stopDevices();
        InitialNaming.unbind(NAME);
    }

    /**
     * Fire a deviceRegistered event to all my listeners
     * 
     * @param device
     */
    protected void fireRegisteredEvent(Device device) {
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            final DeviceManagerListener l = (DeviceManagerListener) i.next();
            l.deviceRegistered(device);
        }
    }

    /**
     * Fire a deviceUnregister event to all my listeners
     * 
     * @param device
     */
    protected void fireUnregisterEvent(Device device) {
        for (Iterator i = listeners.iterator(); i.hasNext();) {
            final DeviceManagerListener l = (DeviceManagerListener) i.next();
            l.deviceUnregister(device);
        }
    }

    /**
     * Fire a device started event to all the device listeners
     * 
     * @param device
     */
    protected void fireStartedEvent(Device device) {
        for (Iterator i = deviceListeners.iterator(); i.hasNext();) {
            final DeviceListener l = (DeviceListener) i.next();
            l.deviceStarted(device);
        }
    }

    /**
     * Fire a device stop event to all the device listeners
     * 
     * @param device
     */
    protected void fireStopEvent(Device device) {
        for (Iterator i = deviceListeners.iterator(); i.hasNext();) {
            final DeviceListener l = (DeviceListener) i.next();
            l.deviceStop(device);
        }
    }

    /**
     * @param point
     * @param extension
     */
    public void extensionAdded(ExtensionPoint point, Extension extension) {
        refreshFinders();
        refreshMappers();
        findDeviceDrivers();
    }

    /**
     * @param point
     * @param extension
     */
    public void extensionRemoved(ExtensionPoint point, Extension extension) {
        refreshFinders();
        refreshMappers();
    }

    /**
     * Refresh the list of finders, based on the mappers extension-point.
     */
    protected void refreshFinders() {
        mappers.clear();
        final Extension[] extensions = findersEP.getExtensions();
        BootLog.debug("Found " + extensions.length + " device finders");

        for (int i = 0; i < extensions.length; i++) {
            final Extension ext = extensions[ i];
            final ConfigurationElement[] elements = ext
                    .getConfigurationElements();
            for (int j = 0; j < elements.length; j++) {
                configureFinder(finders, elements[ j]);
            }
        }
    }

    /**
     * Refresh the list of mappers, based on the mappers extension-point.
     */
    protected void refreshMappers() {
        mappers.clear();
        final Extension[] extensions = mappersEP.getExtensions();
        BootLog.debug("Found " + extensions.length + " mapper extensions");

        // First load all mappers
        for (int i = 0; i < extensions.length; i++) {
            final Extension ext = extensions[ i];
            final ConfigurationElement[] elements = ext
                    .getConfigurationElements();
            for (int j = 0; j < elements.length; j++) {
                configureMapper(mappers, elements[ j]);
            }
        }

        // Now sort them
        Collections.sort(mappers, MapperComparator.INSTANCE);
    }

    /**
     * Configure a finder for a single finder configuration element and add the
     * new finder to the given list.
     * 
     * @param finders
     * @param element
     */
    private void configureFinder(List finders, ConfigurationElement element) {
        final String className = element.getAttribute("class");
        BootLog.debug("Configure finder: class=" + className);
        if (className != null) {
            try {
                final Class cls = Thread.currentThread()
                        .getContextClassLoader().loadClass(className);
                final DeviceFinder finder = (DeviceFinder) cls.newInstance();
                finders.add(finder);
            } catch (ClassNotFoundException ex) {
                BootLog.error("Cannot find finder class " + className);
            } catch (IllegalAccessException ex) {
                BootLog.error("Cannot access finder class " + className);
            } catch (InstantiationException ex) {
                BootLog.error("Cannot instantiate finder class " + className);
            } catch (ClassCastException ex) {
                BootLog.error("Finder class " + className
                        + " does not implement the DeviceFinder interface");
            }
        }
    }

    /**
     * Configure a mapper for a single mapper configuration element and add the
     * new mapper to the given list.
     * 
     * @param mappers
     * @param element
     */
    private void configureMapper(List mappers, ConfigurationElement element) {
        final String className = element.getAttribute("class");
        BootLog.debug("Configure mapper: class=" + className);
        if (className != null) {
            try {
                final Class cls = Thread.currentThread()
                        .getContextClassLoader().loadClass(className);
                final DeviceToDriverMapper mapper = (DeviceToDriverMapper) cls
                        .newInstance();
                mappers.add(mapper);
            } catch (ClassNotFoundException ex) {
                BootLog.error("Cannot find mapper class " + className);
            } catch (IllegalAccessException ex) {
                BootLog.error("Cannot access mapper class " + className);
            } catch (InstantiationException ex) {
                BootLog.error("Cannot instantiate mapper class " + className);
            } catch (ClassCastException ex) {
                BootLog
                        .error("Mapper class "
                                + className
                                + " does not implement the DeviceToDriverMapper interface");
            }
        }
    }

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
    static class MapperComparator implements Comparator {

        public static final MapperComparator INSTANCE = new MapperComparator();

        /**
         * @param o1
         * @param o2
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         * @return int
         */
        public int compare(Object o1, Object o2) {
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
     * @param defaultStartTimeout
     *            The defaultStartTimeout to set.
     */
    public final void setDefaultStartTimeout(long defaultStartTimeout) {
        this.defaultStartTimeout = defaultStartTimeout;
    }
}