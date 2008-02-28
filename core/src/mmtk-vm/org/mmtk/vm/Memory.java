/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
 
package org.mmtk.vm;

import org.jnode.vm.Unsafe;
import org.jnode.vm.VirtualMemoryRegion;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.annotation.Inline;
import org.mmtk.plan.BasePlan;
import org.mmtk.policy.ImmortalSpace;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

/**
 * Memory part of the VM interface of MMTk.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author <a href="http://cs.anu.edu.au/~Steve.Blackburn">Steve Blackburn</a>
 * @author Perry Cheng
 */
public class Memory {

    /** Space reserved by the bootimage */
    private static ImmortalSpace bootSpace;

    /**
     * Gets the start of the virtual address space where object may be found.
     * 
     * @return the start address.
     */
    public static Address HEAP_START() {
        return Vm.getArch().getStart(VirtualMemoryRegion.HEAP);
    }

    /**
     * Gets the end of the virtual address space where object may be found.
     * 
     * @return the end address.
     */
    public static Address HEAP_END() {
        return Vm.getArch().getEnd(VirtualMemoryRegion.HEAP);
    }

    /**
     * Gets the start of the virtual address space available to MMTk for
     * allocating objects.
     * 
     * @return the start address.
     */
    public static Address AVAILABLE_START() {
        return Vm.getArch().getStart(VirtualMemoryRegion.AVAILABLE);
    }

    /**
     * Gets the end of the virtual address space available to MMTk for
     * allocating objects.
     * 
     * @return the end address.
     */
    public static Address AVAILABLE_END() {
        return Vm.getArch().getEnd(VirtualMemoryRegion.AVAILABLE);
    }

    /**
     * Return the space associated with/reserved for the VM. In the case of
     * Jikes RVM this is the boot image space.
     * <p>
     * The boot image space must be mapped at the start of available virtual
     * memory, hence we use the constructor that requests the lowest address in
     * the address space. The address space awarded to this space depends on the
     * order in which the request is made. If this request is not the first
     * request for virtual memory then the Space allocator will die with an
     * error stating that the request could not be satisfied. The remedy is to
     * ensure it is initialized first.
     * 
     * @return The space managed by the virtual machine. In this case, the boot
     *         image space is returned.
     */
    public static ImmortalSpace getVMSpace() {
        if (bootSpace == null) {
            final VmArchitecture arch = Vm.getArch();
            final long bootSize = AVAILABLE_START().sub(HEAP_START().toWord())
                    .toLong();
            final int bootSizeMb = (int) (bootSize >>> 20);
            bootSpace = new ImmortalSpace("boot",
                    BasePlan.DEFAULT_POLL_FREQUENCY, bootSizeMb, false);
        }
        return bootSpace;
    }

    /**
     * Global preparation for a collection.
     */
    public static void globalPrepareVMSpace() {
    }

    /**
     * Thread-local preparation for a collection.
     */
    public static void localPrepareVMSpace() {
    }

    /**
     * Thread-local post-collection work.
     */
    public static void localReleaseVMSpace() {
    }

    /**
     * Global post-collection work.
     */
    public static void globalReleaseVMSpace() {
    }

    /**
     * Sets the range of addresses associated with a heap.
     * 
     * @param id
     *            the heap identifier
     * @param start
     *            the address of the start of the heap
     * @param end
     *            the address of the end of the heap
     */
    public static void setHeapRange(int id, Address start, Address end) {
        // TODO Understand me
    }

    /**
     * Maps an area of virtual memory.
     * 
     * @param start
     *            the address of the start of the area to be mapped
     * @param size
     *            the size, in bytes, of the area to be mapped
     * @return 0 if successful, otherwise the system errno
     */
    public static int mmap(Address start, int size) {
        if (false) {
            Unsafe.debug("mmap ");
            Unsafe.debug(start);
            Unsafe.debug(" sz ");
            Unsafe.debug(size);
        }

        if (Vm.getArch().mmap(VirtualMemoryRegion.HEAP, start,
                Extent.fromIntZeroExtend(size), Address.max())) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Protects access to an area of virtual memory.
     * 
     * @param start
     *            the address of the start of the area to be mapped
     * @param size
     *            the size, in bytes, of the area to be mapped
     * @return <code>true</code> if successful, otherwise <code>false</code>
     */
    public static boolean mprotect(Address start, int size) {
        // Unimplemented for now
        return false;
    }

    /**
     * Allows access to an area of virtual memory.
     * 
     * @param start
     *            the address of the start of the area to be mapped
     * @param size
     *            the size, in bytes, of the area to be mapped
     * @return <code>true</code> if successful, otherwise <code>false</code>
     */
    public static boolean munprotect(Address start, int size) {
        // Unimplemented for now
        return false;
    }

    /**
     * Zero a region of memory.
     * 
     * @param start
     *            Start of address range (inclusive)
     * @param len
     *            Length in bytes of range to zero Returned: nothing
     */
    public static void zero(Address start, Extent len) {
        Plan.getInstance().getHeapHelper().clear(start, len);
    }

    /**
     * Zero a range of pages of memory.
     * 
     * @param start
     *            Start of address range (must be a page address)
     * @param len
     *            Length in bytes of range (must be multiple of page size)
     */
    public static void zeroPages(Address start, int len) {
        Plan.getInstance().getHeapHelper().clear(start, len);
    }

    /**
     * Logs the contents of an address and the surrounding memory to the error
     * output.
     * 
     * @param start
     *            the address of the memory to be dumped
     * @param beforeBytes
     *            the number of bytes before the address to be included
     * @param afterBytes
     *            the number of bytes after the address to be included
     */
    public static void dumpMemory(Address start, int beforeBytes, int afterBytes) {
    }

    /*
     * Utilities from the VM class
     */

    @Inline
    public static void sync() {
        // TODO Understand me
    }

    @Inline
    public static void isync() {
        // TODO Understand me
    }
}
