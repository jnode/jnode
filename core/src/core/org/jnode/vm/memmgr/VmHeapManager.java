/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
import org.jnode.annotation.Inline;
import org.jnode.annotation.MagicPermission;
import org.jnode.annotation.NoInline;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmMagic;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.facade.GCStatistics;
import org.jnode.vm.facade.HeapStatistics;
import org.jnode.vm.facade.ObjectFilter;
import org.jnode.vm.facade.VmProcessor;
import org.jnode.vm.facade.VmWriteBarrier;
import org.jnode.vm.objects.VmSystemObject;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public abstract class VmHeapManager extends VmSystemObject implements org.jnode.vm.facade.VmHeapManager {

    public static final int TRACE_BASIC = 1;   // enable basic debugging of GC phases
    public static final int TRACE_ALLOC = 2;   // enable debugging of GC internal allocation
    public static final int TRACE_TRIGGER = 4;   // enable debugging of GC triggering / scheduling
    public static final int TRACE_OOM = 8;   // enable debugging of OOM events
    public static final int TRACE_AD_HOC = 16;  // enable ad hoc debugging
    public static final int TRACE_FLAGS = 31;  // all of the above

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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
                    + arrayCls.getName() + ']');
        }

        final int slotSize = getCurrentProcessor().getArchitecture()
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
     * {@inheritDoc}
     */
    public abstract boolean isLowOnMemory();

    /**
     * {@inheritDoc}
     */
    public abstract void gc();

    /**
     * {@inheritDoc}
     */
    public final Object clone(Cloneable object) {
        testInited();
        final VmClassType<?> objectClass = VmMagic.getObjectType(object);
        final Address objectPtr = ObjectReference.fromObject(object)
            .toAddress();
        final int size;
        if (objectClass.isArray()) {
            final int slotSize = getCurrentProcessor().getArchitecture()
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
     * {@inheritDoc}
     */
    public abstract long getFreeMemory();

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public abstract void dumpStatistics(PrintWriter out);

    /**
     * {@inheritDoc}
     */
    public abstract GCStatistics getStatistics();

    /**
     * {@inheritDoc}
     */
    public abstract HeapStatistics getHeapStatistics(ObjectFilter objectFilter);

    /**
     * {@inheritDoc}
     */
    public abstract Object createProcessorHeapData(VmProcessor cpu);

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public int getHeapFlags() {
        return heapFlags;
    }

    /**
     * {@inheritDoc}
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
     * Output a a number as debug message controlled by the heap trace flags.
     *
     * @param number
     */
    protected void debug(int number) {
        if ((heapFlags & TRACE_FLAGS) != 0) {
            Unsafe.debug(number);
        }
    }

    protected static final org.jnode.vm.scheduler.VmProcessor getCurrentProcessor() {
        return org.jnode.vm.scheduler.VmProcessor.current();
    }
}
