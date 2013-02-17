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
 
package org.jnode.vm.facade;

import java.io.PrintWriter;

import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmType;
import org.vmmagic.unboxed.Address;

/**
 * Interface with the heap manager.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public interface VmHeapManager {

    /**
     * A new type has been resolved by the VM. Create a new MM type to reflect
     * the VM type, and associate the MM type with the VM type.
     *
     * @param vmType The newly resolved type
     */
    void notifyClassResolved(VmType<?> vmType);

    /**
     * Is the given address the address of an allocated object on this heap?
     *
     * @param ptr The address to examine.
     * @return True if the given address if a valid starting address of an
     *         object, false otherwise.
     */
    boolean isObject(Address ptr);

    /**
     * Create a new array
     *
     * @param arrayCls
     * @param dim
     * @return The new instance
     */
    Object newArray(VmArrayClass<?> arrayCls, int dim);

    /**
     * Create a new instance of a given class with a given object size (in
     * bytes)
     *
     * @param loadClass
     * @param size
     * @return The new instance
     */
    Object newInstance(VmType<?> loadClass, int size);

    /**
     * Create a new instance of a given class
     *
     * @param vmClass
     * @return The new instance
     */
    Object newInstance(VmType<?> vmClass);

    /**
     * Print the statistics on this object on out.
     */
    void dumpStatistics(PrintWriter out);

    /**
     * Get this heap's current flags
     *
     * @return the flags
     */
    int getHeapFlags();

    /**
     * Get this heap's statistics.
     * @param objectFilter The optional filter to apply to objects found on heap. 
     * @return the heap statistics
     */
    HeapStatistics getHeapStatistics(ObjectFilter objectFilter);

    /**
     * Get this heap GC's statistics.
     *
     * @return the heap GC statistics
     */
    GCStatistics getStatistics();

    /**
     * Set this heap's flags
     *
     * @param flags the new heap flags
     * @return the previous heap flags
     */
    int setHeapFlags(int flags);

    /**
     * Create an exact clone of the given object
     *
     * @param obj
     * @return Object
     */
    Object clone(Cloneable obj);

    /**
     * Start a garbage collection process.
     */
    void gc();

    /**
     * Gets the size of all memory in bytes.
     *
     * @return the size of all memory in bytes
     */
    long getTotalMemory();

    /**
     * Gets the size of free memory in bytes.
     *
     * @return long
     */
    long getFreeMemory();

    /**
     * Is the system low on memory?
     *
     * @return boolean
     */
    boolean isLowOnMemory();

    /**
     * Start this heap manager.
     */
    void start();

    /**
     * Gets the write barrier used by this heap manager (if any).
     *
     * @return The write barrier, or null if no write barrier is used.
     */
    VmWriteBarrier getWriteBarrier();

    /**
     * Create a per processor data structure for use by the heap manager.
     *
     * @param vmProcessor
     */
    Object createProcessorHeapData(VmProcessor vmProcessor);
}
