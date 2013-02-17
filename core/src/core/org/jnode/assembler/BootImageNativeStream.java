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
 
package org.jnode.assembler;

/**
 * Interface that contains methods that can be used during the creation
 * of the boot image, but not at runtime.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface BootImageNativeStream {

    /**
     * Write a reference to the given object
     *
     * @param object
     */
    public void writeObjectRef(Object object);

    /**
     * Write a reference to the given object
     *
     * @param object
     */
    public void setObjectRef(int offset, Object object);

    /**
     * Allocate space from the current position and return the current position.
     *
     * @param size
     * @return The start of the allocated region.
     */
    public int allocate(int size);

}
