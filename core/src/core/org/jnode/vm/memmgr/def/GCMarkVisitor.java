/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.Address;
import org.jnode.vm.Monitor;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.Uninterruptible;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmThread;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.HeapHelper;

/**
 * @author epr
 */
public class GCMarkVisitor extends ObjectVisitor implements ObjectFlags, Uninterruptible {

	/** The marking stack */
	private final GCStack stack;
	/** The number of marked objects. */
	private int markedObjects;
	/**
	 * If true, all white and grey objects will be marked, otherwise only the grey objects will be
	 * marked
	 */
	private boolean rootSet;
	private final VmArchitecture arch;
	private final int slotSize;
	private final DefaultHeapManager heapManager;
	private final HeapHelper helper;

	/**
	 * Create a new instance
	 * 
	 * @param stack
	 */
	public GCMarkVisitor(DefaultHeapManager heapManager, VmArchitecture arch, GCStack stack) {
		this.heapManager = heapManager;
		this.stack = stack;
		this.markedObjects = 0;
		this.rootSet = false;
		this.arch = arch;
		this.helper = heapManager.getHelper();
		this.slotSize = arch.getReferenceSize();
	}

	/**
	 * @param object
	 * @see org.jnode.vm.ObjectVisitor#visit(java.lang.Object)
	 * @return boolean
	 */
	public boolean visit(Object object) {

		// Be very paranoia for now
		if (!heapManager.isObject(helper.addressOf(object))) {
			Unsafe.debug("visit got non-object");
			Unsafe.debug(helper.addressToLong(helper.addressOf(object)));
			Unsafe.getCurrentProcessor().getArchitecture().getStackReader().debugStackTrace();
			helper.die("Internal error");
			return false;
		}

		//testObject(object, Unsafe.getVmClass(object));

		// Check the current color first, since a stackoverflow of
		// the mark stack results in another iteration of visits.
		final int flags = helper.getObjectFlags(object);
		final int gcColor = flags & GC_COLOUR_MASK;
		if (((gcColor == GC_WHITE) && rootSet) || (gcColor == GC_GREY)) {
			helper.setObjectFlags(object, (flags & ~GC_MASK) | GC_GREY);
			stack.push(object);
			mark();
		}

		final boolean rc = (!stack.isOverflow());
		return rc;
	}

	/**
	 * Reset this visitor to its original state.
	 */
	public void reset() {
		this.markedObjects = 0;
	}

	/**
	 * Process all objects on the markstack, until the markstack is empty.
	 */
	protected void mark() {
		while (!stack.isEmpty()) {
			final Object object = stack.pop();
			markedObjects++;
			final VmType vmClass = helper.getVmClass(object);
			if (vmClass == null) {
				Unsafe.debug("Oops vmClass == null in (");
				Unsafe.debug(markedObjects);
				Unsafe.debug(")");
			} else if (vmClass.isArray()) {
				if (!((VmArrayClass) vmClass).isPrimitiveArray()) {
					markArray(object, vmClass);
				}
			} else {
				markObject(object, (VmNormalClass) vmClass);
				if (object instanceof VmThread) {
					try {
						markThreadStack((VmThread) object);
					} catch (ClassCastException ex) {
						Unsafe.debug("VmThread");
						Unsafe.debug(object.getClass().getName());
						helper.die("GCMarkVisitor.mark");
					}
				}
			}
			final Monitor monitor = helper.getInflatedMonitor(object, arch);
			if (monitor != null) {
				processChild(monitor, "monitor", 0);
			}
			final int flags = helper.getObjectFlags(object);
			helper.setObjectFlags(object, (flags & ~GC_MASK) | GC_BLACK);
		}
	}

	/**
	 * Mark all elements in the given array. The array must contain references only.
	 * 
	 * @param object
	 * @param vmClass
	 */
	private void markArray(Object object, VmType vmClass) {
		try {
			final Object[] arr = (Object[]) object;
			final int length = arr.length;
			for (int i = 0; i < length; i++) {
				final Object child = arr[i];
				if (child != null) {
					processChild(child, vmClass.getName(), i);
				}
			}
		} catch (ClassCastException ex) {
			System.out.println("object.class=" + object.getClass().getName());
			throw ex;
		}
	}

	/**
	 * Mark all instance variables of the given object.
	 * 
	 * @param object
	 * @param vmClass
	 */
	private void markObject(Object object, VmNormalClass vmClass) {
		final int[] referenceOffsets = vmClass.getReferenceOffsets();
		final int cnt = referenceOffsets.length;
		final int size = vmClass.getObjectSize();
		for (int i = 0; i < cnt; i++) {
			int offset = referenceOffsets[i];
			if ((offset < 0) || (offset >= size)) {
				Unsafe.debug("reference offset out of range!");
				Unsafe.debug(vmClass.getName());
				helper.die("Class internal error");
			} else {
				final Object child = helper.getObject(object, offset);
				if (child != null) {
					processChild(child, vmClass.getName(), offset);
				}
			}
		}
	}

	/**
	 * Mark all objects on the stack of the given thread
	 * 
	 * @param thread
	 */
	private void markThreadStack(VmThread thread) {
		// For now do it stupid, but safe, just scan the whole stack.
		final int stackSize = thread.getStackSize();
		final Object stack = helper.getStack(thread);
		for (int i = 0; i < stackSize; i += slotSize) {
			Address child = Unsafe.getAddress(stack, i);
			if (child != null) {
				if (heapManager.isObject(child)) {
					processChild(child, "stack", i);
				}
			}
		}
	}

	/**
	 * Process a child of an object (this child is a reference).
	 * 
	 * @param child
	 * @param where
	 * @param index_
	 */
	private void processChild(Object child, String where, int index_) {
		final int flags = helper.getObjectFlags(child);
		int gcFlags = flags & GC_COLOUR_MASK;
		if (gcFlags == GC_WHITE) {
			helper.setObjectFlags(child, (flags & ~GC_MASK) | GC_GREY);
			stack.push(child);
		}
	}

	/**
	 * Gets the number of objects marked by this visitor.
	 * 
	 * @return int
	 */
	public int getMarkedObjects() {
		return markedObjects;
	}

	/**
	 * Gets the rootSet attribute.
	 * 
	 * @return boolean
	 */
	public boolean isRootSet() {
		return rootSet;
	}

	/**
	 * Sets the rootSet attribute.
	 * 
	 * @param b
	 *            If true, all white and grey objects will be marked, otherwise only the grey
	 *            objects will be marked.
	 */
	public void setRootSet(boolean b) {
		rootSet = b;
	}
}
