/*
 * $Id$
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

	private final BootableHashMap map = new BootableHashMap();
	private int lastSelector = 1;
	
	/**
	 * Gets the selector for a given name &amp; type
	 * @param name
	 * @param signature
	 * @return The global unique selector
	 */
	public int get(String name, String signature) {
		final String id = name + "#" + signature;
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
			selector = new Integer(++lastSelector);
			map.put(id, selector);
			return selector.intValue();
		}
	}
}
