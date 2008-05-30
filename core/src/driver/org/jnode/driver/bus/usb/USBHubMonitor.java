/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.driver.bus.usb;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;
import org.jnode.util.NumberUtils;

/**
 * Class used to watch an USB HUB for changes in the connection status of its ports.
 * <p/>
 * To have the best chance of success we do things in the exact same order as Windoze98. This
 * should not be necessary, but some devices do not follow the USB specs to the letter.
 * <p/>
 * These are the events on the bus when a hub is attached:
 * <ul>
 * <li>Get device and config descriptors (see attach code)</li>
 * <li>Get hub descriptor (see above)</li>
 * <li>For all ports
 * <ul>
 * <li>turn on power</li>
 * <li>wait for power to become stable</li>
 * </ul>
 * </li>
 * <li>For all ports
 * <ul>
 * <li>clear C_PORT_CONNECTION</li>
 * </ul>
 * </li>
 * <li>For all ports
 * <ul>
 * <li>get port status</li>
 * <li>if device connected
 * <ul>
 * <li>wait 100 ms</li>
 * <li>turn on reset</li>
 * <li>wait</li>
 * <li>clear C_PORT_RESET</li>
 * <li>get port status</li>
 * <li>proceed with device attachment</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * </ul>
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class USBHubMonitor implements USBConstants {

    /**
     * My logger
     */
    protected final Logger log = Logger.getLogger(getClass());
    /**
     * The hub device
     */
    private final Device hubDevice;
    /**
     * The Hub API
     */
    private final USBHubAPI hub;
    /**
     * The device manager
     */
    private final DeviceManager dm;
    /**
     * The monitor thread (if started)
     */
    private USBHubMonitorThread thread;

    /**
     * Initialize a new instance.
     *
     * @param hub
     */
    public USBHubMonitor(Device hubDevice, USBHubAPI hub) {
        this.hubDevice = hubDevice;
        this.hub = hub;
        this.dm = hubDevice.getManager();
    }

    /**
     * Do the actual monitoring.
     */
    protected void checkStatus(boolean first) {
        try {
            final int ports = hub.getNumPorts();
            for (int port = 0; port < ports; port++) {
                if (first) {
                    hub.setPortEnabled(port, false);
                    sleep(100);
                    hub.resetPort(port);
                    sleep(100);
                }
                if (first || hub.isPortConnectionStatusChanged(port)) {
                    portConnectionStatusChanged(port);
                }
            }
        } catch (USBException ex) {
            log.error("Error in portConnectionStatusChanged", ex);
        }
    }

    /**
     * The connection status of a given port has changed.
     *
     * @param port
     */
    protected void portConnectionStatusChanged(int port) throws USBException {
        log.debug("USB hub connection status changed for port " + port);

        if (hub.isPortConnected(port)) {
            //log.debug("Port " + port + " is connected");
            // Wait for at least 100ms to stabilize
            sleep(100);
            // Reset the port
            hub.resetPort(port);
            // Wait a while for reset to stabilize
            sleep(100);

            final int speed;
            if (hub.isPortConnectedToLowSpeed(port)) {
                speed = USB_SPEED_LOW;
                log.debug("Port " + port + " is connected to lowspeed device");
            } else if (hub.isPortConnectedToHighSpeed(port)) {
                log.debug("Port " + port + " is connected to highspeed device");
                speed = USB_SPEED_HIGH;
            } else {
                log.debug("Port " + port + " is connected to fullspeed device");
                speed = USB_SPEED_FULL;
            }

            // Create the new device
            //log.debug("Creating USBDevice");
            final USBDevice dev = new USBDevice(hub.getUSBBus(), speed);
            dev.getDefaultControlPipe().open();

            // Now set the address
            //log.debug("Set the address");
            final int devId = dev.getUSBBus().allocDeviceID();
            try {
                // Set the device address
                dev.setAddress(devId);
                log.debug("Now using address 0x" + NumberUtils.hex(devId, 2));
                sleep(100); // Let the address settle
                log.debug("After sleep");

                // Determine the maximum packet size by fetching 8 bytes of device descriptor.
                //log.debug("Fetching first 8 bytes of device descriptor");
                final DeviceDescriptor devDescr = new DeviceDescriptor();
                dev.readDescriptor(USB_RECIP_DEVICE, USB_DT_DEVICE, 0, 0, 8, devDescr);
                dev.setDescriptor(devDescr);
                log.debug("devDescr[0-7]=" + devDescr + ", len=" + devDescr.getLength());

                // Fetch the complete device descriptor.
                dev.readDescriptor(USB_RECIP_DEVICE, USB_DT_DEVICE, 0, 0, USB_DT_DEVICE_SIZE, devDescr);
                log.debug("read devDescr=" + devDescr);
                dev.setFullDescriptor(devDescr);
                log.debug("Full devDescr=" + devDescr);

                // Get all configurations.
                final int confCount = devDescr.getNumConfigurations();
                for (int i = 0; i < confCount; i++) {
                    final USBConfiguration conf = getConfiguration(dev, i);
                    dev.setConfiguration(i, conf);
                    log.debug("Read configuration " + conf);
                }
                // Set configuration 0
                dev.setConfiguration(dev.getConfiguration(0));

                // Load the strings of the device descriptor
                devDescr.loadStrings(dev);
                log.debug("Got descriptor " + devDescr);

                // Register the device with the HUB
                log.debug("hub.setDevice");
                hub.setDevice(dev, port);
                // Register the device with the device manager
                log.debug("dm.register");
                dm.register(dev);

                log.debug("Found USB device " + devDescr);
            } catch (USBException ex) {
                log.debug("Port status 0x" + NumberUtils.hex(hub.getPortStatus(port)));
                dev.getUSBBus().freeDeviceID(devId);
                hub.unsetDevice(port);
                throw ex;
            } catch (DriverException ex) {
                dev.getUSBBus().freeDeviceID(devId);
                hub.unsetDevice(port);
                throw new USBException(ex);
            } catch (DeviceAlreadyRegisteredException ex) {
                dev.getUSBBus().freeDeviceID(devId);
                hub.unsetDevice(port);
                throw new USBException(ex);
            }

        } else {
            log.debug("Port " + port + " is not connected");
            final USBDevice dev = hub.getDevice(port);
            if (dev != null) {
                try {
                    // Unregister the device
                    dm.unregister(dev);
                } catch (DriverException ex) {
                    log.error("Error unregistering disconnected USB device", ex);
                }
                // Remove the device from the HUB
                hub.unsetDevice(port);
            }
            hub.setPortEnabled(port, false);
        }

        // Clear the connection status
        hub.clearPortConnectionStatusChanged(port);
    }

    /**
     * Gets a specific configuration for the given device.
     *
     * @param dev
     * @param confNum
     */
    private final USBConfiguration getConfiguration(USBDevice dev, int confNum) throws USBException {

        // First read enough to get the wTotalLength
        final ConfigurationDescriptor initDescr = new ConfigurationDescriptor();
        dev.readDescriptor(USB_RECIP_DEVICE, USB_DT_CONFIG, confNum, 0, USB_DT_CONFIG_SIZE, initDescr);

        // Now get the full configuration data
        final byte[] data = new byte[initDescr.getTotalLength()];
        final USBPacket dataP = new USBPacket(data);
        dev.readDescriptor(USB_RECIP_DEVICE, USB_DT_CONFIG, confNum, 0, data.length, dataP);

        // Get the configuration strings
        final ConfigurationDescriptor confDescr = new ConfigurationDescriptor(data, 0, USB_DT_CONFIG_SIZE);
        confDescr.loadStrings(dev);

        // Create the configuration
        final USBConfiguration conf = new USBConfiguration(dev, confDescr);

        // Parse the interface descriptors
        final int intfCount = confDescr.getNumInterfaces();
        int offset = confDescr.getLength();
        for (int i = 0; i < intfCount; i++) {
            final InterfaceDescriptor intfDescr = new InterfaceDescriptor(data, offset, USB_DT_INTERFACE_SIZE);
            intfDescr.loadStrings(dev);
            offset += intfDescr.getLength();
            final USBInterface intf = new USBInterface(conf, intfDescr);
            conf.setInterface(i, intf);

            // Parse the endpoint descriptors
            final int epCount = intfDescr.getNumEndPoints();
            for (int epIndex = 0; epIndex < epCount;) {
                final int dType = dataP.getByte(offset + 1);
                if (dType == USB_DT_ENDPOINT) {
                    // It is an e
                    final EndPointDescriptor epDescr = new EndPointDescriptor(data, offset, USB_DT_ENDPOINT_SIZE);
                    offset += epDescr.getLength();
                    final USBEndPoint ep = new USBEndPoint(intf, epDescr);
                    intf.setEndPoint(epIndex++, ep);
                } else {
                    // Skip this unknown descriptor
                    log.debug("Skipping unknown descriptor type " + dType);
                    offset += dataP.getByte(offset); // Length
                }
            }
        }

        return conf;
    }

    /**
     * Sleep a while
     *
     * @param ms
     */
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            // Ignore
        }
    }

    /**
     * @see org.jnode.driver.DeviceListener#deviceStarted(org.jnode.driver.Device)
     */
    public void startMonitor() {
        if (thread == null) {
            thread = new USBHubMonitorThread("HubMonitor-" + hubDevice.getId());
            thread.start();
        }
    }

    /**
     * @see org.jnode.driver.DeviceListener#deviceStop(org.jnode.driver.Device)
     */
    public void stopMonitor() {
        final USBHubMonitorThread thread = this.thread;
        if (thread != null) {
            thread.stopThread();
            try {
                thread.join(2000);
            } catch (InterruptedException ex) {
                // Ignore
            }
        }
        this.thread = null;
    }

    /**
     * The actual monitor thread.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    class USBHubMonitorThread extends Thread {

        private boolean stop;

        public USBHubMonitorThread(String name) {
            super(name);
            this.stop = false;
        }

        public void stopThread() {
            this.stop = true;
        }

        public void run() {
            boolean first = true;
            while (!stop) {
                try {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        stop = true;
                    }
                    checkStatus(first);
                } catch (Exception ex) {
                    log.error("Error in USBHubMonitor", ex);
                }
                first = false;
            }
        }

    }
}
