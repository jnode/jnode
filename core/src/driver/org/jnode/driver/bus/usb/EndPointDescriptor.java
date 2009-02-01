/*
 * $Id$
 *
 * JNode.org
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

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class EndPointDescriptor extends AbstractDescriptor {

    /**
     * Initialize this instance.
     */
    public EndPointDescriptor() {
        super(USB_DT_ENDPOINT_SIZE);
    }

    /**
     * @param data
     * @param ofs
     * @param len
     */
    public EndPointDescriptor(byte[] data, int ofs, int len) {
        super(data, ofs, len);
    }

    /**
     * Gets the address of this endpoint.
     * This is a combination of address and direction.
     */
    public final int getEndPointAddress() {
        return getByte(2);
    }

    /**
     * Gets the number of this endpoint.
     */
    public final int getEndPointNumber() {
        return getByte(2) & USB_ENDPOINT_NUMBER_MASK;
    }

    /**
     * Gets the direction of this endpoint.
     */
    public final boolean isDirIn() {
        return ((getByte(2) & USB_ENDPOINT_DIR_MASK) == USB_DIR_IN);
    }

    /**
     * Gets the direction of this endpoint.
     */
    public final boolean isDirOut() {
        return ((getByte(2) & USB_ENDPOINT_DIR_MASK) == USB_DIR_OUT);
    }

    /**
     * Gets the attributes of this endpoint.
     * This is a combination of tranfer type, synchronization type and usage type.
     */
    public final int getAttributes() {
        return getByte(3);
    }

    /**
     * Gets the transfer type of this endpoint.
     */
    public final int getTransferType() {
        return getByte(3) & USB_ENDPOINT_XFERTYPE_MASK;
    }

    /**
     * Is the transfer type of this endpoint control.
     */
    public final boolean isControlTransfer() {
        return ((getByte(3) & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_CONTROL);
    }

    /**
     * Is the transfer type of this endpoint interrupt.
     */
    public final boolean isIntTransfer() {
        return ((getByte(3) & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_INT);
    }

    /**
     * Is the transfer type of this endpoint bulk.
     */
    public final boolean isBulkTransfer() {
        return ((getByte(3) & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_BULK);
    }

    /**
     * Is the transfer type of this endpoint isochronous.
     */
    public final boolean isIsochronousTransfer() {
        return ((getByte(3) & USB_ENDPOINT_XFERTYPE_MASK) == USB_ENDPOINT_XFER_ISOC);
    }

    /**
     * Gets the maximum size of packets
     */
    public final int getMaxPacketSize() {
        return getShort(4) & USB_ENDPOINT_MAXPS_MASK;
    }

    /**
     * Gets the interval for polling
     */
    public final int getInterval() {
        return getByte(6);
    }

    /**
     * Convert to a String representation
     *
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        return "EP[epnum:" + getEndPointNumber() +
            ", dir:" + (isDirIn() ? "IN" : "OUT") +
            ", xfertype:" + USB_ENDPOINT_XFER_NAMES[getTransferType()] +
            ", mpsize:" + getMaxPacketSize() +
            ", intval:" + getInterval() + "]";
    }
}
