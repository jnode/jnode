/*
 * $Id$
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
     * @return
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
     * @return
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
     * @return
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
     * @return
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
            refSize = Vm.getVm().getArch().getReferenceSize();
        }
        return refSize;
    }
    
}
