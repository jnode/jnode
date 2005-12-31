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
 
package org.jnode.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import org.jnode.vm.VmSystemObject;

/**
 * A BootableList is a List implementation that can be used in the 
 * build process of JNode.
 * Using this class, instead of e.g. ArrayList, will avoid class incompatibilities
 * between the JNode java.util implementation and Sun's implementation.
 * 
 * @author epr
 */
public class BootableArrayList<T> extends VmSystemObject implements List<T>, RandomAccess {

	private ArrayList<T> listCache;
	private T[] array;
	private int hashCode;
	private transient boolean locked;

	/**
	 * Constructs an empty list with an initial capacity of ten.
	 */
	public BootableArrayList() {
		hashCode = super.hashCode();
	}

	/**
	 * Constructs a list containing the elements of the specified collection, 
	 * in the order they are returned by the collection's iterator.
	 * @param c
	 */
	public BootableArrayList(Collection<? extends T> c) {
		addAll(c);
	}

	/**
	 * Constructs an empty list with an initial capacity of ten.
	 * @param initialCapacity
	 */
	public BootableArrayList(int initialCapacity) {
		listCache = new ArrayList<T>(initialCapacity);
		hashCode = listCache.hashCode();
	}

	/**
	 * Gets (an if needed reload) the arraylist.
	 * @return
	 */
	private final ArrayList<T> getListCache() {
		if (locked) {
			throw new RuntimeException("Cannot change a locked BootableArrayList");
		}
		if (listCache == null) {
			listCache = new ArrayList<T>();
			if (array != null) {
				listCache.addAll(Arrays.asList(array));
			}
			array = null;
		}
		return listCache;
	}
	
	/**
	 * @param index
	 * @param o
	 * @see java.util.AbstractList#add(int, java.lang.Object)
	 */
	public void add(int index, T o) {
		getListCache().add(index, o);
	}

	/**
	 * @param o
	 * @see java.util.AbstractList#add(java.lang.Object)
	 * @return boolean
	 */
	public boolean add(T o) {
		return getListCache().add(o);
	}

	/**
	 * @param c
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 * @return boolean
	 */
	public boolean addAll(Collection<? extends T> c) {
		return getListCache().addAll(c);
	}

	/**
	 * @param index
	 * @param c
	 * @see java.util.AbstractList#addAll(int, java.util.Collection)
	 * @return boolean
	 */
	public boolean addAll(int index, Collection<? extends T> c) {
		return getListCache().addAll(index, c);
	}

	/**
	 * @see java.util.AbstractList#clear()
	 */
	public void clear() {
		getListCache().clear();
	}

	/**
	 * @param o
	 * @see java.util.AbstractCollection#contains(java.lang.Object)
	 * @return boolean
	 */
	public boolean contains(Object o) {
		return getListCache().contains(o);
	}

	/**
	 * @param c
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 * @return boolean
	 */
	public boolean containsAll(Collection c) {
		return getListCache().containsAll(c);
	}

	/**
	 * @param minCapacity
	 */
	public void ensureCapacity(int minCapacity) {
		getListCache().ensureCapacity(minCapacity);
	}

	/**
	 * @param obj
	 * @see java.util.AbstractList#equals(java.lang.Object)
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		return getListCache().equals(obj);
	}

	/**
	 * @see java.util.AbstractList#hashCode()
	 * @return int
	 */
	public int hashCode() {
		if (listCache != null) {
			return getListCache().hashCode();
		} else {
			return hashCode;
		}
	}

	/**
	 * @param o
	 * @see java.util.AbstractList#indexOf(java.lang.Object)
	 * @return int
	 */
	public int indexOf(Object o) {
		return getListCache().indexOf(o);
	}

	/**
	 * @see java.util.AbstractCollection#isEmpty()
	 * @return boolean
	 */
	public boolean isEmpty() {
		return getListCache().isEmpty();
	}

	/**
	 * @see java.util.AbstractList#iterator()
	 * @return the iterator
	 */
	public Iterator<T> iterator() {
		return getListCache().iterator();
	}

	/**
	 * @param o
	 * @see java.util.AbstractList#lastIndexOf(java.lang.Object)
	 * @return int
	 */
	public int lastIndexOf(Object o) {
		return getListCache().lastIndexOf(o);
	}

	/**
	 * @see java.util.AbstractList#listIterator()
	 * @return the iterator
	 */
	public ListIterator<T> listIterator() {
		return getListCache().listIterator();
	}

	/**
	 * @param index
	 * @see java.util.AbstractList#listIterator(int)
	 * @return the iterator
	 */
	public ListIterator<T> listIterator(int index) {
		return getListCache().listIterator(index);
	}

	/**
	 * @param index
	 * @see java.util.AbstractList#remove(int)
	 * @return object
	 */
	public T remove(int index) {
		return getListCache().remove(index);
	}

	/**
	 * @param o
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 * @return boolean
	 */
	public boolean remove(Object o) {
		return getListCache().remove(o);
	}

	/**
	 * @param c
	 * @see java.util.AbstractCollection#removeAll(java.util.Collection)
	 * @return boolean
	 */
	public boolean removeAll(Collection<?> c) {
		return getListCache().removeAll(c);
	}

	/**
	 * @param c
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 * @return boolean
	 */
	public boolean retainAll(Collection c) {
		return getListCache().retainAll(c);
	}

	/**
	 * @param index
	 * @param o
	 * @see java.util.AbstractList#set(int, java.lang.Object)
	 * @return object
	 */
	public T set(int index, T o) {
		return getListCache().set(index, o);
	}

	/**
	 * @param fromIndex
	 * @param toIndex
	 * @see java.util.AbstractList#subList(int, int)
	 * @return the sub list
	 */
	public List<T> subList(int fromIndex, int toIndex) {
		return getListCache().subList(fromIndex, toIndex);
	}

	/**
	 * @see java.util.AbstractCollection#toArray()
	 * @return the array
	 */
	public Object[] toArray() {
		return getListCache().toArray();
	}

	/**
	 * @param a
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 * @return the array
	 */
	public <E> E[] toArray(E[] a) {
		return getListCache().toArray(a);
	}

	/**
	 * @see java.util.AbstractCollection#toString()
	 * @return String
	 */
	public String toString() {
		if (listCache != null) {
			return getListCache().toString();
		} else {
			return super.toString();
		}
	}

	/**
	 * 
	 */
	public void trimToSize() {
		getListCache().trimToSize();
	}

	/**
	 * @param index
	 * @return The element at the given index
	 */
	public T get(int index) {
		return getListCache().get(index);
	}

	/**
	 * @return The number of elements in this list
	 */
	public int size() {
		return getListCache().size();
	}
	
	/**
	 * @see org.jnode.vm.VmSystemObject#verifyBeforeEmit()
	 */
    @SuppressWarnings("unchecked")
	public void verifyBeforeEmit() {
		super.verifyBeforeEmit();
		if (listCache != null) {
			array = (T[])listCache.toArray();
			hashCode = listCache.hashCode();
		} else {
			array = null;
		}
		listCache = null;
		locked = true;
	}

}
