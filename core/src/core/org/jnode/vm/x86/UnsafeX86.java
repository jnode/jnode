/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.vm.VmAddress;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class UnsafeX86 {

    /**
     * Copies the bootstrap GDT.
     * @param gdt 
     * @return The array length of the required array.
     */
    static final native int getGDT(int[] gdt);
    
    /**
     * Copies the bootstrap TSS.
     * @param tss
     * @return The array length of the required array.
     */
    static final native int getTSS(int[] tss);

    /**
     * Gets the size required for the bootcode for an application processor.
     * @return The required size in bytes.
     */
    static final native int getAPBootCodeSize();
    
    /**
     * Prepare the bootcode.
     * This will copy the bootcode into the memory region and patches the
     * appropriate pointers.
     * @param memory
     * @param gdt
     */
    static final native void setupBootCode(VmAddress memory, int[] gdt, int[] tss);
}
