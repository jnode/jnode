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
