/*
 * $Id$
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
public class VmAddressMap extends VmSystemObject {

    private AddressPcEntry list;

    private VmMethod[] methodTable;

    private int[] table;

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
        if (table != null) { throw new RuntimeException(
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
        final int[] table = this.table;
        if (table != null) {
            final int length = table.length;
            int lastPC = 0;
            int lastMethodIdx = 0;
            int lastExpMethPC = 0;
            for (int i = 0; i < length; i += 3) {
                final int o = table[ i + 2];
                if (o > offset) {
                    break;
                } else {
                    lastMethodIdx = table[ i + 0];
                    lastPC = table[ i + 1];
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
        final ArrayList methods = new ArrayList();
        while (p != null) {
            count++;
            final VmMethod m = p.method;
            if (!methods.contains(m)) {
                methods.add(m);
            }
            p = p.next;
        }

        final int[] table = new int[ count * 3];
        this.methodTable = (VmMethod[]) methods.toArray(new VmMethod[ methods
                                                                      .size()]);
        p = list;
        int i = 0;
        int lastOffset = -1;
        while (p != null) {
            table[ i + 0] = methods.indexOf(p.method);
            table[ i + 1] = p.pc;
            table[ i + 2] = p.offset;
            if (p.offset < lastOffset) { throw new InternalError(
                    "unordered offset found"); }
            lastOffset = p.offset;
            i += 3;
            p = p.next;
        }
        this.table = table;
        this.list = null;
    }

    public void writeTo(PrintStream out) {
        for (int i = 0; i < table.length; i += 3) {
            final int methodIdx = table[ i + 0];
            final int pc = table[ i + 1];
            final int offset = table[ i + 2];

            out.println(methodTable[ methodIdx].getName() + ", pc[" + pc
                    + "]\t0x" + NumberUtils.hex(offset));
        }
    }

    static class AddressPcEntry extends VmSystemObject {

        final VmMethod method;

        final int pc;

        final int offset;

        AddressPcEntry next;

        public AddressPcEntry(VmMethod method, int pc, int offset) {
            this.method = method;
            this.pc = pc;
            this.offset = offset;
        }
    }
}