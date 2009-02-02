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

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class StringDescriptor extends AbstractDescriptor {

    /**
     * The cached string
     */
    private String cachedString;

    /**
     * @param data
     * @param ofs
     * @param len
     */
    public StringDescriptor(byte[] data, int ofs, int len) {
        super(data, ofs, len);
    }

    /**
     * @param size
     */
    public StringDescriptor(int size) {
        super(size);
    }

    /**
     * Gets the actual string.
     */
    public final String getString() {
        if (cachedString == null) {
            final int strLen = (getLength() - 2) >> 1;
            final char[] str = new char[strLen];
            for (int i = 0; i < strLen; i++) {
                str[i] = getChar(2 + (i << 1));
            }
            cachedString = new String(str);
        }
        return cachedString;
    }
}
