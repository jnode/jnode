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

import java.io.PrintWriter;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmStaticsAllocator extends VmStaticsBase {

    private byte[] types;

    private final int[] typeCounter = new int[MAX_TYPE + 1];

    private int next;

    private transient boolean locked;

    /**
     * Initialize this instance
     */
    public VmStaticsAllocator(int size) {
        this.types = new byte[size];
    }

    /**
     * Allocate an entry.
     *
     * @param type
     * @param length
     * @return the index of the allocated entry.
     */
    final synchronized int alloc(byte type, int length) {
        if (locked) {
            throw new RuntimeException("Locked");
        }
        final int idx = next;
        types[idx] = type;
        typeCounter[type]++;
        next += length;
        return idx;
    }

    /**
     * Is the entry are the given offset of the given type?
     *
     * @param index
     * @param type
     * @return
     */
    final void testType(int index, byte type) {
        if (types[index] != type) {
            throw new IllegalArgumentException("Type error " + types[index]);
        }
    }

    /**
     * Get the statics type at a given index
     *
     * @return int
     */
    public final int getType(int index) {
        return types[index];
    }

    final byte[] getTypes() {
        return types;
    }

    final int getLength() {
        return next;
    }

    final int getCapacity() {
        return types.length;
    }

    public final void dumpStatistics(PrintWriter out) {
        out.println("  #static int fields  " + typeCounter[TYPE_INT]);
        out.println("  #static long fields " + typeCounter[TYPE_LONG]);
        out.println("  #methods            " + typeCounter[TYPE_METHOD_CODE]);
        out.println("  #types              " + typeCounter[TYPE_CLASS]);
        out.println("  table.length        " + next);
    }
}
