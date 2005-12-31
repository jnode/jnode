/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.vm.classmgr;

import java.security.ProtectionDomain;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmPrimitiveClass<T> extends VmNormalClass<T> {

    /** Is this a floatingpoint type? */
    private final boolean floatingPoint;
    
    /** Is this a wide type? */
    private final boolean wide;
    
    /** JvmType of this type */
    private final int jvmType;
    
     /**
     * @param name
     * @param superClass
     * @param loader
     * @param typeSize
     */
    public VmPrimitiveClass(String name, VmNormalClass<? super T> superClass,
            VmClassLoader loader, int jvmType, int typeSize, boolean floatingPoint, ProtectionDomain protectionDomain) {
        super(name, superClass, loader, typeSize, protectionDomain);
        this.jvmType = jvmType;
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
       
    /**
     * Gets the JvmType of this type.
     * @see org.jnode.vm.JvmType
     * @return
     */
    public int getJvmType() {
    	return jvmType;
    }
}
