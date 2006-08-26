package org.jnode.driver.net.usb.bluetooth;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceToDriverMapper;
import org.jnode.driver.Driver;
import org.jnode.driver.bus.usb.InterfaceDescriptor;
import org.jnode.driver.bus.usb.USBConfiguration;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBInterface;

public class UsbBluetoothDeviceToDriverMapper implements DeviceToDriverMapper, UsbNetConstant {
	
	//private static final Logger log = Logger.getLogger(UsbBluetoothDeviceToDriverMapper.class);
	
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
		//log.debug("Found usb wireless : " + descr);
		if(descr.getInterfaceSubClass() == US_SC_RF){
			//log.debug("Found driver for subclass " + descr.getInterfaceSubClass());
			return new UsbBluetoothDriver();
		}
		return null;
	}

	public int getMatchLevel() {
		return MATCH_DEVCLASS;
	}

}
