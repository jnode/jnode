/*
 * $Id$
 */
package org.jnode.driver;

import gnu.java.security.action.GetPropertyAction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
import org.jnode.util.StopWatch;
import org.jnode.work.Work;
import org.jnode.work.WorkUtils;

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

    private boolean extensionsLoaded = false;
    
    private long defaultStartTimeout = 10000;

    private long fastStartTimeout = 1000;

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
    public void register(Device device)
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
     * @throws DeviceAlreadyRegisteredException
     * @throws DriverException
     * @return true if the device should be tried to start.
     */
    private synchronized final boolean doRegister(Device device)
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
    public void unregister(Device device) throws DriverException {
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
     * @throws DeviceNotFoundException
     *             The device has not been registered.
     * @throws DriverException
     */
    public void start(Device device) throws DeviceNotFoundException,
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
     * @throws DeviceNotFoundException
     *             The device has not been registered.
     * @throws DriverException
     */
    public void stop(Device device) throws DeviceNotFoundException,
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
    private final void findDeviceDrivers() {
        final List devices;
        synchronized (this) {
            devices = new ArrayList(this.devices.values());
        }
        for (Iterator i = devices.iterator(); i.hasNext();) {
            final Device dev = (Device) i.next();
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
    protected void findDevices() throws InterruptedException {
        waitUntilExtensionsLoaded();
        final ArrayList finders;
        synchronized( this ) {
            finders = new ArrayList( this.finders );
        }
        for( Iterator i = finders.iterator(); i.hasNext(); ) {
//            final DeviceFinder finder = (DeviceFinder) i.next();//this fails sometimes on my machine (5% of the time.)
            //I can't find out why, the code below does a ClassCastTest.  Weird.
            Object next = i.next();
            if( next instanceof DeviceFinder ) {
                try {
                    DeviceFinder finder = (DeviceFinder)next;
                    finder.findDevices( this, systemBus );
                }
                catch( DeviceException ex ) {
                    BootLog.error( "Error while trying to find system devices", ex );
                }
                catch( RuntimeException ex ) {
                    BootLog.error( "Runtime exception while trying to find system devices", ex );
                }
            }
            else{
                String errorStr="Instance in DeviceFinder list of wrong type: "+next.getClass();
                System.err.println( errorStr );
                BootLog.error(errorStr );
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
            WorkUtils.add(new Work("Start device manager") {
                public void execute() {
                    loadExtensions();
                }
                });
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
    }
    
    private synchronized void loadExtensions() {
        refreshFinders();
        refreshMappers();  
        extensionsLoaded = true;
        notifyAll();
    }

    private synchronized void waitUntilExtensionsLoaded() throws IllegalMonitorStateException, InterruptedException {
        while (!extensionsLoaded) {
            wait();
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
    protected final void fireRegisteredEvent(Device device) {
        final List list;
        synchronized (this.listeners) {
            list = new ArrayList(this.listeners);
        }
        final StopWatch sw = new StopWatch();
        for (Iterator i = list.iterator(); i.hasNext();) {
            final DeviceManagerListener l = (DeviceManagerListener) i.next();
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
        final List list;
        synchronized (this.listeners) {
            list = new ArrayList(this.listeners);
        }
        final StopWatch sw = new StopWatch();
        for (Iterator i = list.iterator(); i.hasNext();) {
            final DeviceManagerListener l = (DeviceManagerListener) i.next();
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
    protected void fireStartedEvent(Device device) {
        final List list;
        synchronized (this.deviceListeners) {
            list = new ArrayList(this.deviceListeners);
        }
        final StopWatch sw = new StopWatch();
        for (Iterator i = list.iterator(); i.hasNext();) {
            final DeviceListener l = (DeviceListener) i.next();
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
    protected void fireStopEvent(Device device) {
        final List list;
        synchronized (this.deviceListeners) {
            list = new ArrayList(this.deviceListeners);
        }
        final StopWatch sw = new StopWatch();
        for (Iterator i = list.iterator(); i.hasNext();) {
            final DeviceListener l = (DeviceListener) i.next();
            sw.start();
            l.deviceStop(device);
            if (sw.isElapsedLongerThen(100)) {
                BootLog.error("DeviceListener (in manager) took " + sw
                        + " in deviceStop: " + l.getClass().getName());
            }
        }
    }

    /**
     * @param point
     * @param extension
     */
    public final void extensionAdded(ExtensionPoint point, Extension extension) {
        loadExtensions();
        findDeviceDrivers();
    }

    /**
     * @param point
     * @param extension
     */
    public final void extensionRemoved(ExtensionPoint point, Extension extension) {
        loadExtensions();
    }

    /**
     * Refresh the list of finders, based on the mappers extension-point.
     */
    private final void refreshFinders() {
        finders.clear();
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
    private final void refreshMappers() {
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
                final DeviceToDriverMapper mapper = newMapperInstance(cls, element);
                mappers.add(mapper);
            } catch (ClassNotFoundException ex) {
                BootLog.error("Cannot find mapper class " + className, ex);
            } catch (IllegalAccessException ex) {
                BootLog.error("Cannot access mapper class " + className, ex);
            } catch (InstantiationException ex) {
                BootLog.error("Cannot instantiate mapper class " + className, ex);
            } catch (ClassCastException ex) {
                BootLog
                        .error("Mapper class "
                                + className
                                + " does not implement the DeviceToDriverMapper interface");
            }
        } else {
            BootLog.error("class attribute required in mapper");
        }
    }
    
    /**
     * Instantiate the device to driver mapper.
     * First look for a constructor with a ConfigurationElement parameter,
     * if not found, use the default constructor.
     * 
     * @param cls
     * @param element
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private DeviceToDriverMapper newMapperInstance(Class cls, ConfigurationElement element) throws InstantiationException, IllegalAccessException {
        try {
            final Constructor c = cls.getConstructor(new Class[] { ConfigurationElement.class });
            try {
                return (DeviceToDriverMapper)c.newInstance(new Object[] { element });
            } catch (InvocationTargetException ex1) {
                final InstantiationException ie = new InstantiationException();
                ie.initCause(ex1.getTargetException());
                throw ie;
            }
        } catch (NoSuchMethodException ex) {
            return (DeviceToDriverMapper) cls.newInstance();
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