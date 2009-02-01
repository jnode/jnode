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
public final class ConfigurationDescriptor extends AbstractDescriptor {

    private String configurationName;

    /**
     * Initialize a new instance
     */
    public ConfigurationDescriptor() {
        super(USB_DT_CONFIG_SIZE);
    }

    /**
     * @param data
     * @param ofs
     * @param len
     */
    public ConfigurationDescriptor(byte[] data, int ofs, int len) {
        super(data, ofs, len);
    }

    /**
     * Gets the total length of data returned for this configuration.
     */
    public final int getTotalLength() {
        return getShort(2);
    }

    /**
     * Gets the number of interfaces
     */
    public final int getNumInterfaces() {
        return getByte(4);
    }

    /**
     * Gets the value to use as an argument to SetConfiguration.
     */
    public final int getConfigurationValue() {
        return getByte(5);
    }

    /**
     * Gets the index of string descriptor describing this configuration.
     */
    public final int getConfigurationStringIndex() {
        return getByte(6);
    }

    /**
     * @return Returns the configuration.
     */
    public final String getConfigurationName() {
        return this.configurationName;
    }

    /**
     * Load all strings with the default Language ID.
     *
     * @param dev
     */
    final void loadStrings(USBDevice dev)
        throws USBException {
        final int cIdx = getConfigurationStringIndex();
        if (cIdx > 0) {
            configurationName = dev.getString(cIdx, 0);
        }
    }

    /**
     * Convert to a String representation
     *
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        return "CONF[totlen:" + getTotalLength() +
            ", #intf:" + getNumInterfaces() +
            ", cnfval:" + getConfigurationValue() +
            ", name:" + ((configurationName != null) ? configurationName : ("%" + getConfigurationStringIndex())) + "]";
    }

}
