/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.Address;
import org.jnode.vm.PragmaUninterruptible;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.VmWriteBarrier;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DefaultWriteBarrier extends VmWriteBarrier {
	
	/** Offset in an object of the GC flags */
	private final int gcFlagsOffset;
	/** The heap helper */
	private final HeapHelper helper;
	
	/**
	 * Initialize this instance.
	 * @param arch
	 */
	public DefaultWriteBarrier(VmArchitecture arch, HeapHelper helper) {
		this.gcFlagsOffset = ObjectLayout.FLAGS_SLOT * arch.getReferenceSize();
		this.helper = helper;
	}
	
	/**
	 * @see org.jnode.vm.memmgr.VmWriteBarrier#arrayCopyWriteBarrier(java.lang.Object[], int, int)
	 */
	public final void arrayCopyWriteBarrier(Object[] array, int start, int end) throws PragmaUninterruptible {
		for (int i = start; i < end; i++) {
			writeBarrier(array[i]);
		}
	}
	
	/**
	 * @see org.jnode.vm.memmgr.VmWriteBarrier#arrayStoreWriteBarrier(java.lang.Object, int, java.lang.Object)
	 */
	public final void arrayStoreWriteBarrier(Object ref, int index, Object value) throws PragmaUninterruptible {
		writeBarrier(value);
	}
	
	/**
	 * @see org.jnode.vm.memmgr.VmWriteBarrier#putfieldWriteBarrier(java.lang.Object, int, java.lang.Object)
	 */
	public final void putfieldWriteBarrier(Object ref, int offset, Object value) throws PragmaUninterruptible {
		writeBarrier(value);
	}
	
	/**
	 * @see org.jnode.vm.memmgr.VmWriteBarrier#putstaticWriteBarrier(int, java.lang.Object)
	 */
	public final void putstaticWriteBarrier(int staticsIndex, Object value) throws PragmaUninterruptible {
		writeBarrier(value);
	}
	
	/**
	 * Set the GC color of the given value to gray if it is white.
	 * @param value
	 */
	private final void writeBarrier(Object value) {
		if (value != null) {
			final Address flagsAddr = helper.add(helper.addressOf(value), gcFlagsOffset);
			while (true) {
				final int oldFlags = helper.getInt(value, gcFlagsOffset);
				if ((oldFlags & ObjectFlags.GC_COLOUR_MASK) != ObjectFlags.GC_WHITE) {
					// Not white, we're done
					return;
				}
				final int newFlags = (oldFlags & ~ObjectFlags.GC_COLOUR_MASK) | ObjectFlags.GC_GREY;
				if (helper.atomicCompareAndSwap(flagsAddr, oldFlags, newFlags)) {
					// Change to grey, we're done
					return;
				}
			} 
		}
	}
}
