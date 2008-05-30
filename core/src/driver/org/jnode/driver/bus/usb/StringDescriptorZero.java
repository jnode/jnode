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

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class StringDescriptorZero extends AbstractDescriptor {

    /**
     * @param data
     * @param ofs
     * @param len
     */
    public StringDescriptorZero(byte[] data, int ofs, int len) {
        super(data, ofs, len);
    }

    /**
     * @param size
     */
    public StringDescriptorZero(int size) {
        super(size);
    }

    /**
     * Gets the number of language ID's.
     *
     * @return The number of language ID's.
     */
    public final int getNumLangIDs() {
        return (getLength() - 2) / 2;
    }

    /**
     * Gets the language ID at a given index.
     *
     * @param index
     */
    public int getLangID(int index) {
        return getShort(2 + (index << 1));
    }

    /**
     * Is the given language ID contained in this descriptor.
     *
     * @param langID
     */
    public boolean contains(int langID) {
        final int cnt = getNumLangIDs();
        for (int i = 0; i < cnt; i++) {
            if (getLangID(i) == langID) {
                return true;
            }
        }
        return false;
    }

}
