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

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.scheduler.SpinLock;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * An abstract heap class.
 * <p/>
 * All changes to the structure of the heap must be made exclusive by {@link #lock()}
 * and {@link #unlock()}.
 *
 * @author epr
 */
@MagicPermission
abstract class VmAbstractHeap extends SpinLock implements Uninterruptible {

    /**
     * Start address of this heap (inclusive)
     */
    protected Address start;
    /**
     * End address of this heap (exclusive)
     */
    protected Address end;
    /**
     * Size of this heap in bytes
     */
    private int size;
    /**
     * Size of an object header in bytes
     */
    protected int headerSize;
    /**
     * Offset of the flags field in an object header
     */
    protected Offset flagsOffset;
    /**
     * Offset of the type information block field in an object header
     */
    protected Offset tibOffset;
    /**
     * Start address of allocation bitmap
     */
    protected Address allocationBitmapPtr;

    /**
     * Default marker for free object space (marker in VMT field)
     */
    protected Object FREE = null;
    protected HeapHelper helper;

    public VmAbstractHeap(HeapHelper helper) {
        this.helper = helper;
    }

    /**
     * Initialization method for the private fields in this class.
     * This method must be called, after start and end have been set.
     *
     * @param slotSize
     */
    protected final void initializeAbstract(int slotSize) {
        this.headerSize = ObjectLayout.HEADER_SLOTS * slotSize;
        this.flagsOffset = Offset.fromIntSignExtend(ObjectLayout.FLAGS_SLOT * slotSize);
        this.tibOffset = Offset.fromIntSignExtend(ObjectLayout.TIB_SLOT * slotSize);
        this.size = end.toWord().sub(start.toWord()).toInt();
    }

    /**
     * Gets the size in bytes of this heap.
     *
     * @return int
     */
    @Inline
    public final int getSize() {
        return size;
    }

    /**
     * Is the given address the address of an allocated object on this heap?
     *
     * @param addr
     * @return boolean
     */
    @Inline
    protected final boolean isObject(Address addr) {
        if (addr.LT(start) || addr.GE(end)) {
            // The object if not within this heap
            return false;
        }

        final int offset = addr.toWord().sub(start.toWord()).toInt();
        int bit = offset / ObjectLayout.OBJECT_ALIGN;
        final Offset idx = Offset.fromIntZeroExtend(bit / 8);
        final int mask = 1 << (bit & 7);
        final Address bitmapPtr = this.allocationBitmapPtr;
        final int value = bitmapPtr.loadByte(idx);
        return ((value & mask) == mask);
    }

    /**
     * Is the given address an address within this heap.
     * If so, this does not always mean that the address is a valid object!
     *
     * @param addr
     * @return boolean
     */
    @Inline
    protected final boolean inHeap(Address addr) {
        return (addr.GE(start) && addr.LT(end));
    }

    /**
     * Change a bit in the allocation bitmap.
     *
     * @param object
     * @param on
     */
    @Inline
    protected final void setAllocationBit(Object object, boolean on) {
        final Address addr = ObjectReference.fromObject(object).toAddress();
        if (addr.LT(start) || addr.GE(end)) {
            return;
        }

        final int offset = addr.toWord().sub(start.toWord()).toInt();
        final int bit = offset / ObjectLayout.OBJECT_ALIGN;
        final Offset idx = Offset.fromIntZeroExtend(bit / 8);
        final int mask = 1 << (bit & 7);
        final Address bitmapPtr = this.allocationBitmapPtr;
        int value = bitmapPtr.loadByte(idx);
        if (on) {
            value |= mask;
        } else {
            value &= ~mask;
        }
        bitmapPtr.store((byte) value, idx);
    }

    /**
     * Initialize this heap
     *
     * @param start    Start address of this heap
     * @param end      End address of this heap (first address after this heap)
     * @param slotSize
     */
    protected abstract void initialize(Address start, Address end, int slotSize);

    /**
     * Let a selected set of objects in this heap make a visit to the given visitor.
     * The selection is made based on the objectflags. The objectflags are masked
     * by flagsMask and the result is compared with flagsValue, if they are equal
     * the object is visited.
     *
     * @param visitor
     * @param locking    If true, use lock/unlock while proceeding to the next object.
     * @param flagsMask
     * @param flagsValue
     * @throws UninterruptiblePragma
     */
    protected abstract void walk(ObjectVisitor visitor, boolean locking, Word flagsMask, Word flagsValue)
        throws UninterruptiblePragma;
}
