/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.vm.isolate;

import org.jnode.vm.VmSystemObject;

/**
 * Holder for variables specific to an isolate.
 * This class can be compared to ThreadLocal.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmIsolateLocal<T> extends VmSystemObject {

    /**
     * Object used in root isolate
     */
    private T rootObject;

    /**
     * Gets the stored object reference.
     *
     * @return the stored object reference
     */
    public T get() {
        if (VmIsolate.isRoot()) {
            return rootObject;
        } else {
            return (T) VmIsolate.currentIsolate().getIsolateLocalMap().get(this);
        }
    }

    /**
     * Sets the stored object reference for the current isolate.
     */
    public void set(T object) {
        if (VmIsolate.isRoot()) {
            rootObject = object;
        } else {
            VmIsolate.currentIsolate().getIsolateLocalMap().put(this, object);
        }
    }
}
