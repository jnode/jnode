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
 * <description>
 *
 * @author epr
 */
public class VmObject {

    /**
     * The offset of the size of an object from the start of the object. This
     * value must be multipled by SLOT_SIZE to get the offset in bytes.
     */
    public static final int SIZE_OFFSET = -2;

    /**
     * The offset of the VMT of an object from the start of the object. This
     * value must be multipled by SLOT_SIZE to get the offset in bytes.
     */
    public static final int VMT_OFFSET = -1;

    /**
     * The size of the header of an object. This value must be multipled by
     * SLOT_SIZE to get the size in bytes.
     */
    public static final int HEADER_SIZE = 2;

    /**
     * The number of bytes object are aligned on in memory.
     */
    public static final int OBJECT_ALIGN = 8;

    public static final int SIZE_MASK = 0xFFFFFFFC;

    /**
     * Returns the given value aligned to OBJECT_ALIGN.
     *
     * @param value
     * @return int
     */
    public static int objectAlign(int value) {
        return (value + OBJECT_ALIGN - 1) & ~(OBJECT_ALIGN - 1);
    }
}
