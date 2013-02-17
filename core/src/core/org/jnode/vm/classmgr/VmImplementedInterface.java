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

import org.jnode.vm.objects.VmSystemObject;

/**
 * Element of a class that represents a single implemented interface.
 *
 * @author epr
 */
public final class VmImplementedInterface extends VmSystemObject {

    /**
     * The name of the interface class or the resolved class
     */
    private Object data;

    /**
     * Create a new instance
     *
     * @param className
     */
    protected VmImplementedInterface(String className) {
        if (className == null) {
            throw new IllegalArgumentException("className cannot be null");
        }
        this.data = className;
    }

    /**
     * Create a new instance
     *
     * @param vmClass
     */
    protected VmImplementedInterface(VmType vmClass) {
        if (vmClass == null) {
            throw new IllegalArgumentException("vmClass cannot be null");
        }
        if (vmClass instanceof VmInterfaceClass) {
            this.data = (VmInterfaceClass) vmClass;
        } else {
            throw new IllegalArgumentException("vmClass must be an interface class");
        }
    }

    /**
     * Gets the resolved interface class.
     *
     * @return The resolved class
     */
    public VmInterfaceClass<?> getResolvedVmClass()
        throws NotResolvedYetException {
        final Object data = this.data;
        if (data instanceof String) {
            throw new NotResolvedYetException((String) data);
        }
        return (VmInterfaceClass<?>) data;
    }

    /**
     * Resolve the members of this object.
     *
     * @param clc
     * @throws ClassNotFoundException
     */
    protected void resolve(VmClassLoader clc)
        throws ClassNotFoundException {
        final Object data = this.data;
        if (data instanceof String) {
            final String className = (String) data;
            final VmType<?> type = clc.loadClass(className, true);
            if (type instanceof VmInterfaceClass) {
                this.data = (VmInterfaceClass<?>) type;
            } else {
                throw new ClassNotFoundException("Class " + className + " is not an interface");
            }
            type.link();
        }
    }

    /**
     * @return Returns the className.
     */
    final String getClassName() {
        final Object data = this.data;
        if (data instanceof String) {
            return (String) data;
        } else {
            return ((VmInterfaceClass<?>) data).getName();
        }
    }

    /**
     * Convert myself into a String representation
     *
     * @return String
     */
    public String toString() {
        return "_I_" + Mangler.mangleClassName(getResolvedVmClass().getName());
    }
}
