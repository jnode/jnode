/*
 * $Id$
 */
package org.jnode.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author epr
 */
public class ObjectArrayIterator implements Iterator {
	
	private final Object[] array;
	private final int max;
	private int index;
	
	/**
	 * Initialize a new instance
	 * @param array
	 */
	public ObjectArrayIterator(Object[] array) {
		this.array = array;
		if (array == null) {
			max = 0;
		} else {
			max = array.length;
		}
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 * @return boolean
	 */
	public boolean hasNext() {
		return (index < max);
	}

	/**
	 * @see java.util.Iterator#next()
	 * @return Object
	 */
	public Object next() {
		if (index < max) {
			final Object result = array[index];
			index++;
			return result;
		} else {
			throw new NoSuchElementException();
		}
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
