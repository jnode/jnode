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

import org.jnode.vm.InternString;

/**
 * Entry of a constantpool describing a class reference.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmConstClass extends VmResolvableConstObject {

    /**
     * The classname, when not yet resolved, or the VmType when resolved
     */
    private Object data;

    /**
     * Initialize this instance.
     *
     * @param name
     */
    VmConstClass(String name) {
        this.data = InternString.internString(name.replace('/', '.'));
    }

    /**
     * Gets the name of the class this constant is a reference to
     *
     * @return String
     */
    public String getClassName() {
        final Object data = this.data;
        if (data instanceof String) {
            return (String) data;
        } else {
            return ((VmType<?>) data).getName();
        }
    }

    /**
     * Resolve the references of this constant to loaded VmXxx objects.
     *
     * @param clc
     */
    protected void doResolve(VmClassLoader clc) {
        final Object data = this.data;
        if (data instanceof String) {
            final String name = (String) data;
            try {
                this.data = clc.loadClass(name, true);
            } catch (ClassNotFoundException ex) {
                throw (NoClassDefFoundError) new NoClassDefFoundError(name)
                    .initCause(ex);
            }
        }
    }

    /**
     * Convert myself into a String representation
     *
     * @return String
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "ConstClass: " + getClassName();
    }

    /**
     * Returns the vmClass.
     *
     * @return VmClass
     */
    public final VmType<?> getResolvedVmClass() {
        final Object data = this.data;
        if (data instanceof String) {
            throw new RuntimeException("vmClass is not yet resolved");
        } else {
            return (VmType<?>) data;
        }
    }

    /**
     * Sets the resolved vmClass. If the resolved vmClass was already set, any
     * call to this method is silently ignored.
     *
     * @param vmClass The vmClass to set
     */
    public void setResolvedVmClass(VmType vmClass) {
        if (data instanceof String) {
            data = vmClass;
        }
    }

    /**
     * @see org.jnode.vm.classmgr.VmConstObject#getConstType()
     */
    public final int getConstType() {
        return CONST_CLASS;
    }
}
