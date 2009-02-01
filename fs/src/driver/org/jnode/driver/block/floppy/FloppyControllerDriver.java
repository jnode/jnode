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
 
package org.jnode.driver.block.floppy;

import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import javax.naming.NamingException;
import org.apache.log4j.Logger;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.floppy.support.FloppyDeviceFactory;
import org.jnode.driver.block.floppy.support.FloppyDriverUtils;
import org.jnode.naming.InitialNaming;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.TimeoutException;

/**
 * @author epr
 */
public class FloppyControllerDriver extends Driver {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(FloppyControllerDriver.class);
    /**
     * The controller
     */
    private FDC fdc;
    /**
     * The global devicemanager
     */
    private DeviceManager devMan;
    /**
     * All floppy drive devices controlled by this controller
     */
    private final ArrayList<FloppyDevice> devices = new ArrayList<FloppyDevice>();
    private FloppyControllerBus bus;

    /**
     * Start the device.
     *
     * @throws DriverException
     */
    protected void startDevice()
        throws DriverException {
        try {
            devices.clear();
            fdc = FloppyDriverUtils.getFloppyDeviceFactory().createFDC(getDevice());
            bus = new FloppyControllerBus(this);
            registerDevices();
        } catch (FloppyException ex) {
            throw new DriverException("Cannot register drives", ex);
        } catch (ResourceNotFreeException ex) {
            throw new DriverException("Cannot claim all resources", ex);
        } catch (NamingException ex) {
            throw new DriverException("Cannot obtain device factory", ex);
        }
    }

    /**
     * Stop the device.
     *
     * @throws DriverException
     */
    protected void stopDevice()
        throws DriverException {
        unregisterDevices();
        if (fdc != null) {
            fdc.release();
            fdc = null;
        }
        devices.clear();
        devMan = null;
    }

    /**
     * Register all existing floppy drives
     *
     * @throws FloppyException
     * @throws DriverException
     * @throws NamingException
     */
    protected void registerDevices()
        throws FloppyException, DriverException, NamingException {
        devMan = InitialNaming.lookup(DeviceManager.NAME);
        final int max = fdc.getDriveCount();
        final FloppyDeviceFactory factory = FloppyDriverUtils.getFloppyDeviceFactory();
        for (int i = 0; i < max; i++) {
            final FloppyDriveParameters dp = fdc.getDriveParams(i);
            log.debug("For fd" + i + ", found CMOS type " + dp.getCmosType());

            if (dp.isPresent()) {
                try {
                    final FloppyDevice fd = factory.createDevice(bus, i, dp);
                    fd.setDriver(new FloppyDriver());
                    devMan.register(fd);
                    devices.add(fd);
                    log.debug("Registered fd" + i);
                } catch (DeviceAlreadyRegisteredException ex) {
                    log.error("Cannot register fd" + i, ex);
                }
            }
        }
    }

    /**
     * Unregister all floppy drive devices.
     *
     * @throws DriverException
     */
    protected void unregisterDevices()
        throws DriverException {
        for (FloppyDevice fd : devices) {
            devMan.unregister(fd);
        }
    }

    /**
     * Has the disk changed since the last command?
     *
     * @param drive
     * @param resetFlag
     * @return boolean
     */
    protected final boolean diskChanged(int drive, boolean resetFlag) {
        return fdc.diskChanged(drive, resetFlag);
    }

    /**
     * Add the given command to the command queue and wait till the command
     * has finished.
     *
     * @param cmd
     * @param timeout
     * @throws ClosedByInterruptException
     * @throws TimeoutException
     */
    protected final void executeAndWait(FloppyCommand cmd, long timeout)
        throws ClosedByInterruptException, TimeoutException {
        try {
            fdc.executeAndWait(cmd, timeout);
        } catch (InterruptedException ex) {
            throw new ClosedByInterruptException();
        }
    }

    /**
     * Reset the controller
     */
    protected final void resetFDC() {
        log.debug("Reset FDC");
        fdc.reset();
    }
}
