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
 
package org.jnode.driver.bus.ide;

import java.util.ArrayList;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.naming.InitialNaming;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.TimeoutException;
import org.jnode.work.Work;
import org.jnode.work.WorkUtils;

/**
 * @author epr
 */
public class DefaultIDEControllerDriver extends Driver implements IDEControllerAPI {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(DefaultIDEControllerDriver.class);

    /**
     * The actual worker
     */
    private final IDEBus[] ideProcessors;

    /**
     * The actual IDE devices found
     */
    private final ArrayList<IDEDevice> devices = new ArrayList<IDEDevice>();

    private DeviceManager devMan;

    /**
     * Create a new instance
     */
    public DefaultIDEControllerDriver() {
        final int max = IDEConstants.IDE_NR_TASKFILES;
        this.ideProcessors = new IDEBus[max];
    }

    /**
     * Add the given command to the queue of commands to be executed and wait
     * for the command to finish.
     *
     * @param command
     */
    protected final void executeAndWait(IDECommand command, long timeout)
        throws InterruptedException, TimeoutException {
        final int idx = command.isPrimary() ? 0 : 1;
        ideProcessors[idx].executeAndWait(command, timeout);
    }

    /**
     * Probe for the existence of a given IDE device.
     *
     * @param taskfile The taskfile to probe. [0..1]
     * @param master
     */
    protected IDEDriveDescriptor probe(int taskfile, boolean master)
        throws InterruptedException {
        return ideProcessors[taskfile].probe(master);
    }

    protected void registerDevices() throws IDEException, DriverException {
        log.debug("Probing IDE devices");
        try {
            devMan = InitialNaming.lookup(DeviceManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new IDEException("Cannot find device manager", ex);
        }
        final IDEDriveDescriptor[] devs = new IDEDriveDescriptor[4];

        try {
            devs[0] = probe(0, true);
            devs[1] = probe(0, false);
            devs[2] = probe(1, true);
            devs[3] = probe(1, false);
        } catch (InterruptedException ex) {
            throw new IDEException("Probe interrupted");
        }

        log.debug("After probe");

        int count = 0;
        IDEDeviceFactory factory;
        try {
            factory = IDEDriverUtils.getIDEDeviceFactory();
        } catch (NamingException ex) {
            throw new DriverException(ex);
        }
        for (int i = 0; i < devs.length; i++) {
            final IDEDriveDescriptor descr = devs[i];
            if (descr != null) {
                final String name = "hd" + ((char) ('a' + i));
                final IDEDevice dev = factory.createIDEDevice(ideProcessors[i / 2],
                    ((i / 2) == 0), ((i % 2) == 0), name, descr, this);
                try {
                    devMan.register(dev);
                    devices.add(dev);
                    log.info(name + "=" + descr.getModel());
                    count++;
                } catch (DeviceAlreadyRegisteredException ex) {
                    log.error("Cannot reqister " + name, ex);
                    //throw new IDEException("Error in register device", ex);
                } catch (DriverException ex) {
                    log.error("Cannot reqister " + name, ex);
                    //throw new IDEException("Error in register device", ex);
                }
            }
        }

        log.info("Found " + count + " IDE devices");
    }

    protected void unregisterDevices() throws DriverException {
        for (IDEDevice dev : devices) {
            devMan.unregister(dev);
        }
    }

    /**
     * Create and start the structures required by this driver.
     */
    private void startDriver() throws ResourceNotFreeException, DriverException {
        final Device dev = getDevice();
        dev.registerAPI(IDEControllerAPI.class, this);
        IDEDeviceFactory factory;
        try {
            factory = IDEDriverUtils.getIDEDeviceFactory();
        } catch (NamingException ex) {
            throw new DriverException(ex);
        }
        this.ideProcessors[0] = factory.createIDEBus(dev, true);
        this.ideProcessors[1] = factory.createIDEBus(dev, false);
    }

    /**
     * Stop and release the structures required by this driver.
     */
    private void stopDriver() {
        final Device dev = getDevice();
        final int max = ideProcessors.length;
        for (int i = 0; i < max; i++) {
            ideProcessors[i].stop();
            ideProcessors[i] = null;
        }
        dev.unregisterAPI(IDEControllerAPI.class);
    }

    /**
     * Start the IDE controller device.
     *
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected void startDevice() throws DriverException {
        try {
            final Device dev = getDevice();
            dev.getManager().rename(dev, IDEControllerAPI.DEVICE_PREFIX, true);
            startDriver();
            WorkUtils.add(new Work("IDE.registerDevices") {
                public void execute() {
                    try {
                        registerDevices();
                    } catch (IDEException ex) {
                        log.error("Error starting IDE devices", ex);
                    } catch (DriverException ex) {
                        log.error("Error starting IDE devices", ex);
                    }
                }
            });
        } catch (ResourceNotFreeException ex) {
            throw new DriverException(ex);
        } catch (DeviceAlreadyRegisteredException ex) {
            throw new DriverException(ex);
        }
    }

    /**
     * Stop the IDE controller device.
     *
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected void stopDevice() throws DriverException {
        unregisterDevices();
        stopDriver();
    }
}
