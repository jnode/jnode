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
 
package org.vmmagic.unboxed;

import org.jnode.util.NumberUtils;
import org.jnode.vm.Vm;

/**
 * Utility class for magic classes.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class MagicUtils {

    private static transient int refSize;
    
    /**
     * Convert to a String representation.
     * @param v
     * @return the String value of the Address
     */
    public static String toString(Address v) {
        if (getRefSize() == 4) {
            return NumberUtils.hex(v.toInt());
        } else {
            return NumberUtils.hex(v.toLong());            
        }
    }
    
    /**
     * Convert to a String representation.
     * @param v
     * @return  the String value of the Extent
     */
    public static String toString(Extent v) {
        if (getRefSize() == 4) {
            return NumberUtils.hex(v.toInt());
        } else {
            return NumberUtils.hex(v.toLong());            
        }
    }
    
    /**
     * Convert to a String representation.
     * @param v
     * @return the String value of the Offset
     */
    public static String toString(Offset v) {
        if (getRefSize() == 4) {
            return NumberUtils.hex(v.toInt());
        } else {
            return NumberUtils.hex(v.toLong());            
        }
    }
    
    /**
     * Convert to a String representation.
     * @param v
     * @return the String value of the Word
     */
    public static String toString(Word v) {
        if (getRefSize() == 4) {
            return NumberUtils.hex(v.toInt());
        } else {
            return NumberUtils.hex(v.toLong());            
        }
    }
    
    private static final int getRefSize() {
        if (refSize == 0) {
            refSize = Vm.getArch().getReferenceSize();
        }
        return refSize;
    }
    
}
