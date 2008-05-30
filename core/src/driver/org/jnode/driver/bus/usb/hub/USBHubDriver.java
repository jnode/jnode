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

package org.jnode.driver.bus.usb.hub;

import org.apache.log4j.Logger;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.usb.SetupPacket;
import org.jnode.driver.bus.usb.USBBus;
import org.jnode.driver.bus.usb.USBControlPipe;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBException;
import org.jnode.driver.bus.usb.USBHubAPI;
import org.jnode.driver.bus.usb.USBHubMonitor;
import org.jnode.driver.bus.usb.USBRequest;

/**
 * Driver for USB Hubs.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class USBHubDriver extends Driver implements USBHubAPI, USBHubConstants {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(USBHubDriver.class);
    /**
     * The number of ports
     */
    private int nrPorts;
    /**
     * The devices per port
     */
    private USBDevice[] devices;
    /**
     * The monitor for this HUB
     */
    private USBHubMonitor monitor;
    /**
     * The HUB device itself
     */
    private USBDevice dev;
    /**
     * The HUB descriptor
     */
    private HubDescriptor descr;
    /**
     * Data packet for getPortStatus
     */
    private final PortStatus statusData = new PortStatus();

    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected void startDevice() throws DriverException {
        this.dev = (USBDevice) getDevice();

        try {
            // Read the descriptor
            this.descr = new HubDescriptor();
            dev.readDescriptor(USB_RT_HUB, USB_DT_HUB, 0, 0, -1, descr);
            log.debug("Read HUB: " + descr);
            // Set the variables
            this.nrPorts = descr.getNumPorts();
            this.devices = new USBDevice[nrPorts];

            // Power the ports
            powerOn();

            // Create the monitor
            monitor = new USBHubMonitor(dev, this);
            monitor.startMonitor();
        } catch (USBException ex) {
            throw new DriverException(ex);
        }
    }

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected void stopDevice() throws DriverException {
        // First stop all devices connected to this HUB.
        for (int i = 0; i < nrPorts; i++) {
            final USBDevice dev = devices[i];
            if (dev != null) {
                try {
                    dev.getManager().stop(dev);
                } catch (DeviceNotFoundException ex) {
                    log.error("Device not found " + dev.getId(), ex);
                }
            }
            devices[i] = null;
        }
        if (monitor != null) {
            monitor.stopMonitor();
            monitor = null;
        }
        this.nrPorts = 0;
        this.devices = null;
        this.dev = null;
        // Now unregister the API
        final USBDevice dev = (USBDevice) getDevice();
        dev.unregisterAPI(USBHubAPI.class);
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#clearPortConnectionStatusChanged(int)
     */
    public void clearPortConnectionStatusChanged(int port)
        throws USBException {
        testPort(port);
        clearPortFeature(port, USB_PORT_FEAT_C_CONNECTION);
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#getDevice(int)
     */
    public USBDevice getDevice(int port) {
        testPort(port);
        return this.devices[port];
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#getNumPorts()
     */
    public int getNumPorts() {
        return nrPorts;
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#getPortStatus(int)
     */
    public synchronized int getPortStatus(int port) throws USBException {
        readPortStatus(port, statusData);
        return (statusData.getPortStatus() << 16) | statusData.getPortChange();
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#getUSBBus()
     */
    public USBBus getUSBBus() {
        return ((USBDevice) getDevice()).getUSBBus();
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#isPortConnected(int)
     */
    public synchronized boolean isPortConnected(int port) throws USBException {
        readPortStatus(port, statusData);
        return statusData.getPortStatusBits(USB_PORT_STAT_CONNECTION);
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#isPortConnectedToHighSpeed(int)
     */
    public boolean isPortConnectedToHighSpeed(int port) throws USBException {
        readPortStatus(port, statusData);
        return statusData.getPortStatusBits(USB_PORT_STAT_HIGH_SPEED);
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#isPortConnectedToLowSpeed(int)
     */
    public boolean isPortConnectedToLowSpeed(int port) throws USBException {
        readPortStatus(port, statusData);
        return statusData.getPortStatusBits(USB_PORT_STAT_LOW_SPEED);
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#isPortConnectionStatusChanged(int)
     */
    public boolean isPortConnectionStatusChanged(int port) throws USBException {
        readPortStatus(port, statusData);
        return statusData.getPortChangeBits(USB_PORT_STAT_C_CONNECTION);
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#isPortEnabled(int)
     */
    public boolean isPortEnabled(int port) throws USBException {
        readPortStatus(port, statusData);
        return statusData.getPortStatusBits(USB_PORT_STAT_ENABLE);
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#resetPort(int)
     */
    public void resetPort(int port) throws USBException {
        testPort(port);
        setPortFeature(port, USB_PORT_FEAT_RESET);
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#setDevice(org.jnode.driver.bus.usb.USBDevice, int)
     */
    public void setDevice(USBDevice dev, int port) {
        testPort(port);
        this.devices[port] = dev;
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#setPortEnabled(int, boolean)
     */
    public void setPortEnabled(int port, boolean enabled) throws USBException {
        testPort(port);
        if (enabled) {
            setPortFeature(port, USB_PORT_FEAT_ENABLE);
        } else {
            clearPortFeature(port, USB_PORT_FEAT_ENABLE);
        }
    }

    /**
     * @see org.jnode.driver.bus.usb.USBHubAPI#unsetDevice(int)
     */
    public void unsetDevice(int port) {
        testPort(port);
        devices[port] = null;
    }

    /**
     * Test for a valid port number.
     *
     * @param port
     * @throws IllegalArgumentException
     */
    private final void testPort(int port) throws IllegalArgumentException {
        if ((port < 0) || (port >= nrPorts)) {
            throw new IllegalArgumentException("Invalid port number");
        }
    }

    /**
     * Clear a HUB feature
     *
     * @param featureSelector
     * @throws USBException
     */
    /*private final void clearHubFeature(int featureSelector) throws USBException {
         dev.clearFeature(USB_RT_HUB, 0, featureSelector);
     }*/

    /**
     * Clear a Port feature
     *
     * @param port            0..nrPorts-1
     * @param featureSelector
     * @throws USBException
     */
    private final void clearPortFeature(int port, int featureSelector) throws USBException {
        dev.clearFeature(USB_RT_PORT, port + 1, featureSelector);
    }

    /**
     * Set a HUB feature
     *
     * @param featureSelector
     * @throws USBException
     */
    /*private final void setHubFeature(int featureSelector) throws USBException {
         dev.setFeature(USB_RT_HUB, 0, featureSelector);
     }*/

    /**
     * Set a Port feature
     *
     * @param port            0..nrPorts-1
     * @param featureSelector
     * @throws USBException
     */
    private final void setPortFeature(int port, int featureSelector) throws USBException {
        dev.setFeature(USB_RT_PORT, port + 1, featureSelector);
    }

    /**
     * Set power on all ports
     *
     * @throws USBException
     */
    private final void powerOn()
        throws USBException {
        for (int port = 0; port < nrPorts; port++) {
            setPortFeature(port, USB_PORT_FEAT_POWER);
        }
        // Wait for power good
        try {
            Thread.sleep(descr.getPowerOn2PowerGood());
        } catch (InterruptedException ex) {
            // Ignore
        }
    }

    /**
     * Read the status of a given port into the given structure.
     */
    private void readPortStatus(int port, PortStatus data) throws USBException {
        testPort(port);
        final USBControlPipe pipe = dev.getDefaultControlPipe();
        final USBRequest req =
            pipe.createRequest(new SetupPacket(USB_DIR_IN | USB_RT_PORT, USB_REQ_GET_STATUS, 0, port + 1, 4), data);
        pipe.syncSubmit(req, GET_TIMEOUT);
    }

}
