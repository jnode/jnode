/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.vm.classmgr;

import java.io.PrintStream;
import java.util.ArrayList;

import org.jnode.util.NumberUtils;
import org.jnode.vm.VmSystemObject;

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
    private char[] pcTable;
    private byte[] methodIndexTable;

    /**
     * Create a new instance
     */
    public VmAddressMap() {
    }

    /**
     * Add an address-pc mapping
     * 
     * @param offset
     *            Offset from the start of the method
     * @param pc
     */
    public void add(VmMethod method, int pc, int offset) {
        if (offsetTable != null) { throw new RuntimeException(
                "Address table is locked"); }
        final AddressPcEntry entry = new AddressPcEntry(method, pc, offset);
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
     * Gets the linenumber of a given code offset.
     * 
     * @param offset
     * @return The linenumber for the given pc, or -1 is not found.
     */
    public String getLocationInfo(VmMethod expectedMethod, int offset) {
        final int[] offsetTable = this.offsetTable;
        final char[] pcTable = this.pcTable;
        final byte[] methodIndexTable = this.methodIndexTable;
        if (offsetTable != null) {
            final int length = offsetTable.length;
            char lastPC = 0;
            int lastMethodIdx = 0;
            char lastExpMethPC = 0;
            for (int i = 0; i < length; i++) {
                final int o = offsetTable[i];
                if (o > offset) {
                    break;
                } else {
                    lastMethodIdx = methodIndexTable[i];
                    lastPC = pcTable[i];
                    if (methodTable[lastMethodIdx] == expectedMethod) {
                        lastExpMethPC = lastPC;
                    }
                }
            }
            final VmMethod m = methodTable[ lastMethodIdx];
            final VmByteCode bc = m.getBytecode();
            if (bc != null) { 
                final int line = bc.getLineNr(lastPC);
                if (m != expectedMethod) {
                    // This is an inlined method
                    final int expMethLine = expectedMethod.getBytecode().getLineNr(lastExpMethPC);
                    final VmType mClass = m.getDeclaringClass();
                    if (mClass != expectedMethod.getDeclaringClass()) {
                        return expMethLine + " [" + m.getDeclaringClass().getName() + "#" + m.getName() + " " + line + "]";
                    } else {
                        return expMethLine + " [#" + m.getName() + " " + line + "]";
                    }
                } else {
                    // This is a non-inlined method
                    return String.valueOf(line);
                }
            }
        }
        return "?";
    }

    /**
     * Convert to a final contents. After a call to this method, the add method
     * cannot be used.
     */
    final void lock() {
        AddressPcEntry p = list;
        int count = 0;
        final ArrayList<VmMethod> methods = new ArrayList<VmMethod>();
        while (p != null) {
            count++;
            final VmMethod m = p.method;
            if (!methods.contains(m)) {
                methods.add(m);
            }
            p = p.next;
        }

        final int[] offsetTable = new int[ count];
        final char[] pcTable = new char[count];
        final byte[] methodIndexTable = new byte[count];
        this.methodTable = (VmMethod[]) methods.toArray(new VmMethod[ methods
                                                                      .size()]);
        p = list;
        int i = 0;
        int lastOffset = -1;
        while (p != null) {
            methodIndexTable[i] = (byte)methods.indexOf(p.method);
            pcTable[i] = p.pc;
            offsetTable[i] = p.offset;
            if (p.offset < lastOffset) { throw new InternalError(
                    "unordered offset found"); }
            lastOffset = p.offset;
            i++;
            p = p.next;
        }
        this.offsetTable = offsetTable;
        this.pcTable = pcTable;
        this.methodIndexTable = methodIndexTable;
        this.list = null;
    }

    public void writeTo(PrintStream out) {
        for (int i = 0; i < offsetTable.length; i++) {
            final int methodIdx = methodIndexTable[i];
            final int pc = pcTable[i];
            final int offset = offsetTable[i];

            out.println(methodTable[ methodIdx].getName() + ", pc[" + pc
                    + "]\t0x" + NumberUtils.hex(offset));
        }
    }

    static class AddressPcEntry extends VmSystemObject {

        final VmMethod method;

        final char pc;

        final int offset;

        AddressPcEntry next;

        public AddressPcEntry(VmMethod method, int pc, int offset) {
            this.method = method;
            this.pc = (char)pc;
            this.offset = offset;
        }
    }
}
