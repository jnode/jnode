/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.test.fs.driver;

import java.io.IOException;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.jmock.MockObjectTestCase;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceFinder;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.test.fs.driver.context.RamDiskDriverContext;
import org.jnode.test.fs.driver.stubs.StubDeviceManager;
import org.jnode.test.support.Context;
import org.jnode.test.support.TestConfig;

public abstract class BlockDeviceAPIContext extends Context {
    protected static final Logger log = Logger
        .getLogger(BlockDeviceAPIContext.class);

    private BlockDeviceAPI api;

    private BlockDeviceAPIContext parentContext;

    private Partition[] partitions;

    private String name;

    public BlockDeviceAPIContext(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public void init(TestConfig config, MockObjectTestCase testCase)
        throws Exception {
        BlockDeviceAPITestConfig cfg = (BlockDeviceAPITestConfig) config;
        partitions = cfg.getPartitions();
    }

    protected void init(BlockDeviceAPIContext parentContext,
                        BlockDeviceAPI api, Device device) {
        this.api = api;
        this.parentContext = parentContext;

        log.info("api=" + api + " device=" + device);
        if ((device != null) && (api instanceof Driver)) {
            Driver driver = (Driver) api;
            try {
                boolean registered = false;

                try {
                    StubDeviceManager.INSTANCE.getDevice(device.getId());
                    registered = true;
                } catch (DeviceNotFoundException e) {
                    registered = false;
                }

                device.setDriver(driver);

                if (!registered) {
                    StubDeviceManager.INSTANCE.register(device);
                }
            } catch (DriverException e) {
                log.error("Error while starting device " + driver.getDevice(),
                    e);
            } catch (DeviceAlreadyRegisteredException e) {
                log.error("Error while starting device " + driver.getDevice(),
                    e);
            }
        }

        log.info(api.getClass().getName() + " initialized");
    }

    public void destroy() {
        try {
            if (api != null) {
                api.flush();
            }
        } catch (IOException e) {
            log.error("can't flush " + api.getClass().getName(), e);
        }

        if (api instanceof Driver) {
            Driver driver = (Driver) api;
            Device device = driver.getDevice();

            if (device != null) {
                try {
                    StubDeviceManager.INSTANCE.stop(device);
                    StubDeviceManager.INSTANCE.unregister(device);
                } catch (DriverException e) {
                    log.error("Error while stopping device "
                        + driver.getDevice(), e);
                } catch (DeviceNotFoundException e) {
                    log.error("Error while stopping device "
                        + driver.getDevice(), e);
                }
            }
        }

        if (parentContext != null) {
            parentContext.destroy();
        }

        unregisterDevices();

        try {
            destroyImpl();
        } catch (Exception e) {
            log.error("Error while freeing BlockDeviceAPIContext", e);
        }
    }

    public final BlockDeviceAPI getApi() {
        return api;
    }

    protected Driver findDriver(DeviceFinder finder, String devName) {
        try {
            finder.findDevices(StubDeviceManager.INSTANCE,
                StubDeviceManager.INSTANCE.getSystemBus());
        } catch (DeviceAlreadyRegisteredException e) {
            log.warn(e);
        } catch (DeviceException e) {
            log.error(e);
        }

        try {
            Device dev = StubDeviceManager.INSTANCE.getDevice(devName);
            log.debug("dev=" + dev);
            log
                .debug("driver="
                    + (dev == null ? "null" : "" + dev.getDriver()));
            return dev.getDriver();
        } catch (DeviceNotFoundException e) {
            log.fatal("can't find " + devName, e);
            return null;
        }
    }

    protected void destroyImpl() throws Exception {
    }

    private void unregisterDevices() {
        // Collection devs =
        // StubDeviceManager.INSTANCE.getDevicesByAPI(RemovableDeviceAPI.class);
        Collection<Device> devs = StubDeviceManager.INSTANCE.getDevices();
        for (Device device : devs) {
            try {
                StubDeviceManager.INSTANCE.unregister(device);
            } catch (DriverException e) {
                log.error("can't unregister " + device.getClass().getName(), e);
            }
        }
    }

    protected BlockDeviceAPIContext createParentBlockDeviceAPI() {
        return new RamDiskDriverContext();
    }

    public Partition[] getPartitions() {
        return partitions;
    }
}
