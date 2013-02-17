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
 * VM representation of a special (&lt;init&gt;) method.
 *
 * @author epr
 */
public final class VmStaticField extends VmField implements VmSharedStaticsEntry, VmIsolatedStaticsEntry {

    /**
     * The index in the statics table
     */
    private final int staticsIndex;

    /**
     * Is this static field shared
     */
    private final boolean shared;

    /**
     * @param name
     * @param signature
     * @param modifiers
     * @param staticsIndex
     * @param declaringClass
     * @param slotSize
     */
    public VmStaticField(
        String name,
        String signature,
        int modifiers,
        int staticsIndex,
        VmType declaringClass,
        int slotSize,
        boolean shared) {
        super(name, signature, modifiers, declaringClass, slotSize);
        if (!Modifier.isStatic(modifiers)) {
            throw new IllegalArgumentException("Instance field in VmStaticField");
        }
        this.staticsIndex = staticsIndex;
        this.shared = shared;
    }

    /**
     * Gets the index of this field in the shared statics table.
     *
     * @return Returns the staticsIndex.
     * @throws IllegalStateException If this field is not shared
     */
    public final int getSharedStaticsIndex() {
        if (!shared) {
            throw new IllegalStateException("Static field is not shared");
        }
        return this.staticsIndex;
    }

    /**
     * Gets the index of this field in the isolated statics table.
     *
     * @return Returns the staticsIndex.
     * @throws IllegalStateException If this field is shared
     */
    public final int getIsolatedStaticsIndex() {
        if (shared) {
            throw new IllegalStateException("Static field is not isolated");
        }
        return this.staticsIndex;
    }

    /**
     * Is this a shared static field.
     *
     * @return Returns the shared.
     */
    public final boolean isShared() {
        return shared;
    }
}
