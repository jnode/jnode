/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import java.security.ProtectionDomain;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmPrimitiveClass extends VmNormalClass {

    /** Is this a floatingpoint type? */
    private final boolean floatingPoint;
    
    /** Is this a wide type? */
    private final boolean wide;
    
     /**
     * @param name
     * @param superClass
     * @param loader
     * @param typeSize
     */
    public VmPrimitiveClass(String name, VmNormalClass superClass,
            VmClassLoader loader, int typeSize, boolean floatingPoint, ProtectionDomain protectionDomain) {
        super(name, superClass, loader, typeSize, protectionDomain);
        this.floatingPoint = floatingPoint;
        this.wide = (typeSize == 8);
    }
    
    /**
     * Is this class a primitive type?
     * 
     * @return boolean
     */
    public boolean isPrimitive() {
        return true;
    }
   
    /**
     * Is this a wide primitive type; long or double
     */
    public final boolean isWide() {
        return wide;
    }
    
    /**
     * Is this a floatingpoint primitive type; float or double
     */
    public final boolean isFloatingPoint() {
        return floatingPoint;
    }
    
}
