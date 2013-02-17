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
import java.util.HashSet;

/**
 * Class structure for array classes.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmArrayClass<T> extends VmClassType<T> {

    /**
     * The type of elements in an array class
     */
    private final VmType<?> componentType;

    private long totalLength;
    private int maxLength;

    /**
     * @param name
     * @param loader
     * @param componentType
     * @param typeSize
     */
    VmArrayClass(String name, VmClassLoader loader, VmType<?> componentType, int typeSize,
                 ProtectionDomain protectionDomain) {
        super(name, getObjectClass(), loader, typeSize, protectionDomain);
        this.componentType = componentType;
        testClassType();
    }

    /**
     * Returns the componentType.
     *
     * @return VmClass
     */
    public VmType<?> getComponentType() {
        return componentType;
    }

    /**
     * Is this class an array of primitive types
     *
     * @return boolean
     */
    public final boolean isPrimitiveArray() {
        if (isArray()) {
            return componentType.isPrimitive();
        } else {
            return false;
        }
    }

    /**
     * Test if this class is using the right modifiers
     *
     * @throws RuntimeException
     */
    private final void testClassType() throws RuntimeException {
        if (!isArray()) {
            throw new RuntimeException("Not an array class");
        }
        if (isInterface()) {
            throw new RuntimeException("Not an array class (interface-class)");
        }
    }

    /**
     * @see org.jnode.vm.classmgr.VmType#prepareForInstantiation()
     */
    protected void prepareForInstantiation() {
        // Nothing to do here
    }

    /**
     * Create the list of super classes for this class.
     *
     * @param allInterfaces
     * @return Super classes
     * @see org.jnode.vm.classmgr.VmType#createSuperClassesArray(java.util.HashSet)
     */
    protected VmType<?>[] createSuperClassesArray(HashSet<VmInterfaceClass<?>> allInterfaces) {

        final VmType[] compSuperClasses;
        final int compLength;

        if (componentType.isPrimitive()) {
            compSuperClasses = null;
            compLength = 0;
        } else {
            compSuperClasses = componentType.getSuperClassesArray();
            compLength = compSuperClasses.length;
        }

        final int length = compLength + 2 + allInterfaces.size();
        final VmType<?>[] array = new VmType[length];
        array[0] = this;
        array[1] = this.getSuperClass();
        for (int i = 0; i < compLength; i++) {
            array[2 + i] = compSuperClasses[i].getArrayClass(false);
        }

        int index = compLength + 2;
        for (VmInterfaceClass intfClass : allInterfaces) {
            array[index++] = intfClass;
        }

        if (false) {
            System.out.println("SuperClassesArray for " + getName() + ": " + getSuperClassDepth());
            for (int i = 0; i < length; i++) {
                System.out.println("[" + i + "]\t" + array[i].getName());
            }
        }

        return array;
    }

    /**
     * @see org.jnode.vm.classmgr.VmType#prepare()
     */
    void prepare() {
        componentType.prepare();
        super.prepare();
    }

    /**
     * @see org.jnode.vm.classmgr.VmType#compile()
     */
    void compile() {
        componentType.compile();
        super.compile();
    }

    /**
     * @see org.jnode.vm.classmgr.VmType#verify()
     */
    void verify() {
        componentType.verify();
        super.verify();
    }

    public final boolean isArray() {
        return true;
    }

    public final synchronized void incTotalLength(int len) {
        this.totalLength += len;
        if (len > maxLength) {
            maxLength = len;
        }
    }

    public long getTotalLength() {
        return totalLength;
    }

    public int getMaximumLength() {
        return maxLength;
    }
}
