/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.vm.classmgr;

import org.jnode.util.BootableHashMap;
import org.jnode.vm.VmSystemObject;

/**
 * This class is used to maintain a mapping between a method signature (name+type)
 * and a unique selector.
 * 
 * @author epr
 */
public class SelectorMap extends VmSystemObject {

	private final BootableHashMap map = new BootableHashMap(8192);
	private int lastSelector = 1;
	
	/**
	 * Gets the selector for a given name &amp; type
	 * @param name
	 * @param signature
	 * @return The global unique selector
	 */
	public int get(String name, String signature) {
		final String id = name + '#' + signature;
		final Integer selector = (Integer)map.get(id);
		if (selector != null) {
			return selector.intValue();
		} else {
			return getNew(id);
		}
	}

	/**
	 * Get was not able to get a selector, do a synchronized test
	 * and create a new selector if needed.
	 * 
	 * @param id
	 * @return The selector
	 */
	private synchronized int getNew(String id) {
		Integer selector = (Integer)map.get(id);
		if (selector != null) {
			return selector.intValue();
		} else {
		    final int sel = ++lastSelector;
			map.put(id, new Integer(sel));
			return sel;
		}
	}
}
