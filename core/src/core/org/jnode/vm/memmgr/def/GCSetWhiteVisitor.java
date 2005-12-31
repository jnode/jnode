/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.vm.memmgr.def;

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmMagic;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.memmgr.HeapHelper;
import org.vmmagic.pragma.Uninterruptible;

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
		final int gcColor = VmMagic.getObjectColor(object);
        if (gcColor != GC_YELLOW) {
            helper.atomicChangeObjectColor(object, gcColor, GC_WHITE);
        }
		return true;
	}

}
