package org.jnode.driver.net.usb;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.Driver;
import org.jnode.driver.bus.usb.InterfaceDescriptor;
import org.jnode.driver.bus.usb.USBConfiguration;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBInterface;
import org.jnode.driver.net.usb.bluetooth.UsbBluetoothDriver;

/**
 * This class define driver finder for USB Wireless device.
 *
 * @author fabien L.
 */
public class UsbNetDeviceToDriverMapper implements DeviceToDriverMapper, UsbNetConstant {

    private static final Logger log = Logger.getLogger(UsbNetDeviceToDriverMapper.class);

    public Driver findDriver(Device device) {

        if (!(device instanceof USBDevice)) {
            return null;
        }
        final USBDevice dev = (USBDevice) device;
        final USBConfiguration conf = dev.getConfiguration(0);
        final USBInterface intf = conf.getInterface(0);
        final InterfaceDescriptor descr = intf.getDescriptor();
        if (descr.getInterfaceClass() != USB_CLASS_WIRELESS) {
            return null;
        }
        log.debug("Found USB wireless device.");
        if (descr.getInterfaceSubClass() == US_SC_RF) {
            log.debug("Subclass " + descr.getInterfaceSubClass());
            return new UsbBluetoothDriver();
        }
        return null;
    }

    public int getMatchLevel() {
        return MATCH_DEVCLASS;
    }

}
