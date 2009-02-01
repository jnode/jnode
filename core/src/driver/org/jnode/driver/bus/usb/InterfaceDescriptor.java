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
public final class InterfaceDescriptor extends AbstractDescriptor {

    private String intfName;

    /**
     * Initialize a new instance
     */
    public InterfaceDescriptor() {
        super(USB_DT_INTERFACE_SIZE);
    }

    /**
     * @param data
     * @param ofs
     * @param len
     */
    public InterfaceDescriptor(byte[] data, int ofs, int len) {
        super(data, ofs, len);
    }

    /**
     * Gets the number of this interface.
     */
    public final int getInterfaceNumber() {
        return getByte(2);
    }

    /**
     * Gets the alternate setting for the interface number.
     */
    public final int getAlternateSetting() {
        return getByte(3);
    }

    /**
     * Gets the number of endpoints
     */
    public final int getNumEndPoints() {
        return getByte(4);
    }

    /**
     * Gets the class of this interface.
     */
    public final int getInterfaceClass() {
        return getByte(5);
    }

    /**
     * Gets the subclass of this interface.
     */
    public final int getInterfaceSubClass() {
        return getByte(6);
    }

    /**
     * Gets the protocol of this interface.
     */
    public final int getInterfaceProtocol() {
        return getByte(7);
    }

    /**
     * Gets the index of string descriptor describing this interface.
     */
    public final int getInterfaceStringIndex() {
        return getByte(8);
    }

    /**
     * Gets the name of this interface in the default language.
     */
    public final String getInterfaceName() {
        return intfName;
    }

    /**
     * Load all strings with the default Language ID.
     *
     * @param dev
     */
    final void loadStrings(USBDevice dev)
        throws USBException {
        final int iIdx = getInterfaceStringIndex();
        if (iIdx > 0) {
            intfName = dev.getString(iIdx, 0);
        }
    }

    /**
     * Convert to a String representation
     *
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        return "INTF[ifnum:" + getInterfaceNumber() +
            ", altst:" + getAlternateSetting() +
            ", #ep:" + getNumEndPoints() +
            ", iclass:" + getInterfaceClass() +
            ", isubcls:" + getInterfaceSubClass() +
            ", iprot:" + getInterfaceProtocol() +
            ", name:" + ((intfName != null) ? intfName : ("%" + getInterfaceStringIndex())) + "]";
    }
}
