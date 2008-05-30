package org.jnode.driver.virtual;

import org.jnode.driver.DeviceException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;

/**
 * @author Levente S\u00e1ntha
 */
public class VirtualDeviceFactory {
    public static VirtualDevice createDevice(String name) throws DeviceException {
        try {
            final VirtualDevice dev = new VirtualDevice(name);
            dev.setDriver(new VirtualDeviceDriver());
            final DeviceManager dm = DeviceUtils.getDeviceManager();
            dm.register(dev);
            return dev;
        } catch (Exception x) {
            throw new DeviceException(x);
        }
    }
}
