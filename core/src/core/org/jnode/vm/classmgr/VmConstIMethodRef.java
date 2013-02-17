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

/**
 * Entry of a constantpool describing an interface method reference.
 *
 * @author epr
 */
public final class VmConstIMethodRef extends VmConstMethodRef {

    /**
     * The selector of this methods name&type
     */
    private int selector = -1;

    /**
     * Constructor for VmIMethodRef.
     *
     * @param cp
     * @param classIndex
     * @param nameTypeIndex
     */
    VmConstIMethodRef(VmConstClass constClass, String name, String descriptor) {
        super(constClass, name, descriptor);
    }

    /**
     * Resolve the references of this constant to loaded VmXxx objects.
     *
     * @param clc
     */
    protected void doResolveMember(VmClassLoader clc) {
        final VmType vmClass = getConstClass().getResolvedVmClass();
        if (!vmClass.isInterface()) {
            throw new IncompatibleClassChangeError(getClassName() + " must be an interface");
        }
        final VmMethod vmMethod;
        vmMethod = vmClass.getMethod(getName(), getSignature());
        if (vmMethod == null) {
            throw new NoSuchMethodError(toString() + " in class " + getClassName());
        }
        this.selector = vmMethod.getSelector();
        setResolvedVmMethod(vmMethod);
    }

    /**
     * Gets the selector of this methods name&amp;type
     *
     * @return int
     */
    public int getSelector() {
        return selector;
    }

    /**
     * @see org.jnode.vm.classmgr.VmConstObject#getConstType()
     */
    public final int getConstType() {
        return CONST_IMETHODREF;
    }
}
