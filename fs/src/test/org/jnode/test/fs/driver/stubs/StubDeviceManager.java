/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.test.fs.driver.stubs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.jnode.driver.AbstractDeviceManager;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.DriverException;

/**
 * This a (modified) copy of DefaultDeviceManager
 *
 * @author epr
 */
public class StubDeviceManager extends AbstractDeviceManager {
    public static final Logger log = Logger.getLogger(StubDeviceManager.class);

    public static final StubDeviceManager INSTANCE = new StubDeviceManager();

    private List<DeviceFinder> finders = new ArrayList<DeviceFinder>();

    private List<DeviceToDriverMapper> mappers = new ArrayList<DeviceToDriverMapper>();

    private StubDeviceManager() {
        super("");
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
    public final void start() {
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

    /**
     * Refresh the list of finders, based on the mappers extension-point.
     *
     * @param finders
     */
    protected final void refreshFinders(List<DeviceFinder> finders) {
        log.info("refreshFinders");
        finders.clear();
        finders.addAll(this.finders);
    }

    /**
     * Refresh the list of mappers, based on the mappers extension-point.
     *
     * @param mappers
     */
    protected final void refreshMappers(List<DeviceToDriverMapper> mappers) {
        log.info("refreshMappers");
        mappers.clear();
        mappers.addAll(this.mappers);

        // Now sort them
        Collections.sort(mappers, MapperComparator.INSTANCE);
    }
}
