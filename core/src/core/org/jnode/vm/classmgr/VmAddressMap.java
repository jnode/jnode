/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import java.io.PrintStream;

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
    public void add(int pc, int offset) {
        if (table != null) { throw new RuntimeException(
                "Address table is locked"); }
        final AddressPcEntry entry = new AddressPcEntry(pc, offset);
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
     * Find the PC for the given address
     * 
     * @param offset
     *            Offset from the start of the method
     * @return The pc
     */
    public int findPC(int offset) {
        final int[] table = this.table;
        if (table != null) {
            final int length = table.length;
            int lastPC = 0;
            for (int i = 0; i < length; i += 2) {
                final int o = table[ i + 1];
                if (o > offset) {
                    return lastPC;
                } else {
                    lastPC = table[ i + 0];
                }
            }
            return lastPC;
        } else {
            return 0;
        }
    }

    /**
     * Convert to a final contents. After a call to this method, the add method
     * cannot be used.
     */
    final void lock() {
        AddressPcEntry p = list;
        int count = 0;
        while (p != null) {
            count++;
            p = p.next;
        }

        final int[] table = new int[ count * 2];
        p = list;
        int i = 0;
        int lastOffset = -1;
        while (p != null) {
            table[ i + 0] = p.pc;
            table[ i + 1] = p.offset;
            if (p.offset < lastOffset) { throw new VirtualMachineError(
                    "unordered offset found"); }
            lastOffset = p.offset;
            i += 2;
            p = p.next;
        }
        this.table = table;
        this.list = null;
    }

    public void writeTo(PrintStream out) {
        for (int i = 0; i < table.length; i += 2) {
            final int pc = table[ i + 0];
            final int offset = table[ i + 1];

            out.println("PC[" + pc + "]\t0x" + NumberUtils.hex(offset));
        }
    }

    static class AddressPcEntry extends VmSystemObject {

        final int pc;

        final int offset;

        AddressPcEntry next;

        public AddressPcEntry(int pc, int offset) {
            this.pc = pc;
            this.offset = offset;
        }
    }
}