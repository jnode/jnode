/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.Address;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.PragmaUninterruptible;
import org.jnode.vm.Uninterruptible;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.memmgr.HeapHelper;

/**
 * @author epr
 */
public abstract class VmAbstractHeap extends VmSystemObject implements Uninterruptible {

	/** Start address of this heap (inclusive) */
	protected Address start;
	/** End address of this heap (exclusive) */
	protected Address end;
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
	/** Offset of the vmt field in an object header */
	protected int vmtOffset;
	/** Start address of allocation bitmap */
	protected Address allocationBitmapPtr;
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
		this.vmtOffset = ObjectLayout.TIB_SLOT * slotSize;
		this.startL = helper.addressToLong(start);
		this.endL = helper.addressToLong(end);
		this.size = (int)(helper.addressToLong(end) - helper.addressToLong(start));
	}
	
	/**
	 * Gets the starting address of this heap.
	 * @return Address
	 */
	public final Address getStart() {
		return start;
	}
	
	/**
	 * Gets the (exclusive) ending address of this heap.
	 * @return Address
	 */
	public final Address getEnd() {
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
		long addrL = helper.addressToLong(addr);
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
		long addrL = helper.addressToLong(addr);
		return ((addrL >= startL) && (addrL < endL));
	}

	/**
	 * Change a bit in the allocation bitmap.
	 * @param object
	 * @param on
	 */	
	protected void setAllocationBit(Object object, boolean on) {
		Address addr = helper.addressOf(object);
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
	protected abstract void initialize(Address start, Address end, int slotSize);
	
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
	 * Free objects that have not been marked
	 * @return The amount of freed memory in bytes 
	 * @throws PragmaUninterruptible
	 */
	protected abstract int collect()
	throws PragmaUninterruptible;
	
	/**
	 * Let all objects in this heap make a visit to the given visitor.
	 * @param visitor
	 * @throws PragmaUninterruptible
	 */
	protected abstract void walk(ObjectVisitor visitor)
	throws PragmaUninterruptible;
}
