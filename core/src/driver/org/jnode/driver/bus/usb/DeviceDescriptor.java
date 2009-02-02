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

import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class DeviceDescriptor extends AbstractDescriptor {

    private String manufacturer;
    private String product;
    private String serialNumber;

    /**
     * Create a new instance
     */
    public DeviceDescriptor() {
        super(USB_DT_DEVICE_SIZE);
    }

    /**
     * Gets the USB version as BCD encoded value.
     * E.g. 0x210 = 2.10.
     */
    public final int getUSBVersion() {
        return getShort(2);
    }

    /**
     * Gets the device class.
     */
    public final int getDeviceClass() {
        return getByte(4);
    }

    /**
     * Gets the device subclass.
     */
    public final int getDeviceSubClass() {
        return getByte(5);
    }

    /**
     * Gets the device protocol.
     */
    public final int getDeviceProtocol() {
        return getByte(6);
    }

    /**
     * Gets the maximum packet size for endpoint 0.
     */
    public final int getMaxPacketSize0() {
        return getByte(7);
    }

    /**
     * Gets the vendor ID
     */
    public final int getVendorID() {
        return getShort(8);
    }

    /**
     * Gets the product ID
     */
    public final int getProductID() {
        return getShort(10);
    }

    /**
     * Gets the device release number as BCD encoded value.
     */
    public final int getDeviceRelease() {
        return getShort(12);
    }

    /**
     * Gets the index of string descriptor describing manufacturer.
     */
    public final int getManufacturerStringIndex() {
        return getByte(14);
    }

    /**
     * Gets the index of string descriptor describing product.
     */
    public final int getProductStringIndex() {
        return getByte(15);
    }

    /**
     * Gets the index of string descriptor describing serial number.
     */
    public final int getSerialNumberStringIndex() {
        return getByte(16);
    }

    /**
     * Gets the number of configurations
     */
    public final int getNumConfigurations() {
        return getByte(17);
    }

    /**
     * @return Returns the manufacturer.
     */
    public final String getManufacturerName() {
        return this.manufacturer;
    }

    /**
     * @return Returns the product.
     */
    public final String getProductName() {
        return this.product;
    }

    /**
     * @return Returns the serialNumber.
     */
    public final String getSerialNumber() {
        return this.serialNumber;
    }

    /**
     * Load all strings with the default Language ID.
     *
     * @param dev
     */
    final void loadStrings(USBDevice dev)
        throws USBException {
        final int manIdx = getManufacturerStringIndex();
        if (manIdx > 0) {
            manufacturer = dev.getString(manIdx, 0);
        }
        final int prodIdx = getProductStringIndex();
        if (prodIdx > 0) {
            product = dev.getString(prodIdx, 0);
        }
        final int snIdx = getSerialNumberStringIndex();
        if (snIdx > 0) {
            serialNumber = dev.getString(snIdx, 0);
        }
    }

    /**
     * Convert to a String representation
     *
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        return "DEV[usb:0x" + NumberUtils.hex(getUSBVersion(), 4) +
            ", dclass:" + getDeviceClass() +
            ", dsubcls:" + getDeviceSubClass() +
            ", dprot:" + getDeviceProtocol() +
            ", maxps0:" + getMaxPacketSize0() +
            ", vendor:0x" + NumberUtils.hex(getVendorID(), 4) +
            ", prod:0x" + NumberUtils.hex(getProductID(), 4) +
            ", devrel:0x" + NumberUtils.hex(getDeviceRelease(), 4) +
            ", manu:" + ((manufacturer != null) ? manufacturer : ("%" + getManufacturerStringIndex())) +
            ", prod:" + ((product != null) ? product : ("%" + getProductStringIndex())) +
            ", sernr:" + ((serialNumber != null) ? serialNumber : ("%" + getSerialNumberStringIndex())) +
            ", #cnf:" + getNumConfigurations() + "]";
    }

}
