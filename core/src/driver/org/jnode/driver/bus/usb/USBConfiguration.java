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
public class USBConfiguration extends AbstractDeviceItem {

    /**
     * The descriptor of this configuration
     */
    private final ConfigurationDescriptor descr;
    /**
     * The interfaces
     */
    private final USBInterface[] interfaces;

    /**
     * Initialize this instance.
     *
     * @param descr
     */
    public USBConfiguration(USBDevice device, ConfigurationDescriptor descr) {
        super(device);
        this.descr = descr;
        this.interfaces = new USBInterface[descr.getNumInterfaces()];
    }

    /**
     * @return Returns the descr.
     */
    public final ConfigurationDescriptor getDescriptor() {
        return this.descr;
    }

    /**
     * Gets a specific interface.
     */
    public final USBInterface getInterface(int index) {
        return this.interfaces[index];
    }

    /**
     * @param index The index of the interface
     * @param intf  The interface to set.
     */
    final void setInterface(int index, USBInterface intf) {
        if (this.interfaces[index] != null) {
            throw new SecurityException("Cannot overwrite a specific interface");
        } else {
            this.interfaces[index] = intf;
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuffer b = new StringBuffer();
        b.append("DESCR:");
        b.append(descr);
        b.append(", INTFS{");
        for (int i = 0; i < interfaces.length; i++) {
            if (i > 0) {
                b.append(", ");
            }
            b.append(interfaces[i]);
        }
        b.append("}");
        return b.toString();
    }

}
