/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.Uninterruptible;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.memmgr.HeapHelper;

/**
 * @author epr
 */
final class GCSetWhiteVisitor extends ObjectVisitor implements ObjectFlags, Uninterruptible {

	private final HeapHelper helper;
	
	public GCSetWhiteVisitor(DefaultHeapManager heapMgr) {
		this.helper = heapMgr.getHelper();
	}
	
	/**
	 * Mark every visited object white.
	 * @param object
	 * @return boolean
	 */
	public boolean visit(Object object) {
		final int gcColor = helper.getObjectColor(object);
		helper.atomicChangeObjectColor(object, gcColor, GC_WHITE);
		return true;
	}

}
