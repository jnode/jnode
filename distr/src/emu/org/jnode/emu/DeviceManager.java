/*
 * $Id$
 */
package org.jnode.emu;

import org.jnode.driver.*;
import org.apache.log4j.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Levente Sántha
*/
public class DeviceManager extends AbstractDeviceManager {
    public static final Logger log = Logger.getLogger(DeviceManager.class);

    public static final DeviceManager INSTANCE = new DeviceManager();

    private List<DeviceFinder> finders = new ArrayList<DeviceFinder>();

    private List<DeviceToDriverMapper> mappers = new ArrayList<DeviceToDriverMapper>();

    private DeviceManager() {
    }

    public void removeAll() {
        finders.clear();
        mappers.clear();

        for (Device device : getDevices()) {
            try {
                unregister(device);
            } catch (DriverException e) {
                log.error("can't unregister " + device);
            }
        }
    }

    public void add(DeviceFinder finder, DeviceToDriverMapper mapper) {
        boolean doStart = false;

        if (!finders.contains(finder)) {
            finders.add(finder);
            doStart = true;
        }

        if (!mappers.contains(mapper)) {
            mappers.add(mapper);
            doStart = true;
        }

        if (doStart) {
            start();
        }
    }

    /**
     * Start this manager
     */
    final public void start() {
        // Thread thread = new Thread()
        // {
        // public void run()
        // {
        log.debug("Loading extensions ...");
        loadExtensions();
        log.debug("Extensions loaded !");
        // }
        // };
        // thread.start();

        try {
            // must be called before findDeviceDrivers
            log.debug("findDevices ...");
            findDevices();

            log.debug("findDeviceDrivers ...");
            findDeviceDrivers();

            log.debug("StubDeviceManager initialized !");
        } catch (InterruptedException e) {
            log.fatal("can't find devices", e);
        }
    }

    protected final void refreshFinders(List<DeviceFinder> finders) {
        log.info("refreshFinders");
        finders.clear();
        finders.addAll(this.finders);
    }

    protected final void refreshMappers(List<DeviceToDriverMapper> mappers) {
        log.info("refreshMappers");
        mappers.clear();
        mappers.addAll(this.mappers);

        // Now sort them
        Collections.sort(mappers, MapperComparator.INSTANCE);
    }
}
