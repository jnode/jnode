/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.VmAddress;
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
    private int sizeOffset;

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
    protected static VmAbstractHeap setupHeap(HeapHelper helper, VmAddress start,
            VmNormalClass heapClass, int slotSize) {
        final int headerSize = ObjectLayout
                .objectAlign((ObjectLayout.HEADER_SLOTS + 1) * slotSize);
        //final int flagsOffset = ObjectLayout.FLAGS_SLOT * slotSize;
        final int vmtOffset = ObjectLayout.TIB_SLOT * slotSize;
        final int sizeOffset = -((ObjectLayout.HEADER_SLOTS + 1) * slotSize);
        final int flagsOffset = ObjectLayout.FLAGS_SLOT * slotSize;

        // Setup a heap object, so the heap can initialize itself.
        VmAddress heapPtr = VmAddress.add(start, headerSize);
        final int heapObjSize = ObjectLayout.objectAlign(heapClass
                .getObjectSize());
        helper.setInt(heapPtr, sizeOffset, heapObjSize);
        helper.setInt(heapPtr, flagsOffset, ObjectFlags.GC_DEFAULT_COLOR);
        helper.setObject(heapPtr, vmtOffset, heapClass.getTIB());
        helper.clear(heapPtr, heapObjSize);

        VmDefaultHeap heap = (VmDefaultHeap) helper.objectAt(heapPtr);
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
    protected void initialize(VmAddress start, VmAddress end, int slotSize) {

        // Set my variables
        this.start = start;
        this.end = end;
        initializeAbstract(slotSize);
        this.sizeOffset = -((ObjectLayout.HEADER_SLOTS + 1) * slotSize);
        this.headerSize = ObjectLayout.objectAlign(this.headerSize + slotSize);
        final int size = getSize();

        final int mySize = helper.getInt(this, sizeOffset);
        final VmAddress myAddr = helper.addressOf(this);
        VmAddress firstObject;
        if (inHeap(Address.fromAddress(myAddr))) {
            firstObject = VmAddress.add(myAddr, mySize + headerSize);
        } else {
            firstObject = VmAddress.add(start, headerSize);
        }

        // Initialize an allocation bitmap
        final int allocationBits = size / ObjectLayout.OBJECT_ALIGN;
        final int allocationBitmapSize = ObjectLayout
                .objectAlign((allocationBits + 7) / 8);
        this.allocationBitmapPtr = firstObject;
        // Make the bitmap an object, so it is easy to manipulate.
        helper.setInt(allocationBitmapPtr, sizeOffset, allocationBitmapSize);
        helper.setInt(allocationBitmapPtr, flagsOffset, GC_DEFAULT_COLOR);
        helper.setObject(allocationBitmapPtr, tibOffset, VmType
                .getObjectClass().getTIB());
        firstObject = VmAddress.add(firstObject, allocationBitmapSize
                + headerSize);
        helper.clear(allocationBitmapPtr, allocationBitmapSize);
        this.allocationBitmap = helper.objectAt(allocationBitmapPtr);

        // Mark this heap in the allocation bitmap
        setAllocationBit(this, true);
        // Mark the allocation bitmap in the allocation bitmap
        setAllocationBit(allocationBitmap, true);

        // Initialize the remaining space as free object.
        final int remainingSize = (int) VmAddress.distance(end, firstObject);
        final Address ptr = Address.fromAddress(firstObject);
        helper.setInt(ptr, sizeOffset, remainingSize);
        helper.setObject(ptr, tibOffset, FREE);
        this.nextFreePtr = ptr;
        this.freeSize = remainingSize;
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
        final Offset tibOffset = Offset.fromIntZeroExtend(this.tibOffset);
        final int headerSize = this.headerSize;
        final Offset flagsOffset = Offset.fromIntZeroExtend(this.flagsOffset);
        final Offset sizeOffset = Offset.fromIntZeroExtend(this.sizeOffset);

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
        helper.clear(objectPtr.toAddress(), alignedSize);

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
            final int objSize = ptr.loadInt(Offset.fromIntSignExtend(sizeOffset));
            ptr.store(ObjectReference.fromObject(FREE), Offset.fromIntSignExtend(tibOffset));
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
        final Offset sizeOffset = Offset.fromIntZeroExtend(this.sizeOffset);
        final Offset tibOffset = Offset.fromIntZeroExtend(this.tibOffset);

        lock();
        try {
            Address firstFreePtr = null;
            while (offset < size) {
                final Address ptr = Address.fromAddress(start).add(offset);
                final int objSize = ptr.loadInt(sizeOffset);
                final int nextOffset = offset + objSize + headerSize;
                final Object vmt = ptr.loadObjectReference(tibOffset);
                if ((firstFreePtr == null) && (vmt == FREE)) {
                    firstFreePtr = ptr;
                }
                if ((vmt == FREE) && (nextOffset < size)) {
                    final Object nextVmt;
                    final Address nextObjectPtr = Address.fromAddress(start).add(nextOffset);
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
        final Offset sizeOffset = Offset.fromIntZeroExtend(this.sizeOffset);
        final Offset tibOffset = Offset.fromIntZeroExtend(this.tibOffset);
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
                    final Address ptr = Address.fromAddress(start).add(offset);
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
                final Address ptr = Address.fromAddress(start).add(offset);
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
