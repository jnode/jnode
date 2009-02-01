/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.vm.memmgr;

import java.io.PrintWriter;

import org.jnode.vm.Unsafe;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.NoInline;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.scheduler.VmProcessor;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public abstract class VmHeapManager extends VmSystemObject {

    public static int TRACE_BASIC = 1;   // enable basic debugging of GC phases
    public static int TRACE_ALLOC = 2;   // enable debugging of GC internal allocation
    public static int TRACE_TRIGGER = 4;   // enable debugging of GC triggering / scheduling
    public static int TRACE_OOM = 8;   // enable debugging of OOM events
    public static int TRACE_AD_HOC = 16;  // enable ad hoc debugging
    public static int TRACE_FLAGS = 31;  // all of the above

    /**
     * Has this manager been initialized yet
     */
    private boolean inited = false;

    /**
     * The current debug flags
     */
    protected int heapFlags = TRACE_BASIC | TRACE_OOM | TRACE_ALLOC;

    protected OutOfMemoryError OOME;

    /**
     * The memory access helper
     */
    protected final HeapHelper helper;

    /**
     * Write barrier used
     */
    private VmWriteBarrier writeBarrier;

    /**
     * Initialize this instance
     */
    public VmHeapManager(HeapHelper helper) {
        this.helper = helper;
    }

    /**
     * Initialize this manager. This method is called before the first call to
     * allocObject.
     */
    protected abstract void initialize();

    /**
     * Start any threads needed by this manager.
     */
    public abstract void start();

    /**
     * Create a new instance of a given class
     *
     * @param cls
     * @return The new instance
     */
    @Inline
    public final Object newInstance(VmType<?> cls) {
        return newInstance(cls, ((VmNormalClass<?>) cls).getObjectSize());
    }

    /**
     * Create a new instance of a given class with a given object size (in
     * bytes)
     *
     * @param cls
     * @param size
     * @return The new instance
     */
    @NoInline
    public final Object newInstance(VmType<?> cls, int size) {
        testInited();
        cls.initialize();
        if (cls.isArray()) {
            throw new IllegalArgumentException(
                "Cannot instantiate an array like this");
        }
        if (cls.isInterface()) {
            throw new IllegalArgumentException(
                "Cannot instantiate an interface");
        }

        final Object obj = allocObject((VmNormalClass<?>) cls, size);
        if (obj == null) {
            if ((heapFlags & TRACE_OOM) != 0) {
                debug("Out of memory");
            }
            throw OOME;
        }
        return obj;
    }

    /**
     * Create a new array
     *
     * @param arrayCls
     * @param elements
     * @return The new instance
     */
    public final Object newArray(VmArrayClass<?> arrayCls, int elements) {
        testInited();
        if (elements < 0) {
            throw new NegativeArraySizeException(
                "elements must be greater or equal to 0");
        }
        if (!arrayCls.isArray()) {
            throw new IllegalArgumentException(
                "Cannot instantiate a non-array like this ["
                    + arrayCls.getName() + "]");
        }

        final int slotSize = VmProcessor.current().getArchitecture()
            .getReferenceSize();
        final int elemSize;
        if (arrayCls.isPrimitiveArray()) {
            switch (arrayCls.getSecondNameChar()) {
                case 'B': // byte
                case 'Z': // boolean
                    elemSize = 1;
                    break;
                case 'C': // char
                case 'S': // short
                    elemSize = 2;
                    break;
                case 'I': // int
                case 'F': // float
                    elemSize = 4;
                    break;
                case 'D': // double
                case 'J': // long
                    elemSize = 8;
                    break;
                default:
                    throw new IllegalArgumentException(arrayCls.getName());
            }
        } else {
            elemSize = slotSize;
        }

        arrayCls.incTotalLength(elements);
        final Object obj = newArray0(arrayCls, elemSize, elements, slotSize);
        if (obj == null) {
            if ((heapFlags & TRACE_OOM) != 0) {
                debug("Out of memory");
            }
            throw OOME;
        }

        return obj;
    }

    /**
     * Is the system low on memory?
     *
     * @return boolean
     */
    public abstract boolean isLowOnMemory();

    /**
     * Start a garbage collection process
     */
    public abstract void gc();

    /**
     * Create an exact clone of the given object
     *
     * @param object
     * @return Object
     */
    public final Object clone(Cloneable object) {
        testInited();
        final VmClassType<?> objectClass = VmMagic.getObjectType(object);
        final Address objectPtr = ObjectReference.fromObject(object)
            .toAddress();
        final int size;
        if (objectClass.isArray()) {
            final int slotSize = VmProcessor.current().getArchitecture()
                .getReferenceSize();
            final VmArrayClass<?> arrayClass = (VmArrayClass<?>) objectClass;
            final int length = objectPtr.loadInt(Offset
                .fromIntSignExtend(VmArray.LENGTH_OFFSET * slotSize));
            final int elemSize = arrayClass.getComponentType().getTypeSize();
            size = (VmArray.DATA_OFFSET * slotSize) + (length * elemSize);
        } else {
            final VmNormalClass<?> normalClass = (VmNormalClass<?>) objectClass;
            size = normalClass.getObjectSize();
        }
        final Object newObj = allocObject(objectClass, size);
        helper.copy(objectPtr, ObjectReference.fromObject(newObj).toAddress(),
            Extent.fromIntZeroExtend(size));
        return newObj;
    }

    /**
     * Gets the size of free memory in bytes.
     *
     * @return long
     */
    public abstract long getFreeMemory();

    /**
     * Gets the size of all memory in bytes.
     *
     * @return the size of all memory in bytes
     */
    public abstract long getTotalMemory();

    /**
     * Allocate a new instance for the given class. Not that this method cannot
     * be synchronized, since obtaining a monitor might require creating one,
     * which in turn needs this method.
     *
     * @param vmClass The class to allocate
     * @param size    The size of the class data, without any header
     * @return The allocated object
     */
    protected abstract Object allocObject(VmClassType<?> vmClass, int size);

    /**
     * Create a new instance of an array with a given class, no constructor will
     * be called, the object will be filled with zeros.
     *
     * @param vmClass
     * @param elemSize The length in bytes of each element
     * @param elements The number of elements
     * @param slotSize
     * @return The new instance
     */
    @Inline
    private final Object newArray0(VmClassType vmClass, int elemSize,
                                   int elements, int slotSize) {
        final int size = (VmArray.DATA_OFFSET * slotSize)
            + (elemSize * elements);
        final Object array = allocObject(vmClass, size);
        final Address arrayPtr = ObjectReference.fromObject(array).toAddress();
        arrayPtr.store(elements, Offset.fromIntSignExtend(VmArray.LENGTH_OFFSET
            * slotSize));
        return array;
    }

    /**
     * Is the given address the address of an allocated object on this heap?
     *
     * @param ptr The address to examine.
     * @return True if the given address if a valid starting address of an
     *         object, false otherwise.
     */
    public abstract boolean isObject(Address ptr);

    @Inline
    private final void testInited() {
        if (!inited) {
            // Unsafe.debug("testInitid.initialize");
            initialize();
            inited = true;
            OOME = new OutOfMemoryError();
            // Unsafe.debug("eo-testInitid.initialize");
        }
    }

    /**
     * @return Returns the helper.
     */
    public final HeapHelper getHelper() {
        return this.helper;
    }

    /**
     * Gets the write barrier used by this heap manager (if any).
     *
     * @return The write barrier, or null if no write barrier is used.
     */
    public final VmWriteBarrier getWriteBarrier() {
        return writeBarrier;
    }

    /**
     * Sets the write barrier.
     * Call this method in the constructor.
     *
     * @param barrier
     */
    protected final void setWriteBarrier(VmWriteBarrier barrier) {
        this.writeBarrier = barrier;
    }

    /**
     * Print the statics on this object on out.
     */
    public abstract void dumpStatistics(PrintWriter out);

    public abstract GCStatistics getStatistics();

    public abstract HeapStatistics getHeapStatistics();

    /**
     * Create a per processor data structure for use by the heap manager.
     *
     * @param cpu
     */
    public abstract Object createProcessorHeapData(VmProcessor cpu);

    /**
     * A new type has been resolved by the VM. Create a new MM type to reflect
     * the VM type, and associate the MM type with the VM type.
     *
     * @param vmType The newly resolved type
     */
    public abstract void notifyClassResolved(VmType<?> vmType);

    /**
     * Load classes required by this memory manager at build time.
     *
     * @param loader
     */
    public abstract void loadClasses(VmClassLoader loader)
        throws ClassNotFoundException;

    /**
     * Get this heap's current flags
     *
     * @return the flags
     */
    public int getHeapFlags() {
        return heapFlags;
    }

    /**
     * Set this heap's flags
     *
     * @param reapFlags the new heap flags
     * @return the previous heap flags
     */
    public int setHeapFlags(int reapFlags) {
        int res = this.heapFlags;
        this.heapFlags = reapFlags;
        return res;
    }

    /**
     * Output a debug message controlled by the heap trace flags.
     *
     * @param text
     */
    public void debug(String text) {
        if ((heapFlags & TRACE_FLAGS) != 0) {
            Unsafe.debug(text);
        }
    }

    /**
     * Output a debug message controlled by the heap trace flags.
     *
     * @param text
     */
    protected void debug(int number) {
        if ((heapFlags & TRACE_FLAGS) != 0) {
            Unsafe.debug(number);
        }
    }
}
