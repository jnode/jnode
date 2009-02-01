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
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.memmgr.HeapHelper;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * @author epr
 */
@MagicPermission
final class VmBootHeap extends VmAbstractHeap {

    //public static final String START_FIELD_NAME = "start";
    //public static final String END_FIELD_NAME = "end";
    /**
     * Offset (in bytes) from the start of an object to the size of an object
     */
    private Offset sizeOffset;

    /**
     * Initialize this instance
     *
     * @param helper
     */
    public VmBootHeap(HeapHelper helper) {
        super(helper);
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
        this.sizeOffset = Offset.fromIntSignExtend(-((ObjectLayout.HEADER_SLOTS + 1) * slotSize));
        this.headerSize = ObjectLayout.objectAlign(this.headerSize + slotSize);

        // Create an allocation bitmap
        final int heapSize = getSize();
        final int bits = ObjectLayout.objectAlign(heapSize)
            / ObjectLayout.OBJECT_ALIGN;
        final int bitmapSize = ObjectLayout.objectAlign(bits / 8);
        allocationBitmapPtr = helper.allocateBlock(Extent.fromIntZeroExtend(bitmapSize));
        //allocationBitmapPtr = MemoryBlockManager.allocateBlock(bitmapSize);

        // Initialize the allocation bitmap
        helper.clear(allocationBitmapPtr, bitmapSize);
        // Go through the heap and mark all objects in the allocation bitmap.
        final Word heapSizeW = Word.fromIntZeroExtend(heapSize);
        final Word headerSize = Word.fromIntZeroExtend(this.headerSize);
        Word offset = headerSize;
        while (offset.LT(heapSizeW)) {
            final Address ptr = start.add(offset);
            setAllocationBit(ptr, true);
            final Word objSize = ptr.loadWord(sizeOffset);
            offset = offset.add(objSize).add(headerSize);
        }
        //Unsafe.debug("end of bootheap.initialize");
    }

    /**
     * Let all objects in this heap make a visit to the given visitor.
     *
     * @param visitor
     * @param locking If true, use lock/unlock while proceeding to the next object.
     *                This parameter is irrelevant here, since the structure of
     *                this heap never changes.
     */
    protected void walk(ObjectVisitor visitor, boolean locking, Word flagsMask, Word flagsValue) {
        // Go through the heap and mark all objects in the allocation bitmap.
        final Word headerSize = Word.fromIntZeroExtend(this.headerSize);
        final Offset sizeOffset = this.sizeOffset;
        final Word size = Word.fromIntZeroExtend(getSize());
        Word offset = headerSize;
        while (offset.LT(size)) {
            final Address ptr = start.add(offset);
            final Object object = ptr.toObjectReference().toObject();
            final Word flags = VmMagic.getObjectFlags(object).and(flagsMask);
            if (!flags.EQ(flagsValue) || visitor.visit(object)) {
                // Continue
                final Word objSize = ptr.loadWord(sizeOffset);
                offset = offset.add(objSize).add(headerSize);
            } else {
                // Stop
                offset = size;
            }
        }
    }
}
