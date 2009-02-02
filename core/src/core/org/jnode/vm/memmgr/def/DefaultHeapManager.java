/*
 * $Id$
 *
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
 
package org.jnode.vm.memmgr.def;

import java.io.PrintWriter;

import org.jnode.vm.MemoryBlockManager;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmMagic;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.GCStatistics;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.HeapStatistics;
import org.jnode.vm.memmgr.VmHeapManager;
import org.jnode.vm.scheduler.Monitor;
import org.jnode.vm.scheduler.VmProcessor;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Word;

@MagicPermission
public final class DefaultHeapManager extends VmHeapManager {

    /**
     * Default size in bytes of a new heap
     */
    public static final int DEFAULT_HEAP_SIZE = 16 * 1024 * 1024;

    /**
     * When this percentage of the free memory has been allocated, a GC is
     * triggered (0..1.0)
     */
    public static float GC_TRIGGER_PERCENTAGE = 0.75f;

    /**
     * The boot heap
     */
    private final VmBootHeap bootHeap;

    /**
     * The GC thread
     */
    private GCThread gcThread;

    /**
     * The finalizer thread
     */
    private FinalizerThread finalizerThread;

    /**
     * Monitor to synchronize heap access
     */
    private Monitor heapMonitor;

    /**
     * Are we low on memory
     */
    private boolean lowOnMemory;

    /**
     * Linked list of all heaps.
     */
    private VmDefaultHeap heapList;

    /**
     * The first heap.
     */
    private final VmDefaultHeap firstNormalHeap;

    /**
     * The heap currently used for allocation
     */
    private VmDefaultHeap currentHeap;

    /**
     * The heap used for allocations during a GC
     */
    private VmDefaultHeap gcHeap;

    /**
     * The class of the default heap type. Set by initialize
     */
    private final VmNormalClass<VmDefaultHeap> defaultHeapClass;

    /**
     * The number of allocated bytes since the last GC trigger
     */
    private int allocatedSinceGcTrigger;

    private int triggerSize = Integer.MAX_VALUE;

    private boolean gcActive;

    private GCManager gcManager;

    /**
     * Make this private, so we cannot be instantiated
     */
    @SuppressWarnings("unchecked")
    public DefaultHeapManager(VmClassLoader loader, HeapHelper helper)
        throws ClassNotFoundException {
        super(helper);
        this.bootHeap = new VmBootHeap(helper);
        // this.writeBarrier = new DefaultWriteBarrier(helper);
        setWriteBarrier(null);
        this.firstNormalHeap = new VmDefaultHeap(this);
        this.currentHeap = firstNormalHeap;
        this.heapList = firstNormalHeap;
        this.defaultHeapClass = (VmNormalClass<VmDefaultHeap>) loader.loadClass(
            VmDefaultHeap.class.getName(), true);
    }

    /**
     * Is the given address the address of an allocated object on this heap?
     *
     * @param ptr The address to examine.
     * @return True if the given address if a valid starting address of an
     *         object, false otherwise.
     */
    @Inline
    public final boolean isObject(Address ptr) {
        if (!ptr.toWord().and(Word.fromIntZeroExtend(ObjectLayout.OBJECT_ALIGN - 1)).isZero()) {
            // The object is not at an object aligned boundary
            return false;
        }
        if (bootHeap.isObject(ptr)) {
            return true;
        }
        VmDefaultHeap heap = heapList;
        while (heap != null) {
            if (heap.isObject(ptr)) {
                return true;
            }
            heap = heap.getNext();
        }
        return false;
    }

    /**
     * Is the system low on memory?
     *
     * @return boolean
     */
    public boolean isLowOnMemory() {
        return lowOnMemory;
    }

    /**
     * Start a garbage collection process
     */
    public final void gc() {
        gcThread.trigger(false);
    }

    /**
     * Gets the size of free memory in bytes.
     *
     * @return long
     */
    public long getFreeMemory() {
        Extent size = Extent.zero();
        VmDefaultHeap h = firstNormalHeap;
        while (h != null) {
            size = size.add(h.getFreeSize());
            h = h.getNext();
        }
        // size += (Unsafe.addressToLong(heapEnd) -
        // Unsafe.addressToLong(nextHeapPtr));
        size = size.add(Extent.fromLong(MemoryBlockManager.getFreeMemory()));
        return size.toLong();
    }

    /**
     * Gets the size of all memory in bytes.
     *
     * @return the size of all memory in bytes
     */
    public long getTotalMemory() {
        long size = bootHeap.getSize();
        VmDefaultHeap h = firstNormalHeap;
        while (h != null) {
            size += h.getSize();
            h = h.getNext();
        }
        // size += (Unsafe.addressToLong(heapEnd) -
        // Unsafe.addressToLong(nextHeapPtr));
        size += MemoryBlockManager.getFreeMemory();
        return size;
    }

    /**
     * Gets the first heap. All other heaps can be iterated through the
     * <code>getNext()</code> method.
     *
     * @return the first heap
     */
    public final VmDefaultHeap getHeapList() {
        return heapList;
    }

    // ------------------------------------------
    // Private natives
    // ------------------------------------------

    protected void initialize() {
        // Set the basic fields
        helper.bootArchitecture(false);
        final VmArchitecture arch = VmProcessor.current().getArchitecture();
        final int slotSize = arch.getReferenceSize();

        // Initialize the boot heap.
        bootHeap.initialize(helper.getBootHeapStart(), helper.getBootHeapEnd(),
            slotSize);

        // Initialize the first normal heap
        final Address ptr = helper.allocateBlock(Extent
            .fromIntZeroExtend(DEFAULT_HEAP_SIZE));
        firstNormalHeap.initialize(ptr, ptr.add(DEFAULT_HEAP_SIZE), slotSize);

        // Initialize the GC heap
        gcHeap = allocHeap(Extent.fromIntZeroExtend(DEFAULT_HEAP_SIZE), false);
        gcHeap.append(firstNormalHeap);

        // Initialize the total heap list.
        heapList = gcHeap;
    }

    public void start() {
        // Create a Heap monitor
        heapMonitor = new Monitor();
        final VmArchitecture arch = VmProcessor.current().getArchitecture();
        this.gcManager = new GCManager(this, arch);
        this.gcThread = new GCThread(gcManager, heapMonitor);
        this.finalizerThread = new FinalizerThread(this);
        gcThread.start();
        finalizerThread.start();
        // Calculate the trigger size
        triggerSize = (int) Math.min(Integer.MAX_VALUE, getFreeMemory()
            * GC_TRIGGER_PERCENTAGE);
    }

    /**
     * Allocate a new instance for the given class. Not that this method cannot
     * be synchronized, since obtaining a monitor might require creating one,
     * which in turn needs this method.
     *
     * @param vmClass
     * @param size
     * @return Object
     */
    protected Object allocObject(VmClassType<?> vmClass, int size) {
        // Make sure the class is initialized
        vmClass.initialize();

        final int alignedSize = ObjectLayout.objectAlign(size);
        // final Monitor mon = heapMonitor;

        VmDefaultHeap heap = currentHeap;
        Object result = null;
        int oomCount = 0;

        final Monitor m = heapMonitor;
        // final Monitor m = null;
        if (m != null) {
            m.enter();
        }
        try {
            if (gcActive) {
                if ((heapFlags & TRACE_ALLOC) != 0) {
                    debug("Using GC Heap type ");
                    debug(vmClass.getName());
                }
                result = gcHeap.alloc(vmClass, alignedSize);
                if (result == null) {
                    helper.die("Out of GC heap memory.");
                }
            } else {
                while (result == null) {
                    // The current heap is full
                    if (heap == null) {
                        // Unsafe.debug("allocHeap in allocObject(");
                        // Unsafe.debug(alignedSize);
                        // Unsafe.debug(") ");
                        int newHeapSize = DEFAULT_HEAP_SIZE;
                        if (size > newHeapSize) {
                            // this is a BIG object, try to allocate a new
                            // heap
                            // only for it
                            newHeapSize = size;
                        }
                        if ((heap = allocHeap(Extent
                            .fromIntZeroExtend(newHeapSize), true)) == null) {
                            lowOnMemory = true;
                            // It was not possible to allocate another heap.
                            // First try to GC, if we've done that before
                            // in this allocation, then we're in real panic.
                            if (oomCount == 0) {
                                oomCount++;
                                if ((heapFlags & TRACE_OOM) != 0) {
                                    debug("<oom/>");
                                }
                                gcThread.trigger(true);
                                heap = firstNormalHeap;
                                currentHeap = firstNormalHeap;
                            } else {
                                if ((heapFlags & TRACE_OOM) != 0) {
                                    debug("Out of memory in allocObject(");
                                    debug(size);
                                    debug(")");
                                }
                                throw OOME;
                                // Unsafe.die();
                            }
                        } else {
                            // Unsafe.debug("AO.G");
                            // We successfully allocated a new heap, set it
                            // to current, so we'll use it for the following
                            // allocations.
                            currentHeap = heap;
                        }
                    }

                    result = heap.alloc(vmClass, alignedSize);

                    if (result == null) {
                        heap = (VmDefaultHeap) heap.getNext();
                    }
                }
                lowOnMemory = false;

                allocatedSinceGcTrigger += alignedSize;
                if ((allocatedSinceGcTrigger > triggerSize)
                    && (gcThread != null)) {
                    if ((heapFlags & TRACE_TRIGGER) != 0) {
                        debug("<alloc:GC trigger/>");
                    }
                    allocatedSinceGcTrigger = 0;
                    gcThread.trigger(false);
                }
            }
            vmClass.incInstanceCount();
            // Allocated objects are initially black.
            VmMagic.setObjectFlags(result, Word
                .fromIntZeroExtend(ObjectFlags.GC_DEFAULT_COLOR));
        } finally {
            if (m != null) {
                m.exit();
            }
        }

        return result;
    }

    /**
     * Allocate a new heap with a given size. The heap object itself is
     * allocated on the new heap, so this method can be called even if all other
     * heaps are full.
     *
     * @param size
     * @return The heap
     */
    private VmDefaultHeap allocHeap(Extent size, boolean addToHeapList) {
        // Unsafe.debug("allocHeap");
        final Address start = helper.allocateBlock(size);
        // final Address start = MemoryBlockManager.allocateBlock(size);
        if (start == null) {
            return null;
        }
        final Address end = start.add(size);
        final int slotSize = VmProcessor.current().getArchitecture()
            .getReferenceSize();
        final VmDefaultHeap heap = VmDefaultHeap.setupHeap(helper, start,
            defaultHeapClass, slotSize);
        heap.initialize(start, end, slotSize);

        if (addToHeapList) {
            heapList.append(heap);
        }
        return heap;
    }

    /**
     * Print the statics on this object on out.
     */
    public void dumpStatistics(PrintWriter out) {
        out.println("WriteBarrier: " + getWriteBarrier());
    }

    /**
     * @return Returns the bootHeap.
     */
    final VmBootHeap getBootHeap() {
        return this.bootHeap;
    }

    /**
     * @param gcActive The gcActive to set.
     */
    final void setGcActive(boolean gcActive) {
        this.gcActive = gcActive;
    }

    /**
     * Sets the currentHeap to the first heap.
     */
    final void resetCurrentHeap() {
        this.currentHeap = this.firstNormalHeap;
        // Recalculate the trigger size
        triggerSize = (int) Math.min(Integer.MAX_VALUE, getFreeMemory()
            * GC_TRIGGER_PERCENTAGE);
    }

    /**
     * Sets the currentHeap to the first heap.
     */
    final void triggerFinalization() {
        finalizerThread.trigger(false);
    }

    public GCStatistics getStatistics() {
        return gcManager.getStatistics();
    }

    public HeapStatistics getHeapStatistics() {
        final DefHeapStatistics heapStatistics = new DefHeapStatistics();
        final HeapStatisticsVisitor heapStatisticsVisitor = new HeapStatisticsVisitor(
            heapStatistics);

        VmDefaultHeap heap = firstNormalHeap;
        final Word zero = Word.zero();

        while (heap != null) {
            heap.walk(heapStatisticsVisitor, false, zero, zero);
            heap = heap.getNext();
        }

        return heapStatistics;
    }

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#createProcessorHeapData(org.jnode.vm.scheduler.VmProcessor)
     */
    public Object createProcessorHeapData(VmProcessor cpu) {
        // No need here, so return null.
        return null;
    }

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#notifyClassResolved(org.jnode.vm.classmgr.VmType)
     */
    public void notifyClassResolved(VmType<?> vmType) {
        // Do nothing
    }

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#loadClasses(org.jnode.vm.classmgr.VmClassLoader)
     */
    public void loadClasses(VmClassLoader loader) throws ClassNotFoundException {
        loader.loadClass("org.jnode.vm.memmgr.def.VmBootHeap", true);
        loader.loadClass("org.jnode.vm.memmgr.def.VmDefaultHeap", true);
    }
}
