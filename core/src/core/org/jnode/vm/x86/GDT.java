/*
 * $Id$
 */
package org.jnode.vm.x86;

import java.io.PrintStream;

import org.jnode.util.NumberUtils;
import org.jnode.vm.VmAddress;

/**
 * Global descriptor table wrapper.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class GDT {

    public static final int NULL_ENTRY = 0;

    public static final int KERNEL_CODE_ENTRY = 1;

    public static final int KERNEL_DATA_ENTRY = 2;

    public static final int USER_CODE_ENTRY = 3;

    public static final int USER_DATA_ENTRY = 4;

    public static final int TSS_ENTRY = 5;

    public static final int PROCESSOR_ENTRY = 6;

    private final int[] gdt;

    /**
     * Initialize this instance.
     */
    public GDT() {
        final int len = UnsafeX86.getGDT(null);
        this.gdt = new int[ len];
        UnsafeX86.getGDT(gdt);
    }

    /**
     * Gets the GDT 
     */
    public int[] getGdt() {
        return gdt;
    }

    /**
     * Sets the base address of an entry at a given index.
     * 
     * @param index
     * @param base
     */
    public final void setBase(int index, VmAddress base) {
        final int intBase = VmAddress.as32bit(base);
        final int idx = index * 2;
        gdt[ idx + 0] &= 0x0000FFFF; // Remove base bits
        gdt[ idx + 1] &= 0x00FFFF00; // Remove base bits
        gdt[ idx + 0] |= (intBase & 0x0000FFFF) << 16; // Base 0-15
        gdt[ idx + 1] |= (intBase >> 16) & 0x000000FF; // Base 16-23
        gdt[ idx + 1] |= intBase & 0xFF000000; // Base 23-31
    }
    
    public final void dump(PrintStream out) {
        for (int i = 0; i < gdt.length; i += 2) {
            out.println("GDT[" + i + "] " + NumberUtils.hex(gdt[i+1]) + " " + NumberUtils.hex(gdt[i]));
        }
    }
}