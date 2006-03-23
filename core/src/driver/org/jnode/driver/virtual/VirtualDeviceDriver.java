package org.jnode.driver.virtual;

import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.DeviceAlreadyRegisteredException;

/**
 * @author Levente S\u00e1ntha
 */
public class VirtualDeviceDriver extends Driver {
    /**
     * Start the device.
     *
     * @throws org.jnode.driver.DriverException
     *
     */
    protected void startDevice() throws DriverException {
        try {
            VirtualDevice device = (VirtualDevice)getDevice();
            device.getManager().rename(device, device.getInitialName(), true);
        } catch (DeviceAlreadyRegisteredException ex) {
            throw new DriverException(ex);
        }
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
