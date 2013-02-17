/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author epr
 */
public class ObjectArrayIterator<T> implements Iterator<T> {

    private final T[] array;
    private final int max;
    private int index;

    /**
     * Initialize a new instance
     *
     * @param array
     */
    public ObjectArrayIterator(T[] array) {
        this.array = array;
        if (array == null) {
            max = 0;
        } else {
            max = array.length;
        }
    }

    /**
     * @return boolean
     * @see java.util.Iterator#hasNext()
     */
    public boolean hasNext() {
        return (index < max);
    }

    /**
     * @return Object
     * @see java.util.Iterator#next()
     */
    public T next() {
        if (index < max) {
            final T result = array[index];
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
