/**
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.Address;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.memmgr.HeapHelper;

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
	 * @param start
	 * @param end
	 * @param slotSize
	 * @see VmAbstractHeap#initialize(Address, Address, int)
	 * For this class, the parameters are always null, so ignore them!
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
		final int bits = ObjectLayout.objectAlign(heapSize) / ObjectLayout.OBJECT_ALIGN;
		final int bitmapSize = ObjectLayout.objectAlign(bits / 8);
		allocationBitmapPtr = helper.allocateBlock(bitmapSize);
		//allocationBitmapPtr = MemoryBlockManager.allocateBlock(bitmapSize);
		
		// Initialize the allocation bitmap
		helper.clear(allocationBitmapPtr, bitmapSize);
		// Go through the heap and mark all objects in the allocation bitmap.
		int offset = headerSize;
		while (offset < heapSize) {
			Address ptr = Address.add(start, offset);
			setAllocationBit(ptr, true);
			int objSize = helper.getInt(ptr, sizeOffset);
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
	 * Free objects that have not been marked 
	 * @return The amount of freed memory in bytes 
	 */
	protected int collect() {
		// Do nothing, since the bootheap is never cleaned.
		return 0;
	}
	
	/**
	 * Let all objects in this heap make a visit to the given visitor.
	 * @param visitor
	 */
	protected void walk(ObjectVisitor visitor) {
		// Go through the heap and mark all objects in the allocation bitmap.
		final int headerSize = this.headerSize;
		final int sizeOffset = this.sizeOffset;
		final int size = getSize();
		int offset = headerSize;
		while (offset < size) {
			Address ptr = Address.add(start, offset);
			Object object = helper.objectAt(ptr);
			if (visitor.visit(object)) {
				// Continue
				int objSize = helper.getInt(ptr, sizeOffset);
				offset += objSize + headerSize;
			} else {
				// Stop
				offset = size;
			}
		}
	}
}
