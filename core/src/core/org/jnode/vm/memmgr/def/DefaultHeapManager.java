/**
 * $Id$
 */

package org.jnode.vm.memmgr.def;

import org.jnode.vm.Address;
import org.jnode.vm.MemoryBlockManager;
import org.jnode.vm.Monitor;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.VmHeapManager;
import org.jnode.vm.memmgr.VmWriteBarrier;

public final class DefaultHeapManager extends VmHeapManager {

	/** Default size in bytes of a new heap */
	public static final int DEFAULT_HEAP_SIZE = 2 * 1024 * 1024;
	/** A GC manager alllocated early, so we have it when we're out of memory */
	private GCManager gcManager;
	/** Monitor to synchronize heap access */
	private Monitor heapMonitor;
	/** Are we low on memory */
	private boolean lowOnMemory;
	/** The used write barrier */
	private final VmWriteBarrier writeBarrier;

	/** The first heap. */
	private final VmAbstractHeap firstHeap;
	/** The heap currently used for allocation */
	private VmAbstractHeap currentHeap;
	/** The class of the default heap type. Set by initialize */
	private final VmNormalClass defaultHeapClass;
	private final VmStatics statics;

	/**
	 * Make this private, so we cannot be instantiated
	 */
	public DefaultHeapManager(VmClassLoader loader, HeapHelper helper, VmStatics statics) 
	throws ClassNotFoundException {
		super(helper);
		this.writeBarrier = new DefaultWriteBarrier(loader.getArchitecture(), helper);
		this.firstHeap = new VmDefaultHeap(this);
		this.currentHeap = firstHeap;
		this.defaultHeapClass = (VmNormalClass)loader.loadClass(VmDefaultHeap.class.getName(), true);
		this.statics = statics;
	}

	/**
	 * Is the given address the address of an allocated object on this heap?
	 * 
	 * @param ptr
	 *            The address to examine.
	 * @return True if the given address if a valid starting address of an object, false otherwise.
	 */
	public final boolean isObject(Address ptr) {
		long addrL = helper.addressToLong(ptr);
		if ((addrL & (ObjectLayout.OBJECT_ALIGN - 1)) != 0) {
			// The object is not at an object aligned boundary
			return false;
		}
		if (bootHeap.isObject(ptr)) {
			return true;
		}
		VmAbstractHeap heap = firstHeap;
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
	 * @return boolean
	 */
	public boolean isLowOnMemory() {
		return lowOnMemory;
	}

	/**
	 * Start a garbage collection process
	 */
	public void gc() {
		final Monitor m = heapMonitor;
		if (m != null) {
			m.enter();
		}
		try {
			gcManager.gc(bootHeap, firstHeap);
		} finally {
			if (m != null) {
				m.exit();
			}
		}
	}

	/**
	 * Gets the size of free memory in bytes.
	 * @return long
	 */
	public long getFreeMemory() {
		long size = bootHeap.getFreeSize();
		VmAbstractHeap h = firstHeap;
		while (h != null) {
			size += h.getFreeSize();
			h = h.getNext();
		}
		//size += (Unsafe.addressToLong(heapEnd) - Unsafe.addressToLong(nextHeapPtr));
		size += MemoryBlockManager.getFreeMemory();
		return size;
	}

	/**
	 * Gets the size of all memory in bytes.
	 * 
	 * @return the size of all memory in bytes
	 */
	public long getTotalMemory() {
		long size = bootHeap.getSize();
		VmAbstractHeap h = firstHeap;
		while (h != null) {
			size += h.getSize();
			h = h.getNext();
		}
		//size += (Unsafe.addressToLong(heapEnd) - Unsafe.addressToLong(nextHeapPtr));
		size += MemoryBlockManager.getFreeMemory();
		return size;
	}

	/**
	 * Gets the first heap. All other heaps can be iterated through the <code>getNext()</code>
	 * method.
	 * 
	 * @return the first heap
	 */
	public final VmAbstractHeap getFirstHeap() {
		return firstHeap;
	}

	// ------------------------------------------
	// Private natives
	// ------------------------------------------

	protected void initialize() {
		// Set the basic fields
		final VmArchitecture arch = Unsafe.getCurrentProcessor().getArchitecture();
		final int slotSize = arch.getReferenceSize();

		// Initialize the boot heap.
		bootHeap.initialize(helper.getBootHeapStart(), helper.getBootHeapEnd(), slotSize);

		// Initialize the first normal heap
		Address ptr = helper.allocateBlock(DEFAULT_HEAP_SIZE);
		firstHeap.initialize(ptr, Address.add(ptr, DEFAULT_HEAP_SIZE), slotSize);	
	}
	
	public void start() {
		// Create a Heap monitor
		heapMonitor = new Monitor();
		final VmArchitecture arch = Unsafe.getCurrentProcessor().getArchitecture();
		this.gcManager = new GCManager(this, arch, statics);
		// Do nothing for now
		// We will start the GC thread here
	}
	
	
	/**
	 * Gets the write barrier used by this heap manager (if any).
	 * @return The write barrier, or null if no write barrier is used.
	 */
	public final VmWriteBarrier getWriteBarrier() {
		return writeBarrier;
	}

	/**
	 * Allocate a new instance for the given class. Not that this method cannot be synchronized,
	 * since obtaining a monitor might require creating one, which in turn needs this method.
	 * 
	 * @param vmClass
	 * @param size
	 * @return Object
	 */
	protected Object allocObject(VmClassType vmClass, int size) {
		if (size > DEFAULT_HEAP_SIZE) {
			throw new OutOfMemoryError("Object too large (" + size + ")");
		}
	
		// Make sure the class is initialized
		vmClass.initialize();
		
		final int alignedSize = ObjectLayout.objectAlign(size);
		//final Monitor mon = heapMonitor;

		VmAbstractHeap heap = currentHeap;
		Object result = null;
		int oomCount = 0;

		final Monitor m = heapMonitor;
		//final Monitor m = null;
		if (m != null) {
			m.enter();
		}
		try {

			while (result == null) {
				// The current heap is full
				if (heap == null) {
					//Unsafe.debug("allocHeap in allocObject("); Unsafe.debug(alignedSize);
					//Unsafe.debug(") ");
					if ((heap = allocHeap(DEFAULT_HEAP_SIZE)) == null) {
						lowOnMemory = true;
						// It was not possible to allocate another heap.
						// First try to GC, if we've done that before
						// in this allocation, then we're in real panic.
						if (oomCount == 0) {
							oomCount++;
							gc();
							heap = firstHeap;
							currentHeap = firstHeap;
						} else {
							Unsafe.debug("Out of memory in allocObject(");
							Unsafe.debug(size);
							Unsafe.debug(")");
							throw OOME;
							//Unsafe.die();
						}
					} else {
						//Unsafe.debug("AO.G");
						// We successfully allocated a new heap, set it
						// to current, so we'll use it for the following
						// allocations.
						currentHeap = heap;
					}
				}

				result = heap.alloc(vmClass, alignedSize);

				if (result == null) {
					heap = heap.getNext();
				}
			}
			vmClass.incInstanceCount();
			lowOnMemory = false;
		} finally {
			if (m != null) {
				m.exit();
			}
		}

		//Unsafe.debug("new="); Unsafe.debug(Unsafe.addressToInt(Unsafe.addressOf(result)));
		
		return result;
	}

	/**
	 * Allocate a new heap with a given size. The heap object itself is allocated on the new heap,
	 * so this method can be called even if all other heaps are full.
	 * 
	 * @param size
	 * @return The heap
	 */
	private VmAbstractHeap allocHeap(int size) {
		//Unsafe.debug("allocHeap");
		final Address start = helper.allocateBlock(size);
		//final Address start = MemoryBlockManager.allocateBlock(size);
		if (start == null) {
			return null;
		}
		final Address end = Address.add(start, size);
		final int slotSize = Unsafe.getCurrentProcessor().getArchitecture().getReferenceSize();
		final VmAbstractHeap heap = VmDefaultHeap.setupHeap(helper, start, defaultHeapClass, slotSize);
		heap.initialize(start, end, slotSize);

		firstHeap.append(heap);
		return heap;
	}
}
