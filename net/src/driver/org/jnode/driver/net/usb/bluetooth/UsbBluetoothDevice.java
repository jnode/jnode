package org.jnode.driver.net.usb.bluetooth;

import org.jnode.driver.bus.usb.SetupPacket;
import org.jnode.driver.bus.usb.USBConstants;
import org.jnode.driver.bus.usb.USBControlPipe;
import org.jnode.driver.bus.usb.USBDataPipe;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBEndPoint;
import org.jnode.driver.bus.usb.USBException;
import org.jnode.driver.bus.usb.USBRequest;

public class UsbBluetoothDevice implements USBConstants {

    private USBDevice usbDevice;

    private USBEndPoint bulkInEndpoint;
    private USBEndPoint bulkOutEndpoint;
    private USBEndPoint intrInEndpoint;

    public UsbBluetoothDevice() {
    }

    public USBEndPoint getBulkInEndpoint() {
        return bulkInEndpoint;
    }

    public void setBulkInEndpoint(USBEndPoint bulkInEndpoint) {
        this.bulkInEndpoint = bulkInEndpoint;
    }

    public USBEndPoint getBulkOutEndpoint() {
        return bulkOutEndpoint;
    }

    public void setBulkOutEndpoint(USBEndPoint bulkOutEndpoint) {
        this.bulkOutEndpoint = bulkOutEndpoint;
    }

    public USBEndPoint getIntrInEndpoint() {
        return intrInEndpoint;
    }

    public void setIntrInEndpoint(USBEndPoint intrInEndpoint) {
        this.intrInEndpoint = intrInEndpoint;
    }

    public void testCommand() throws USBException {
        final USBControlPipe pipe = usbDevice.getDefaultControlPipe();
        final USBRequest req =
            pipe.createRequest(new SetupPacket(USB_DIR_IN | USB_TYPE_CLASS | USB_RECIP_DEVICE, 0x20, 0, 0, 0), null);
        pipe.syncSubmit(req, GET_TIMEOUT);
    }

    public USBDevice getUsbDevice() {
        return usbDevice;
    }

    public void setUsbDevice(USBDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

}
