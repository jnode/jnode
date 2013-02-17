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
 * Entry of a constantpool describing a field reference.
 *
 * @author Ewout Prangsma (ewout@users.sourceforge.net)
 */
public final class VmConstFieldRef extends VmConstMemberRef {

    /**
     * The reference to the resolved field
     */
    private VmField vmResolvedField;

    /**
     * Constructor for VmFieldRef.
     *
     * @param cp
     * @param classIndex
     * @param nameTypeIndex
     */
    VmConstFieldRef(VmConstClass constClass, String name, String descriptor) {
        super(constClass, name, descriptor);
    }

    /**
     * Resolve the references of this constant to loaded VmXxx objects.
     *
     * @param clc
     */
    protected void doResolveMember(VmClassLoader clc) {
        VmType vmClass = getConstClass().getResolvedVmClass();
        vmResolvedField = vmClass.getField(getName());
        if (vmResolvedField == null) {
            throw new NoSuchFieldError(toString() + " in class " + getClassName());
        }
    }

    /**
     * Returns the resolved field.
     *
     * @return VmField
     */
    public VmField getResolvedVmField() {
        if (vmResolvedField == null) {
            throw new NotResolvedYetException("vmField is not yet resolved");
        } else {
            return vmResolvedField;
        }
    }

    /**
     * Sets the resolved vmField. If the resolved vmField was already set, any
     * call to this method is silently ignored.
     *
     * @param vmField The vmField to set
     */
    public void setResolvedVmField(VmField vmField) {
        if (this.vmResolvedField == null) {
            this.vmResolvedField = vmField;
        }
    }

    /**
     * Is this a field of double width (double, long)
     *
     * @return boolean
     */
    public boolean isWide() {
        return Modifier.isWide(getSignature());
    }

    /**
     * @see org.jnode.vm.classmgr.VmConstObject#getConstType()
     */
    public final int getConstType() {
        return CONST_FIELDREF;
    }
}
