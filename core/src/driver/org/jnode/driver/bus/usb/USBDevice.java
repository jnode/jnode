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
 
package org.jnode.driver.bus.usb;

import java.util.HashMap;
import org.jnode.driver.Device;

/**
 * USB device.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class USBDevice extends Device implements USBConstants {

    private static int usbIdCounter = 0;
    /**
     * My device id 0..127
     */
    private int devId;
    /**
     * Speed of this device
     */
    private int speed;
    /**
     * The maximum packet size per endpoint
     */
    private final int[] maxPacketSize;
    /**
     * The device descriptor
     */
    private DeviceDescriptor deviceDescriptor;
    /**
     * The configurations
     */
    private USBConfiguration[] confs;
    /**
     * The active configuration
     */
    private USBConfiguration activeConf;
    /**
     * The string cache
     */
    private final HashMap<Integer, String> stringCache = new HashMap<Integer, String>();
    /**
     * The default language ID for getString, -1 means not initialized yet
     */
    private int defaultLangID = -1;
    /**
     * The default control pipe
     */
    private final USBControlPipe defaultControlPipe;

    /**
     * Initialize this device. The USB device id is not set yet. The speed is set to the given
     * parameter.
     *
     * @param bus
     * @param speed
     */
    public USBDevice(USBBus bus, int speed) {
        super(bus, "usb" + usbIdCounter++);
        this.devId = 0;
        if ((speed < USB_SPEED_LOW) || (speed > USB_SPEED_HIGH)) {
            throw new IllegalArgumentException("Invalid speed value");
        }
        this.speed = speed;
        this.maxPacketSize = new int[USB_ENDPOINT_MAX];
        if (speed == USB_SPEED_HIGH) {
            // Required for high speed devices
            this.maxPacketSize[0] = 64;
        } else {
            // Default for both low & full speed devices
            this.maxPacketSize[0] = 8;
        }
        this.defaultControlPipe = bus.getHcApi().createDefaultControlPipe(this);
    }

    /**
     * Gets the USB bus i'm connected to.
     */
    public final USBBus getUSBBus() {
        return (USBBus) getBus();
    }

    /**
     * @return Returns the USB device id.
     */
    public final int getUSBDeviceId() {
        return this.devId;
    }

    /**
     * Terminate this object
     *
     * @see java.lang.Object#finalize()
     */
    public void finalize() {
        getUSBBus().freeDeviceID(devId);
    }

    /**
     * Gets the speed of this device.
     *
     * @return Returns the speed.
     * @see USBConstants#USB_SPEED_LOW
     * @see USBConstants#USB_SPEED_FULL
     * @see USBConstants#USB_SPEED_HIGH
     */
    public final int getSpeed() {
        return this.speed;
    }

    /**
     * Is this a low speed device.
     */
    public final boolean isLowSpeed() {
        return (this.speed == USB_SPEED_LOW);
    }

    /**
     * Is this a full speed device.
     */
    public final boolean isFullSpeed() {
        return (this.speed == USB_SPEED_FULL);
    }

    /**
     * Is this a high speed device.
     */
    public final boolean isHighSpeed() {
        return (this.speed == USB_SPEED_HIGH);
    }

    /**
     * Gets the maximum packet size for a given endpoint.
     */
    public final int getMaxPacketSize(int endPoint) {
        return this.maxPacketSize[endPoint];
    }

    /**
     * @return Returns the deviceDescriptor.
     */
    public final DeviceDescriptor getDescriptor() {
        return this.deviceDescriptor;
    }

    /**
     * Sets the device descriptor. This method can only be called once. It also set the maximum
     * packet size for endpioint 0.
     *
     * @param deviceDescriptor The deviceDescriptor to set.
     */
    final void setDescriptor(DeviceDescriptor deviceDescriptor) {
        if (this.deviceDescriptor != null) {
            throw new IllegalStateException("Cannot overwrite the device descriptor");
        } else {
            this.deviceDescriptor = deviceDescriptor;
            this.maxPacketSize[0] = deviceDescriptor.getMaxPacketSize0();
        }
    }

    /**
     * Sets the device descriptor. This method can only be called once. It also set the maximum
     * packet size for endpioint 0.
     *
     * @param deviceDescriptor The deviceDescriptor to set.
     */
    final void setFullDescriptor(DeviceDescriptor deviceDescriptor) {
        if (this.confs != null) {
            throw new IllegalStateException("Cannot overwrite the full device descriptor");
        } else {
            this.deviceDescriptor = deviceDescriptor;
            this.maxPacketSize[0] = deviceDescriptor.getMaxPacketSize0();
            this.confs = new USBConfiguration[deviceDescriptor.getNumConfigurations()];
        }
    }

    /**
     * Gets a specific configuration.
     *
     * @param index
     * @return The configuration at the given index.
     */
    public final USBConfiguration getConfiguration(int index) {
        return this.confs[index];
    }

    /**
     * Sets a specific configuration.
     *
     * @param index
     * @param conf
     */
    final void setConfiguration(int index, USBConfiguration conf) {
        if (this.confs[index] != null) {
            throw new SecurityException("Cannot overwrite a specific configuration");
        } else {
            this.confs[index] = conf;
        }
    }

    /**
     * Gets a String from the device.
     *
     * @param strIndex
     * @param langID   If <= 0, the device first language is used
     */
    public final String getString(int strIndex, int langID) throws USBException {

        if (strIndex <= 0) {
            throw new IllegalArgumentException("Invalid string index " + strIndex);
        }

        // Is the language ID given?
        if (langID <= 0) {
            // Not given, use the default
            if (defaultLangID < 0) {
                // Initialize the default langID first
                final StringDescriptorZero descr = new StringDescriptorZero(256);
                try {
                    // Get on the first langID (length=4)
                    readDescriptor(USB_RECIP_DEVICE, USB_DT_STRING, 0, 0, 4, descr);
                    defaultLangID = descr.getLangID(0);
                } catch (USBException ex) {
                    // Just pick something then
                    defaultLangID = 0;
                }
            }
            langID = defaultLangID;
        }

        final int cacheKey = (langID << 16) | strIndex;
        final String cacheValue = stringCache.get(cacheKey);
        if (cacheValue != null) {
            return cacheValue;
        }

        final StringDescriptor descr = new StringDescriptor(256);
        readDescriptor(USB_RECIP_DEVICE, USB_DT_STRING, strIndex, langID, -1, descr);

        // Put it in the cache
        stringCache.put(cacheKey, cacheValue);

        // Return the string
        return descr.getString();
    }

    /**
     * Read a descriptor from the device.
     *
     * @param reqType
     * @param descrType
     * @param index
     * @param langID
     * @param length    The length of the descriptor to read. When (length &lt; 0) then first the length
     *                  of the descriptor is read, after which the full length is read.
     * @param descr
     */
    public final void readDescriptor(int reqType, int descrType, int index, int langID, int length, USBPacket descr)
        throws USBException {
        USBException lastEx = null;
        for (int attempt = 0; attempt < GET_DESCRIPTOR_ATTEMPTS; attempt++) {
            try {
                if (length < 0) {
                    // Determine length first
                    final USBRequest initReq = defaultControlPipe.createRequest(
                        SetupPacket.createGetDescriptorPacket(reqType, descrType, index, langID, 1), descr);
                    defaultControlPipe.syncSubmit(initReq, GET_TIMEOUT);
                    length = descr.getByte(0); // The length field
                }
                final USBRequest req = defaultControlPipe.createRequest(
                    SetupPacket.createGetDescriptorPacket(reqType, descrType, index, langID, length), descr);
                defaultControlPipe.syncSubmit(req, GET_TIMEOUT);
            } catch (USBException ex) {
                lastEx = ex;
            }
        }
        if (lastEx != null) {
            throw lastEx;
        }
    }

    /**
     * Set the USB device address of this device. A SET_ADDRESS request is send to the device.
     *
     * @param usbAddress
     * @throws USBException
     */
    final void setAddress(int usbAddress) throws USBException {
        if ((this.devId != 0) && (usbAddress != 0)) {
            throw new SecurityException("Cannot overwrite the USB device id.");
        }
        final USBRequest req =
            defaultControlPipe.createRequest(SetupPacket.createDeviceSetAddressPacket(usbAddress), null);
        defaultControlPipe.syncSubmit(req, SET_TIMEOUT);
        this.devId = usbAddress;
    }

    /**
     * Gets the active configuration
     *
     * @return The active configuration, or null if the active configuration has not been set.
     */
    public final USBConfiguration getConfiguration() {
        return this.activeConf;
    }

    /**
     * Sets the active configuration.
     *
     * @param activeConf The configuration to set.
     */
    public final void setConfiguration(USBConfiguration activeConf) throws USBException {
        final int confNum = activeConf.getDescriptor().getConfigurationValue();
        final USBRequest req =
            defaultControlPipe.createRequest(SetupPacket.createDeviceSetConfigurationPacket(confNum), null);
        defaultControlPipe.syncSubmit(req, SET_TIMEOUT);
        this.activeConf = activeConf;
    }

    /**
     * Issue a GET_STATUS request to this device.
     *
     * @return The status returned by the device.
     * @throws USBException
     */
    public final int getStatus() throws USBException {
        final USBPacket data = new USBPacket(2);
        final USBRequest req =
            defaultControlPipe.createRequest(SetupPacket.createGetStatusPacket(USB_RECIP_DEVICE, 0), data);
        defaultControlPipe.syncSubmit(req, GET_TIMEOUT);
        return data.getShort(0);
    }

    /**
     * Issue a SET_FEATURE request.
     *
     * @param featureSelector
     * @throws USBException
     */
    public final void setFeature(int reqType, int index, int featureSelector) throws USBException {
        final USBRequest req =
            defaultControlPipe.createRequest(SetupPacket.createSetFeaturePacket(reqType, index, featureSelector), null);
        defaultControlPipe.syncSubmit(req, SET_TIMEOUT);
    }

    /**
     * Issue a CLEAR_FEATURE request.
     *
     * @param featureSelector
     * @throws USBException
     */
    public final void clearFeature(int reqType, int index, int featureSelector) throws USBException {
        final USBRequest req =
            defaultControlPipe
                .createRequest(SetupPacket.createClearFeaturePacket(reqType, index, featureSelector), null);
        defaultControlPipe.syncSubmit(req, SET_TIMEOUT);
    }

    /**
     * Gets the default control pipe.
     *
     * @return Returns the defaultControlPipe.
     */
    public final USBControlPipe getDefaultControlPipe() {
        return this.defaultControlPipe;
    }

}
