/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.vm.classmgr;

import java.security.ProtectionDomain;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmPrimitiveClass<T> extends VmNormalClass<T> {

    /**
     * Is this a floating point type?
     */
    private final boolean floatingPoint;

    /**
     * Is this a wide type?
     */
    private final boolean wide;

    /**
     * The {@link org.jnode.vm.JvmType} for this type
     */
    private final int jvmType;

    /**
     * @param name
     * @param superClass
     * @param loader
     * @param typeSize
     */
    public VmPrimitiveClass(String name, VmNormalClass<? super T> superClass,
                            VmClassLoader loader, int jvmType, int typeSize, boolean floatingPoint,
                            ProtectionDomain protectionDomain) {
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
     * Is this a wide primitive type; i.e. a long or double
     */
    public final boolean isWide() {
        return wide;
    }

    /**
     * Is this a floating point primitive type; i.e. a float or double
     */
    public final boolean isFloatingPoint() {
        return floatingPoint;
    }

    /**
     * Gets the {@link org.jnode.vm.JvmType} value for this type.
     *
     * @return the {@link org.jnode.vm.JvmType} (integer) value
     */
    public int getJvmType() {
        return jvmType;
    }
}
