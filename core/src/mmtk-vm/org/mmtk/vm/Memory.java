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

package org.mmtk.vm;

import org.mmtk.policy.ImmortalSpace;
import org.vmmagic.pragma.InlinePragma;
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

    public static Address HEAP_START() {
        return Address.zero();
    }

    public static Address HEAP_END() {
        return Address.zero();
    }

    public static Address AVAILABLE_START() {
        return Address.zero();
    }

    public static Address AVAILABLE_END() {
        return Address.zero();
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
        return null;
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
        return 0;
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

    public static void sync() throws InlinePragma {
    }

    public static void isync() throws InlinePragma {
    }
}
