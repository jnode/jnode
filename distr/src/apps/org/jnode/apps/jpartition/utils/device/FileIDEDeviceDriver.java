package org.jnode.apps.jpartition.utils.device;

import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.bus.ide.IDEDevice;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
public class FileIDEDeviceDriver extends Driver {
    /**
     * Start the device.
     *
     * @throws org.jnode.driver.DriverException
     *
     */
    protected void startDevice() throws DriverException {
    }

    /**
     * Stop the device.
     *
     * @throws org.jnode.driver.DriverException
     *
     */
    protected void stopDevice() throws DriverException {

    }
}
