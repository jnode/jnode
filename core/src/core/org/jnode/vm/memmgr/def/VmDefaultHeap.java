/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmMagic;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.HeapHelper;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * @author epr
 */
public class VmDefaultHeap extends VmAbstractHeap implements ObjectFlags {

    /** Offset within this heap of the next free memory block */
    private Address nextFreePtr;

    /** The allocation bitmap as object, so we won't throw it away in a GC cycle */
    private Object allocationBitmap;

    /** The total size of free space */
    private int freeSize;

    /** Offset (in bytes) from the start of an object to the size of an object */
    private Offset sizeOffset;

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
    protected static VmAbstractHeap setupHeap(HeapHelper helper, Address start,
            VmNormalClass heapClass, int slotSize) {
        final int headerSize = ObjectLayout
                .objectAlign((ObjectLayout.HEADER_SLOTS + 1) * slotSize);
        final Offset vmtOffset = Offset.fromIntSignExtend(ObjectLayout.TIB_SLOT * slotSize);
        final Offset sizeOffset = Offset.fromIntSignExtend(-((ObjectLayout.HEADER_SLOTS + 1) * slotSize));
        final Offset flagsOffset = Offset.fromIntSignExtend(ObjectLayout.FLAGS_SLOT * slotSize);

        // Setup a heap object, so the heap can initialize itself.
        final Address heapPtr = start.add(headerSize);
        final int heapObjSize = ObjectLayout.objectAlign(heapClass
                .getObjectSize());
        heapPtr.store(heapObjSize, sizeOffset);
        heapPtr.store(ObjectFlags.GC_DEFAULT_COLOR, flagsOffset);
        heapPtr.store(ObjectReference.fromObject(heapClass.getTIB()), vmtOffset);
        helper.clear(heapPtr, heapObjSize);

        VmDefaultHeap heap = (VmDefaultHeap) heapPtr.toObjectReference().toObject();
        heap.helper = helper;
        return heap;
    }

    /**
     * Initialize this heap
     * 
     * @param start
     *            Start address of this heap
     * @param end
     *            End address of this heap (first address after this heap)
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
        final int mySize = myAddr.loadInt(sizeOffset);
        Address firstObject;
        if (inHeap(myAddr)) {
            firstObject = myAddr.add(mySize + headerSize);
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
        bitmapPtr.store(allocationBitmapSize, sizeOffset);
        bitmapPtr.store(GC_DEFAULT_COLOR, flagsOffset);
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
        this.freeSize = remainingSize.toInt();
    }

    /**
     * Allocate a new instance for the given class. Not that this method cannot
     * be synchronized, since the synchronization is handled in VmHeap.
     * 
     * @param vmClass
     * @param alignedSize
     * @return Object Null if no space is left.
     */
    protected Object alloc(VmClassType vmClass, int alignedSize) {

        if (nextFreePtr == null) { /* This heap is full */
        return null; }

        final int totalSize = alignedSize + headerSize;
        final Object tib = vmClass.getTIB();
        if (tib == null) {
            throw new IllegalArgumentException("vmClass.TIB is null");
        }
        //final int size = getSize();
        final Offset tibOffset = this.tibOffset;
        final int headerSize = this.headerSize;
        final Offset flagsOffset = this.flagsOffset;
        final Offset sizeOffset = this.sizeOffset;

        Address objectPtr = null;
        lock();
        try {
            // Search for the first free block that is large enough
            //Screen.debug("a");
            while (objectPtr == null) {
                final Address ptr = nextFreePtr;
                final int objSize = ptr.loadInt(sizeOffset);
                final Object objVmt = ptr.loadObjectReference(tibOffset);
                final Address nextPtr = ptr.add(objSize + headerSize);
                if ((objVmt == FREE) && (alignedSize <= objSize)) {
                    objectPtr = ptr;
                } else {
                    if (!inHeap(nextPtr)) {
                        // No large enough free space has been found
                        // A collect may recover smaller free spaces in this
                        // heap, but we leave that to a GC iteration.
                        nextFreePtr = null;
                        //Screen.debug("B");
                        return null;
                    } else {
                        this.nextFreePtr = nextPtr;
                    }
                }
            }
            //Screen.debug("A");

            final int curFreeSize = objectPtr.loadInt(sizeOffset);
            if (curFreeSize > totalSize) {
                // Block is larger then we need, split it up.
                final int newFreeSize = curFreeSize - totalSize;
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
                alignedSize = curFreeSize;
            }

            // Create the object header
            objectPtr.store(alignedSize, sizeOffset);
            objectPtr.store(0, flagsOffset);
            objectPtr.store(ObjectReference.fromObject(tib), tibOffset);
            // Mark the object in the allocation bitmap
            setAllocationBit(objectPtr, true);

            // Fix the freeSize
            freeSize -= alignedSize;
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
    protected final void free(Object object) {
        lock();
        try {
        	final Address ptr = ObjectReference.fromObject(object).toAddress();
            final int objSize = ptr.loadInt(sizeOffset);
            ptr.store(ObjectReference.fromObject(FREE), tibOffset);
            setAllocationBit(object, false);
            freeSize += objSize;
        } finally {
            unlock();
        }
    } 

    /**
     * @see VmAbstractHeap#getFreeSize()
     * @return The free size
     */
    protected int getFreeSize() {
        return freeSize;
    }

    /**
     * Join all adjacent free spaces.
     * 
     * @throws UninterruptiblePragma
     */
    protected final void defragment() throws UninterruptiblePragma {
        final int size = getSize();
        int offset = headerSize;
        final Offset sizeOffset = this.sizeOffset;
        final Offset tibOffset = this.tibOffset;

        lock();
        try {
            Address firstFreePtr = null;
            while (offset < size) {
                final Address ptr = start.add(offset);
                final int objSize = ptr.loadInt(sizeOffset);
                final int nextOffset = offset + objSize + headerSize;
                final Object vmt = ptr.loadObjectReference(tibOffset);
                if ((firstFreePtr == null) && (vmt == FREE)) {
                    firstFreePtr = ptr;
                }
                if ((vmt == FREE) && (nextOffset < size)) {
                    final Object nextVmt;
                    final Address nextObjectPtr = start.add(nextOffset);
                    nextVmt = nextObjectPtr.loadObjectReference(tibOffset);
                    if (nextVmt == FREE) {
                        // Combine two free spaces
                        int nextObjSize = nextObjectPtr.loadInt(sizeOffset);
                        int newObjSize = objSize + headerSize + nextObjSize;
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
     * @param locking
     *            If true, use lock/unlock while proceeding to the next object.
     */
    protected final void walk(ObjectVisitor visitor, boolean locking,
            int flagsMask, int flagsValue) {
        // Go through the heap and call visit on each object
        final int headerSize = this.headerSize;
        final Offset sizeOffset = this.sizeOffset;
        final Offset tibOffset = this.tibOffset;
        final Object FREE = this.FREE;
        int offset = headerSize;
        final int size = getSize();

        if (locking) {
            while (offset < size) {
                final Object tib;
                final Object object;
                final int objSize;
                final int flags;

                lock();
                try {
                    final Address ptr = start.add(offset);
                    object = ptr.toObjectReference().toObject();
                    tib = ptr.loadObjectReference(tibOffset);
                    objSize = ptr.loadInt(sizeOffset);
                    flags = (flagsMask == 0) ? 0 : (VmMagic.getObjectFlags(object) & flagsMask);
                } finally {
                    unlock();
                }
                if (tib != FREE) {
                    if (flags == flagsValue) {
                        if (!visitor.visit(object)) {
                            // Stop
                            offset = size;
                        }
                    }
                }
                offset += objSize + headerSize;
            }
        } else {
            while (offset < size) {
                final Address ptr = start.add(offset);
                final Object object = ptr.toObjectReference().toObject();
                final Object tib = ptr.loadObjectReference(tibOffset);
                final int objSize = ptr.loadInt(sizeOffset);
                final int flags = (flagsMask == 0) ? 0 : (VmMagic.getObjectFlags(object) & flagsMask);
                if (tib != FREE) {
                    if (flags == flagsValue) {
                        if (!visitor.visit(object)) {
                            // Stop
                            offset = size;
                        }
                    }
                }
                offset += objSize + headerSize;
            }
        }
    }
}
