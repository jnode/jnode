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
 * Entry of a constantpool describing a method reference.
 *
 * @author epr
 */
public class VmConstMethodRef extends VmConstMemberRef {

    /**
     * The resolved method
     */
    private VmMethod vmMethod;

    /**
     * Constructor for VmMethodRef.
     *
     * @param cp
     * @param classIndex
     * @param nameTypeIndex
     */
    VmConstMethodRef(VmConstClass constClass, String name, String descriptor) {
        super(constClass, name, descriptor);
    }

    /**
     * Resolve the references of this constant to loaded VmXxx objects.
     *
     * @param clc
     */
    protected void doResolveMember(VmClassLoader clc) {
        final VmType vmClass = getConstClass().getResolvedVmClass();
        if (vmClass.isInterface()) {
            throw new IncompatibleClassChangeError(getClassName() + " must be a class");
        }
        final VmMethod vmMethod = vmClass.getMethod(getName(), getSignature());
        if (vmMethod == null) {
            throw new NoSuchMethodError(toString() + " in class " + getClassName());
        }
        if (vmMethod.isAbstract() && !vmClass.isAbstract()) {
            throw new AbstractMethodError("Abstract method " + toString() + " in class " + getClassName());
        }
        this.vmMethod = vmMethod;
    }

    /**
     * Returns the resolved method.
     *
     * @return VmMethod
     */
    public VmMethod getResolvedVmMethod() {
        if (vmMethod == null) {
            throw new NotResolvedYetException("vmMethod is not yet resolved");
        } else {
            return vmMethod;
        }
    }

    /**
     * Sets the resolved vmMethod. If the resolved vmMethod was already set,
     * any call to this method is silently ignored.
     *
     * @param vmMethod The vmMethod to set
     */
    public void setResolvedVmMethod(VmMethod vmMethod) {
        if (this.vmMethod == null) {
            this.vmMethod = vmMethod;
        }
    }

    /**
     * @see org.jnode.vm.classmgr.VmConstObject#getConstType()
     */
    public int getConstType() {
        return CONST_METHODREF;
    }
}
