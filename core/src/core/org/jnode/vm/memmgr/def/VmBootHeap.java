/**
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmMagic;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.memmgr.HeapHelper;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author epr
 */
public class VmBootHeap extends VmAbstractHeap {

    //public static final String START_FIELD_NAME = "start";
    //public static final String END_FIELD_NAME = "end";
    /** Offset (in bytes) from the start of an object to the size of an object */
    private int sizeOffset;

    /**
     * Initialize this instance
     * 
     * @param helper
     */
    public VmBootHeap(HeapHelper helper) {
        super(helper);
    }

    /**
     * @param vmClass
     * @param size
     * @see VmAbstractHeap#alloc(VmClassType, int)
     * @return The new object
     */
    protected Object alloc(VmClassType vmClass, int size) {
        return null;
    }

    /**
     * Mark the given object as free space.
     * 
     * @param object
     */
    protected final void free(Object object) {
        // This heap does not free memory.
    }

    /**
     * @param start
     * @param end
     * @param slotSize
     * @see VmAbstractHeap#initialize(Address, Address, int) For this class,
     *      the parameters are always null, so ignore them!
     */
    protected void initialize(Address start, Address end, int slotSize) {
        //Unsafe.debug("bootheap.initialize");
        //Unsafe.debug("start"); Unsafe.debug(Unsafe.addressToInt(start));
        //Unsafe.debug("end"); Unsafe.debug(Unsafe.addressToInt(end));

        this.start = start;
        this.end = end;
        initializeAbstract(slotSize);
        this.sizeOffset = -((ObjectLayout.HEADER_SLOTS + 1) * slotSize);
        this.headerSize = ObjectLayout.objectAlign(this.headerSize + slotSize);

        // Create an allocation bitmap
        final int heapSize = getSize();
        final int bits = ObjectLayout.objectAlign(heapSize)
                / ObjectLayout.OBJECT_ALIGN;
        final int bitmapSize = ObjectLayout.objectAlign(bits / 8);
        allocationBitmapPtr = helper.allocateBlock(bitmapSize);
        //allocationBitmapPtr = MemoryBlockManager.allocateBlock(bitmapSize);

        // Initialize the allocation bitmap
        helper.clear(allocationBitmapPtr, bitmapSize);
        // Go through the heap and mark all objects in the allocation bitmap.
        int offset = headerSize;
        while (offset < heapSize) {
            final Address ptr = start.add(offset);
            setAllocationBit(ptr, true);
            final int objSize = ptr.loadInt(Offset.fromIntSignExtend(sizeOffset));
            offset += objSize + headerSize;
        }
        //Unsafe.debug("end of bootheap.initialize");
    }

    /**
     * @see VmAbstractHeap#getFreeSize()
     * @return the free size
     */
    protected int getFreeSize() {
        return 0;
    }

    /**
     * Join all adjacent free spaces.
     * 
     * @throws UninterruptiblePragma
     */
    protected final void defragment() throws UninterruptiblePragma {
        // Do nothing, since the bootheap is never changed.
    }
    
    /**
     * Let all objects in this heap make a visit to the given visitor.
     * 
     * @param visitor
     * @param locking
     *            If true, use lock/unlock while proceeding to the next object.
     *            This parameter is irrelevant here, since the structure of
     *            this heap never changes.
     */
    protected void walk(ObjectVisitor visitor, boolean locking, int flagsMask, int flagsValue) {
        // Go through the heap and mark all objects in the allocation bitmap.
        final int headerSize = this.headerSize;
        final int sizeOffset = this.sizeOffset;
        final int size = getSize();
        int offset = headerSize;
        while (offset < size) {
            final Address ptr = start.add(offset);
            final Object object = ptr.toObjectReference().toObject();
            final int flags = VmMagic.getObjectFlags(object) & flagsMask;
            if ((flags != flagsValue) || visitor.visit(object)) {
                // Continue
                final int objSize = ptr.loadInt(Offset.fromIntSignExtend(sizeOffset));
                offset += objSize + headerSize;
            } else {
                // Stop
                offset = size;
            }
        }
    }
}
