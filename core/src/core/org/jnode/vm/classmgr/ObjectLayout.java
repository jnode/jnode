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

import org.jnode.annotation.Inline;

/**
 * <description>
 *
 * @author epr
 */
public class ObjectLayout {

    /**
     * The offset of the flags of an object from the start of the object. This
     * value must be multiplied by {@link org.jnode.vm.BaseVmArchitecture#getReferenceSize() SLOT_SIZE}
     * to get the offset in bytes.
     */
    public static final int FLAGS_SLOT = -2;

    /**
     * The offset of the TIB of an object from the start of the object. This
     * value must be multiplied by {@link org.jnode.vm.BaseVmArchitecture#getReferenceSize() SLOT_SIZE}
     * to get the offset in bytes.
     */
    public static final int TIB_SLOT = -1;

    /**
     * The size of the header of an object. This value must be multiplied by
     * {@link org.jnode.vm.BaseVmArchitecture#getReferenceSize() SLOT_SIZE} to get the size in bytes.
     */
    public static final int HEADER_SLOTS = 2;

    /**
     * The number of bytes object are aligned on in memory.
     */
    public static final int OBJECT_ALIGN = 8;

    /**
     * The fixed length (in elements) of an Interface Method Table.
     *
     * @see IMTBuilder
     */
    public static final int IMT_LENGTH = 64;

    /**
     * Returns the given value aligned to OBJECT_ALIGN.
     *
     * @param value
     * @return int
     */
    @Inline
    public static int objectAlign(int value) {
        return (value + OBJECT_ALIGN - 1) & ~(OBJECT_ALIGN - 1);
    }
}
