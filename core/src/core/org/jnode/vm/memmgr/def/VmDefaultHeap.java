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
 
package org.jnode.vm.memmgr.def;

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmMagic;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.HeapHelper;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * @author epr
 */
@MagicPermission
final class VmDefaultHeap extends VmAbstractHeap implements ObjectFlags {

    /**
     * Offset within this heap of the next free memory block
     */
    private Address nextFreePtr;

    /**
     * The allocation bitmap as object, so we won't throw it away in a GC cycle
     */
    private Object allocationBitmap;

    /**
     * The total size of free space
     */
    private Extent freeSize;

    /**
     * Offset (in bytes) from the start of an object to the size of an object
     */
    private Offset sizeOffset;

    /**
     * The next heap (linked list)
     */
    private VmDefaultHeap next;

    /**
     * Initialize this instance
     *
     * @param heapMgr
     */
    public VmDefaultHeap(DefaultHeapManager heapMgr) {
        super(heapMgr.getHelper());
    }

    /**
     * Setup the heap object according to the given start-address of the heap.
     *
     * @param start
     * @param heapClass
     * @param slotSize
     * @return the heap
     */
    protected static VmDefaultHeap setupHeap(HeapHelper helper, Address start,
                                             VmNormalClass<VmDefaultHeap> heapClass, int slotSize) {
        final int headerSize = ObjectLayout
            .objectAlign((ObjectLayout.HEADER_SLOTS + 1) * slotSize);
        final Offset vmtOffset = Offset.fromIntSignExtend(ObjectLayout.TIB_SLOT * slotSize);
        final Offset sizeOffset = Offset.fromIntSignExtend(-((ObjectLayout.HEADER_SLOTS + 1) * slotSize));
        final Offset flagsOffset = Offset.fromIntSignExtend(ObjectLayout.FLAGS_SLOT * slotSize);

        // Setup a heap object, so the heap can initialize itself.
        final Address heapPtr = start.add(headerSize);
        final Word heapObjSize = Word.fromIntZeroExtend(ObjectLayout.objectAlign(heapClass
            .getObjectSize()));
        final Word flags = Word.fromIntZeroExtend(ObjectFlags.GC_DEFAULT_COLOR);
        heapPtr.store(heapObjSize, sizeOffset);
        heapPtr.store(flags, flagsOffset);
        heapPtr.store(ObjectReference.fromObject(heapClass.getTIB()), vmtOffset);
        helper.clear(heapPtr, heapObjSize.toInt());

        VmDefaultHeap heap = (VmDefaultHeap) heapPtr.toObjectReference().toObject();
        heap.helper = helper;
        return heap;
    }

    /**
     * Append a new heap to the end of the linked list of heaps.
     *
     * @param newHeap
     */
    protected final void append(VmDefaultHeap newHeap) {
        VmDefaultHeap heap = this;
        while (heap.next != null) {
            heap = heap.next;
        }
        heap.next = newHeap;
    }

    /**
     * Gets the next heap in the linked list of heaps.
     *
     * @return Next heap
     */
    @Inline
    public final VmDefaultHeap getNext() {
        return next;
    }

    /**
     * Initialize this heap
     *
     * @param start    Start address of this heap
     * @param end      End address of this heap (first address after this heap)
     * @param slotSize
     */
    protected void initialize(Address start, Address end, int slotSize) {

        // Set my variables
        this.start = start;
        this.end = end;
        initializeAbstract(slotSize);
        this.sizeOffset = Offset.fromIntSignExtend(-((ObjectLayout.HEADER_SLOTS + 1) * slotSize));
        this.headerSize = ObjectLayout.objectAlign(this.headerSize + slotSize);
        final int size = getSize();

        final Address myAddr = ObjectReference.fromObject(this).toAddress();
        final Word mySize = myAddr.loadWord(sizeOffset);
        Address firstObject;
        if (inHeap(myAddr)) {
            firstObject = myAddr.add(mySize).add(headerSize);
        } else {
            firstObject = start.add(headerSize);
        }

        // Initialize an allocation bitmap
        final int allocationBits = size / ObjectLayout.OBJECT_ALIGN;
        final int allocationBitmapSize = ObjectLayout
            .objectAlign((allocationBits + 7) / 8);
        this.allocationBitmapPtr = firstObject;
        final Address bitmapPtr = this.allocationBitmapPtr;
        // Make the bitmap an object, so it is easy to manipulate.
        bitmapPtr.store(Word.fromIntZeroExtend(allocationBitmapSize), sizeOffset);
        bitmapPtr.store(Word.fromIntZeroExtend(GC_DEFAULT_COLOR), flagsOffset);
        bitmapPtr.store(ObjectReference.fromObject(VmType.getObjectClass().getTIB()), tibOffset);
        firstObject = firstObject.add(allocationBitmapSize + headerSize);
        helper.clear(allocationBitmapPtr, allocationBitmapSize);
        this.allocationBitmap = allocationBitmapPtr.toObjectReference().toObject();

        // Mark this heap in the allocation bitmap
        setAllocationBit(this, true);
        // Mark the allocation bitmap in the allocation bitmap
        setAllocationBit(allocationBitmap, true);

        // Initialize the remaining space as free object.
        final Word remainingSize = end.toWord().sub(firstObject.toWord());
        final Address ptr = firstObject;
        ptr.store(remainingSize, sizeOffset);
        ptr.store(ObjectReference.fromObject(FREE), tibOffset);
        this.nextFreePtr = ptr;
        this.freeSize = remainingSize.toExtent();
    }

    /**
     * Allocate a new instance for the given class. Not that this method cannot
     * be synchronized, since the synchronization is handled in VmHeap.
     *
     * @param vmClass
     * @param alignedSize
     * @return Object Null if no space is left.
     */
    protected Object alloc(VmClassType<?> vmClass, int alignedSize) {

        if (nextFreePtr.EQ(Address.zero())) { /* This heap is full */
            return null;
        }

        final Offset tibOffset = this.tibOffset;
        final Word headerSize = Word.fromIntZeroExtend(this.headerSize);
        final Offset flagsOffset = this.flagsOffset;
        final Offset sizeOffset = this.sizeOffset;

        Word alignedSizeW = Word.fromIntZeroExtend(alignedSize);
        final Word totalSize = alignedSizeW.add(headerSize);
        final Object tib = vmClass.getTIB();
        if (tib == null) {
            throw new IllegalArgumentException("vmClass.TIB is null");
        }
        //final int size = getSize();
        Address objectPtr = Address.zero();
        lock();
        try {
            // Search for the first free block that is large enough
            //Screen.debug("a");
            while (objectPtr == null) {
                final Address ptr = nextFreePtr;
                final Word objSize = ptr.loadWord(sizeOffset);
                final Object objVmt = ptr.loadObjectReference(tibOffset);
                final Address nextPtr = ptr.add(objSize.add(headerSize));
                if ((objVmt == FREE) && alignedSizeW.LE(objSize)) {
                    objectPtr = ptr;
                } else {
                    if (!inHeap(nextPtr)) {
                        // No large enough free space has been found
                        // A collect may recover smaller free spaces in this
                        // heap, but we leave that to a GC iteration.
                        nextFreePtr = Address.zero();
                        //Screen.debug("B");
                        return null;
                    } else {
                        this.nextFreePtr = nextPtr;
                    }
                }
            }
            //Screen.debug("A");

            final Word curFreeSize = objectPtr.loadWord(sizeOffset);
            if (curFreeSize.GT(totalSize)) {
                // Block is larger then we need, split it up.
                final Word newFreeSize = curFreeSize.sub(totalSize);
                /*if (newFreeSize <= headerSize) {
                    Unsafe.debug("Block splitup failed");
                    Unsafe.debug("\ncurFreeSize "); Unsafe.debug(curFreeSize);
                    Unsafe.debug("\ntotalSize   "); Unsafe.debug(totalSize);
                    Unsafe.debug("\nnewFreeSize "); Unsafe.debug(newFreeSize);
                    Unsafe.debug("\nheaderSize  "); Unsafe.debug(headerSize);
                    throw new Error("Block splitup failed");
                }*/
                final Address newFreePtr = objectPtr.add(totalSize);
                // Set the header for the remaining free block
                newFreePtr.store(newFreeSize, sizeOffset);
                newFreePtr.store(0, flagsOffset);
                newFreePtr.store(ObjectReference.fromObject(FREE), tibOffset);
                // Set the next free offset
                nextFreePtr = newFreePtr;
            } else {
                // The block is not large enough to split up, make the
                // new object the size of the free block.
                alignedSizeW = curFreeSize;
            }

            // Create the object header
            objectPtr.store(alignedSizeW, sizeOffset);
            objectPtr.store(0, flagsOffset);
            objectPtr.store(ObjectReference.fromObject(tib), tibOffset);
            // Mark the object in the allocation bitmap
            setAllocationBit(objectPtr, true);

            // Fix the freeSize
            freeSize = freeSize.sub(alignedSizeW);
        } finally {
            unlock();
        }

        // Clear the contents of the object.
        helper.clear(objectPtr, alignedSize);

        return objectPtr.toObjectReference().toObject();
    }

    /**
     * Mark the given object as free space.
     *
     * @param object
     */
    @Inline
    final void free(Object object) {
        final Address ptr = ObjectReference.fromObject(object).toAddress();
        final Word objSize = ptr.loadWord(sizeOffset);
        ptr.store(ObjectReference.fromObject(FREE), tibOffset);
        setAllocationBit(object, false);
        freeSize = freeSize.add(objSize);
    }

    /**
     * @return The free size
     * @see VmAbstractHeap#getFreeSize()
     */
    protected Extent getFreeSize() {
        return freeSize;
    }

    /**
     * Join all adjacent free spaces.
     *
     * @throws UninterruptiblePragma
     */
    protected final void defragment() throws UninterruptiblePragma {
        final Word size = Word.fromIntZeroExtend(getSize());
        final Word headerSize = Word.fromIntZeroExtend(this.headerSize);
        Word offset = headerSize;
        final Offset sizeOffset = this.sizeOffset;
        final Offset tibOffset = this.tibOffset;


        lock();
        try {
            Address firstFreePtr = Address.zero();
            while (offset.LT(size)) {
                final Address ptr = start.add(offset);
                final Word objSize = ptr.loadWord(sizeOffset);
                final Word nextOffset = offset.add(objSize).add(headerSize);
                final Object vmt = ptr.loadObjectReference(tibOffset);
                if ((firstFreePtr == null) && (vmt == FREE)) {
                    firstFreePtr = ptr;
                }
                if ((vmt == FREE) && (nextOffset.LT(size))) {
                    final Object nextVmt;
                    final Address nextObjectPtr = start.add(nextOffset);
                    nextVmt = nextObjectPtr.loadObjectReference(tibOffset);
                    if (nextVmt == FREE) {
                        // Combine two free spaces
                        Word nextObjSize = nextObjectPtr.loadWord(sizeOffset);
                        Word newObjSize = objSize.add(headerSize).add(nextObjSize);
                        ptr.store(newObjSize, sizeOffset);
                        // Do not increment offset here, because there may be
                        // another next free object, which we will combine
                        // in the next loop.
                    } else {
                        offset = nextOffset;
                    }
                } else {
                    offset = nextOffset;
                }
            }
            // Set the address of the next free block, to the first free block
            this.nextFreePtr = firstFreePtr;
        } finally {
            unlock();
        }
    }

    /**
     * Let all objects in this heap make a visit to the given visitor.
     *
     * @param visitor
     * @param locking If true, use lock/unlock while proceeding to the next object.
     */
    protected final void walk(ObjectVisitor visitor, boolean locking,
                              Word flagsMask, Word flagsValue) {
        // Go through the heap and call visit on each object
        final Word headerSize = Word.fromIntZeroExtend(this.headerSize);
        final Offset sizeOffset = this.sizeOffset;
        final Offset tibOffset = this.tibOffset;
        final Object FREE = this.FREE;
        Word offset = headerSize;
        final Word size = Word.fromIntZeroExtend(getSize());

        if (locking) {
            while (offset.LT(size)) {
                final Object tib;
                final Object object;
                final Word objSize;
                final Word flags;

                lock();
                try {
                    final Address ptr = start.add(offset);
                    object = ptr.toObjectReference().toObject();
                    tib = ptr.loadObjectReference(tibOffset);
                    objSize = ptr.loadWord(sizeOffset);
                    flags = (flagsMask.isZero()) ? Word.zero() : VmMagic.getObjectFlags(object).and(flagsMask);
                } finally {
                    unlock();
                }
                if (tib != FREE) {
                    if (flags.EQ(flagsValue)) {
                        if (!visitor.visit(object)) {
                            // Stop
                            return;
                        }
                    }
                }
                offset = offset.add(objSize).add(headerSize);
            }
        } else {
            while (offset.LT(size)) {
                final Address ptr = start.add(offset);
                final Object object = ptr.toObjectReference().toObject();
                final Object tib = ptr.loadObjectReference(tibOffset);
                final Word objSize = ptr.loadWord(sizeOffset);
                final Word flags = flagsMask.isZero() ? Word.zero() : VmMagic.getObjectFlags(object).and(flagsMask);
                if (tib != FREE) {
                    if (flags.EQ(flagsValue)) {
                        if (!visitor.visit(object)) {
                            // Stop
                            return;
                        }
                    }
                }
                offset = offset.add(objSize).add(headerSize);
            }
        }
    }
}
