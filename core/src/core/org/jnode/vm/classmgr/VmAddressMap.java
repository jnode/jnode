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

import java.io.PrintStream;
import java.util.ArrayList;

import org.jnode.util.NumberUtils;
import org.jnode.vm.objects.VmSystemObject;

/**
 * This table is a mapping between a program counter and an address of that
 * program counter in compiled code.
 *
 * @author epr
 */
public final class VmAddressMap extends VmSystemObject {

    private AddressPcEntry list;

    private VmMethod[] methodTable;

    private int[] offsetTable;

    /**
     * Program counter at a given index
     */
    private char[] pcTable;

    /**
     * Index in the methodTable at a index (null is methodTable.length == 1)
     */
    private byte[] methodIndexTable;

    /**
     * Inline depth at a given index (0..) (null is methodTable.length == 1)
     */
    private byte[] inlineDepthTable;

    /**
     * Create a new instance
     */
    public VmAddressMap() {
    }

    /**
     * Add an address-pc mapping.  This method cannot be used after {@link #lock()} 
     * has been called.
     *
     * @param offset Offset from the start of the method
     * @param pc
     * @param method
     * @param inlineDepth
     */
    public void add(VmMethod method, int pc, int offset, int inlineDepth) {
        if (offsetTable != null) {
            throw new RuntimeException("Address table is locked");
        }
        final AddressPcEntry entry = new AddressPcEntry(method, pc, offset, inlineDepth);
        if (list == null) {
            list = entry;
        } else {
            // Sort on offset (from low to high)
            if (offset < list.offset) {
                entry.next = list;
                list = entry;
            } else {
                AddressPcEntry p = list;
                while ((p.next != null) && (offset > p.next.offset)) {
                    p = p.next;
                }
                entry.next = p.next;
                p.next = entry;
            }
        }
    }

    /**
     * Gets the last known address index that corresponds to the given code offset.
     *
     * @param offset
     * @return the index, or {@code -1} if the offset is not found in the offset table.
     */
    public final int getIndexForOffset(int offset) {
        final int[] offsetTable = this.offsetTable;
        if (offsetTable != null) {
            final int length = offsetTable.length;
            for (int i = 1; i < length; i++) {
                if (offsetTable[i] > offset) {
                    return i - 1;
                }
            }
        }
        return -1;
    }

    /**
     * Gets the method at the given index.
     *
     * @param index
     * @return the method
     */
    public final VmMethod getMethodAtIndex(int index) {
        if (index < 0) {
            return null;
        } else if (methodIndexTable == null) {
            return methodTable[0];
        } else {
            return methodTable[methodIndexTable[index] & 0xFF];
        }
    }

    /**
     * Gets the program counter at the given index.
     *
     * @param index
     * @return the program counter
     */
    public final int getProgramCounterAtIndex(int index) {
        if (index < 0) {
            return 0;
        } else {
            return pcTable[index];
        }
    }

    /**
     * Gets the index that contains the call to the (inlined) method that is
     * identified by the given index.
     *
     * @param index
     * @return The call-site index, or {@code -1} if there is no call-site within this
     *         address map.
     */
    public final int getCallSiteIndex(int index) {
        final byte[] inlineDepthTable = this.inlineDepthTable;
        if ((index >= 0) && (inlineDepthTable != null)) {
            final int tableLength = inlineDepthTable.length;
            index = Math.min(index, tableLength - 1);
            final int depth = inlineDepthTable[index--];
            while (index >= 0) {
                if (inlineDepthTable[index] == depth - 1) {
                    return index;
                } else {
                    index--;
                }
            }
        }
        return -1;
    }

    /**
     * Convert the address map to its final form. After a call to this method, 
     * the {@link #add(VmMethod, int, int, int)} method cannot be used.
     */
    final void lock() {
        AddressPcEntry p = list;
        int count = 0;
        int maxInlineDepth = 0;
        final ArrayList<VmMethod> methods = new ArrayList<VmMethod>();
        while (p != null) {
            count++;
            maxInlineDepth = Math.max(maxInlineDepth, p.inlineDepth & 0xFF);
            final VmMethod m = p.method;
            if (!methods.contains(m)) {
                methods.add(m);
            }
            p = p.next;
        }

        final int methodCount = methods.size();
        final int[] offsetTable = new int[count];
        final char[] pcTable = new char[count];
        final byte[] methodIndexTable = (methodCount > 1) ? new byte[count] : null;
        final byte[] inlineDepthTable = (maxInlineDepth == 0) ? null : new byte[count];
        this.methodTable = (VmMethod[]) methods.toArray(new VmMethod[methodCount]);

        p = list;
        int i = 0;
        int lastOffset = -1;
        while (p != null) {
            if (methodIndexTable != null) {
                methodIndexTable[i] = (byte) methods.indexOf(p.method);
            }
            pcTable[i] = p.pc;
            offsetTable[i] = p.offset;
            if (inlineDepthTable != null) {
                inlineDepthTable[i] = p.inlineDepth;
            }
            if (p.offset < lastOffset) {
                throw new InternalError("unordered offset found");
            }
            lastOffset = p.offset;
            i++;
            p = p.next;
        }
        this.offsetTable = offsetTable;
        this.pcTable = pcTable;
        this.methodIndexTable = methodIndexTable;
        this.inlineDepthTable = inlineDepthTable;
        this.list = null;
    }

    public void writeTo(PrintStream out) {
        for (int i = 0; i < offsetTable.length; i++) {
            final int methodIdx = (methodIndexTable != null) ? methodIndexTable[i] : 0;
            final int pc = pcTable[i];
            final int offset = offsetTable[i];

            out.println(methodTable[methodIdx].getName() + ", pc[" + pc
                + "]\t0x" + NumberUtils.hex(offset));
        }
    }

    static class AddressPcEntry extends VmSystemObject {

        final VmMethod method;

        final char pc;

        final int offset;

        final byte inlineDepth;

        AddressPcEntry next;

        public AddressPcEntry(VmMethod method, int pc, int offset, int inlineDepth) {
            this.method = method;
            this.pc = (char) pc;
            this.offset = offset;
            this.inlineDepth = (byte) inlineDepth;
        }
    }
}
