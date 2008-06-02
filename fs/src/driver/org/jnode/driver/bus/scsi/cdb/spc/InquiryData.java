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

package org.jnode.driver.bus.scsi.cdb.spc;

import org.jnode.driver.bus.scsi.SCSIBuffer;
import org.jnode.util.NumberUtils;


/**
 * Wrapper class for interpreting INQUIRY results.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InquiryData extends SCSIBuffer {

    /**
     * Initialize this instance
     *
     * @param data
     */
    public InquiryData(byte[] data) {
        super(data);
    }

    /**
     * Gets the peripheral device type.
     */
    public final int getPeripheralDeviceType() {
        return getUInt8(0) & 0x1F;
    }

    /**
     * Gets the peripheral qualifier.
     */
    public final int getPeripheralQualifier() {
        return getUInt8(0) >> 5;
    }

    /**
     * Is this a removable device.
     */
    public final boolean isRemovable() {
        return ((getUInt8(1) & 0x80) != 0);
    }

    /**
     * Gets the implemented version of the SCSI standard.
     * MM Logical Units attached via the ATA-PI shall report 00h.
     * MM Logical Units attached via the SCSI shall report 04h.
     */
    public final int getVersion() {
        return getUInt8(2);
    }

    /**
     * Gets the vendor identification.
     */
    public final String getVendorIdentification() {
        return getASCII(8, 8);
    }

    /**
     * Gets the product identification.
     */
    public final String getProductIdentification() {
        return getASCII(16, 16);
    }

    /**
     * Gets the product revision level.
     */
    public final String getProductRevisionLevel() {
        return getASCII(32, 4);
    }

    /**
     * Gets the standards to which the device claims conformance.
     */
    public final int[] getVersionDescriptors() {
        final int[] descriptors = new int[8];
        for (int i = 0; i < descriptors.length; i++) {
            descriptors[i] = getUInt16(58 + i * 2);
        }
        return descriptors;
    }

    /**
     * Does this device claim conformance to the given standard.
     */
    public final boolean containsVersionDescriptors(int standardDescriptor) {
        for (int i = 0; i < 8; i++) {
            if (standardDescriptor == getUInt16(58 + i * 2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "PeripheralDeviceType 0x" + NumberUtils.hex(getPeripheralDeviceType(), 2) +
            ", PeripheralQualifier 0x" + NumberUtils.hex(getPeripheralQualifier(), 2) +
            ", Version 0x" + NumberUtils.hex(getVersion(), 2) +
            ", Vendor " + getVendorIdentification() +
            ", Product " + getProductIdentification() +
            ", Revision " + getProductRevisionLevel() +
            ", Version descriptors " + NumberUtils.hex(getVersionDescriptors(), 4);

    }
}
