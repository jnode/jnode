/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.SpinLock;
import org.jnode.vm.VmAddress;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.memmgr.HeapHelper;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;

/**
 * An abstract heap class.
 * 
 * All changes to the structure of the heap must be made exclusive by {@link #lock()} 
 * and {@link #unlock()}.
 * 
 * @author epr
 */
public abstract class VmAbstractHeap extends SpinLock implements Uninterruptible {

	/** Start address of this heap (inclusive) */
	protected VmAddress start;
	/** End address of this heap (exclusive) */
	protected VmAddress end;
	/** Start address of this heap (inclusive) */
	private long startL;
	/** End address of this heap (exclusive) */
	private long endL;
	/** Size of this heap in bytes */
	private int size;
	/** Size of an object header in bytes */
	protected int headerSize;
	/** Offset of the flags field in an object header */
	protected int flagsOffset;
	/** Offset of the type information block field in an object header */
	protected int tibOffset;
	/** Start address of allocation bitmap */
	protected VmAddress allocationBitmapPtr;
	/** The next heap (linked list) */
	private VmAbstractHeap next;
	
	/** Default marker for free object space (marker in VMT field) */
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
		this.flagsOffset = ObjectLayout.FLAGS_SLOT * slotSize;
		this.tibOffset = ObjectLayout.TIB_SLOT * slotSize;
		this.startL = helper.addressToLong(start);
		this.endL = helper.addressToLong(end);
		this.size = (int)(helper.addressToLong(end) - helper.addressToLong(start));
	}
	
	/**
	 * Gets the starting address of this heap.
	 * @return Address
	 */
	public final VmAddress getStart() {
		return start;
	}
	
	/**
	 * Gets the (exclusive) ending address of this heap.
	 * @return Address
	 */
	public final VmAddress getEnd() {
		return end;
	}
	
	/**
	 * Gets the size in bytes of this heap.
	 * @return int
	 */
	public final int getSize() {
		return size;
	}

	/**
	 * Is the given address the address of an allocated object on this heap?
	 * @param addr
	 * @return boolean
	 */
	protected boolean isObject(Address addr) {
		long addrL = addr.toLong();
		if ((addrL < startL) || (addrL >= endL)) {
			// The object if not within this heap
			return false;
		}
		int offset = (int)(addrL - startL);
		int bit = offset / ObjectLayout.OBJECT_ALIGN;
		int idx = bit / 8;
		int mask = 1 << (bit & 7);
		int value = helper.getByte(allocationBitmapPtr, idx);
		return ((value & mask) == mask);
	}

	/**
	 * Is the given address an address within this heap.
	 * If so, this does not always mean that the address is a valid object!
	 * @param addr
	 * @return boolean
	 */
	protected boolean inHeap(Address addr) {
		long addrL = addr.toLong();
		return ((addrL >= startL) && (addrL < endL));
	}

	/**
	 * Change a bit in the allocation bitmap.
	 * @param object
	 * @param on
	 */	
	protected final void setAllocationBit(Object object, boolean on) {
		VmAddress addr = helper.addressOf(object);
		long addrL = helper.addressToLong(addr);
		if ((addrL < startL) || (addrL >= endL)) {
			return;
		}
		int offset = (int)(addrL - startL);
		int bit = offset / ObjectLayout.OBJECT_ALIGN;
		int idx = bit / 8;
		int mask = 1 << (bit & 7);
		int value = helper.getByte(allocationBitmapPtr, idx);
		if (on) {
			value |= mask;
		} else {
			value &= ~mask;
		}
		helper.setByte(allocationBitmapPtr, idx, (byte)value);
	}
	
	/**
	 * Append a new heap to the end of the linked list of heaps.
	 * @param newHeap
	 */	
	protected void append(VmAbstractHeap newHeap) {
		VmAbstractHeap heap = this;
		while (heap.next != null) {
			heap = heap.next;
		}
		heap.next = newHeap;
	}
	
	/**
	 * Gets the next heap in the linked list of heaps.
	 * @return Next heap
	 */
	public final VmAbstractHeap getNext() {
		return next;
	}
	
	/**
	 * Initialize this heap
	 * @param start Start address of this heap
	 * @param end End address of this heap (first address after this heap)
	 * @param slotSize
	 */
	protected abstract void initialize(VmAddress start, VmAddress end, int slotSize);
	
	/**
	 * Allocate a new instance for the given class.
	 * Not that this method cannot be synchronized, since the synchronization
	 * is handled in VmHeap.
	 * @param vmClass
	 * @param alignedSize
	 * @return Object Null if no space is left.
	 */
	protected abstract Object alloc(VmClassType vmClass, int alignedSize);
	
	/**
	 * Gets the size of free memory in this heap in bytes
	 * @return size
	 */
	protected abstract int getFreeSize();
	
	/**
	 * Join all adjacent free spaces.
	 * @throws UninterruptiblePragma
	 */
	protected abstract void defragment()
	throws UninterruptiblePragma;
	
	/**
	 * Let a selected set of objects in this heap make a visit to the given visitor.
	 * The selection is made based on the objectflags. The objectflags are masked
	 * by flagsMask and the result is compared with flagsValue, if they are equal
	 * the object is visited.
	 * 
	 * @param visitor
	 * @param locking If true, use lock/unlock while proceeding to the next object.
	 * @param flagsMask 
	 * @param flagsValue 	 
	 * @throws UninterruptiblePragma
	 */
	protected abstract void walk(ObjectVisitor visitor, boolean locking, int flagsMask, int flagsValue)
	throws UninterruptiblePragma;
	
	/**
	 * Mark the given object as free space.
	 * @param object
	 */
	protected abstract void free(Object object);
}
