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
 * A constant object that needs to be resolved.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmResolvableConstObject extends VmConstObject {

    private boolean resolved = false;

    public VmResolvableConstObject() {
    }

    /**
     * Resolve the references of this constant to loaded VmXxx objects.
     *
     * @param clc
     */
    public void resolve(VmClassLoader clc) {
        if (!resolved) {
            doResolve(clc);
            resolved = true;
        }
    }

    /**
     * Returns the resolved.
     *
     * @return boolean
     */
    public boolean isResolved() {
        return resolved;
    }

    /**
     * Resolve the references of this constant to loaded VmXxx objects.
     *
     * @param clc
     */
    protected abstract void doResolve(VmClassLoader clc);
}
