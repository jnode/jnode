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
 * @author epr
 */
public class VmInstanceField extends VmField {

    /**
     * Offset of this field in an object (used for instance fields only)
     */
    private int offset;

    /**
     * @param name
     * @param signature
     * @param modifiers
     * @param offset
     * @param declaringClass
     * @param slotSize
     */
    public VmInstanceField(
        String name,
        String signature,
        int modifiers,
        int offset,
        VmType declaringClass,
        int slotSize) {
        super(name, signature, modifiers, declaringClass, slotSize);
        if (Modifier.isStatic(modifiers)) {
            throw new IllegalArgumentException("Static field in VmInstanceField");
        }
        this.offset = offset;
    }

    /**
     * Gets the offset of this field in the object
     *
     * @return int
     */
    public final int getOffset() {
        return offset;
    }

    /**
     * Resolve the offset on this field in a class.
     *
     * @param classOffset
     */
    protected final void resolveOffset(int classOffset) {
        offset += classOffset;
    }

    /**
     * Override the offset of this field.
     *
     * @param offset
     */
    final void setOffset(int offset) {
        this.offset = offset;
    }
}
