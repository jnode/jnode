/*
 * $Id$
 */
package org.jnode.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jnode.vm.VmSystemObject;

/**
 * @author epr
 */
public class BootableHashMap extends VmSystemObject implements Map {

	private HashMap mapCache;
	private Entry[] entryArray;
	private int hashCode;
	private transient boolean locked;
	
	/**
	 * Constructs an empty HashMap with the default initial capacity (16) 
	 * and the default load factor (0.75).
	 */
	public BootableHashMap() {
		this.hashCode = super.hashCode();
	}
	
	/**
	 * Constructs an empty HashMap with the default initial capacity (16) 
	 * and the default load factor (0.75).
	 * @param initialCapacity
	 */
	public BootableHashMap(int initialCapacity) {
		mapCache = new HashMap(initialCapacity);
		this.hashCode = mapCache.hashCode();
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 * @return int
	 */
	public int hashCode() {
		if (mapCache != null) {
			return getMapCache().hashCode();			
		} else {
			return hashCode;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return String
	 */
	public String toString() {
		if (mapCache != null) {
			return getMapCache().toString();
		} else {
			return super.toString();
		}
	}

	/**
	 * @return The collection of values
	 */
	public Collection values() {
		return getMapCache().values();
	}

	/**
	 * @return The set of keys
	 */
	public Set keySet() {
		return getMapCache().keySet();
	}

	/**
	 * @param key
	 * @return The object for the given key, or null if the given key is not found.
	 */
	public Object get(Object key) {
		return getMapCache().get(key);
	}

	/**
	 * 
	 */
	public void clear() {
		getMapCache().clear();
	}

	/**
	 * @return The number of elements
	 */
	public int size() {
		return getMapCache().size();
	}

	/**
	 * @param key
	 * @param value
	 * @return Object
	 */
	public Object put(Object key, Object value) {
		return getMapCache().put(key, value);
	}

	/**
	 * @param m
	 */
	public void putAll(Map m) {
		getMapCache().putAll(m);
	}

	/**
	 * @return The set of entries
	 */
	public Set entrySet() {
		return getMapCache().entrySet();
	}

	/**
	 * @param key
	 * @return True if the key is contained, false otherwiser
	 */
	public boolean containsKey(Object key) {
		return getMapCache().containsKey(key);
	}

	/**
	 * @return True if this map is empty, false otherwise
	 */
	public boolean isEmpty() {
		return getMapCache().isEmpty();
	}

	/**
	 * @param obj
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		return getMapCache().equals(obj);
	}

	/**
	 * @param o
	 * @return Object
	 */
	public Object remove(Object o) {
		return getMapCache().remove(o);
	}

	/**
	 * @param value
	 * @return True if the given value is contained, false otherwise
	 */
	public boolean containsValue(Object value) {
		return getMapCache().containsValue(value);
	}



	static final class Entry extends VmSystemObject {
		private final Object key;
		private final Object value;
		
		public Entry(Map.Entry entry) {
			this.key = entry.getKey();
			this.value = entry.getValue();
		}
		
		/**
		 * Gets the key
		 * @return Object
		 */
		public Object getKey() {
			return key;
		}

		/**
		 * Gets the value
		 * @return Object
		 */
		public Object getValue() {
			return value;
		}
	}
	
	/**
	 * Gets the hashmap
	 * @return
	 */
	private final HashMap getMapCache() {
		if (locked) {
			throw new RuntimeException("Cannot change a locked BootableHashMap");
		}
		if (mapCache == null) {
			mapCache = new HashMap();
			if (entryArray != null) {
				final int max = entryArray.length;
				for (int i = 0; i < max; i++) {
					final Entry e = entryArray[i];
					mapCache.put(e.getKey(), e.getValue());
				}
				entryArray = null;
			}
		}
		return mapCache;
	}

	/**
	 * @see org.jnode.vm.VmSystemObject#verifyBeforeEmit()
	 */
	public void verifyBeforeEmit() {
		super.verifyBeforeEmit();
		
		if (mapCache != null) {
			entryArray = new Entry[mapCache.size()];
			int index = 0;
			for (Iterator i = mapCache.entrySet().iterator(); i.hasNext(); ) {
				entryArray[index++] = new Entry((Map.Entry)i.next());
			}
			hashCode = mapCache.hashCode();
			mapCache = null;
		}
		locked = true;
	}

}
