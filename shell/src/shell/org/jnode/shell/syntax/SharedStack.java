/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

package org.jnode.shell.syntax;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * This a custom stack (FIFO) class for MuParser.  Each time MuParser creates a "choice point",
 * it needs to record the current state of its syntax stack.  This is gets expensive in time
 * and space; e.g. if the syntax and/or choice stack is deep.  This stack implementation 
 * takes advantage of the fact that the saved stack will not normally be referred to.  It
 * defers the actual copying until the active stack's size drops below the saved stack's 
 * size.
 * <p>
 * This class is not a complete Deque implementation.  It only implements the small subset of 
 * the interface that is required by MuParser.  
 * 
 * @author crawley@jnode.org
 *
 * @param <E>
 */
class SharedStack<E> implements Deque<E> {
    private int baseStackSize;
    private Deque<E> baseStack;
    private Deque<E> myStack;
    
    private static final boolean DEBUG = true;
    
    /**
     * Create a new working stack with an initial state taken from the supplied Deque.
     * It is assumed that the supplied Deque is not be changed for the lifetime of
     * the SharedStack.
     *  
     * @param stack
     */
    public SharedStack(Deque<E> stack) {
        this.baseStack = stack;
        this.baseStackSize = stack.size();
        this.myStack = new LinkedList<E>();
    }

    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    public void addFirst(E e) {
        if (DEBUG) check();
        myStack.addFirst(e);
    }

    public void addLast(E e) {
        throw new UnsupportedOperationException();
    }

    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    public Iterator<E> descendingIterator() {
        throw new UnsupportedOperationException();
    }

    public E element() {
        throw new UnsupportedOperationException();
    }

    public E getFirst() {
        if (DEBUG) check();
        return myStack.getFirst();
    }

    public E getLast() {
        throw new UnsupportedOperationException();
    }

    public Iterator<E> iterator() {
        if (DEBUG) check();
        final Iterator<E> it1 = myStack.iterator();
        final Iterator<E> it2 = baseStack == null ? null : baseStack.iterator();
        
        return new Iterator<E>() {

            public boolean hasNext() {
                return it1.hasNext() || (it2 != null && it2.hasNext());
            }

            public E next() {
                if (it1.hasNext()) {
                    return it1.next();
                }
                else if (it2 != null) {
                    return it2.next();
                }
                else {
                    throw new NoSuchElementException("iterator is tired and emotional");
                }
            }

            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
            
        };
    }

    public boolean offer(E e) {
        throw new UnsupportedOperationException();
    }

    public boolean offerFirst(E e) {
        throw new UnsupportedOperationException();
    }

    public boolean offerLast(E e) {
        throw new UnsupportedOperationException();
    }

    public E peek() {
        if (DEBUG) check();
        return myStack.peek();
    }

    public E peekFirst() {
        if (DEBUG) check();
        return myStack.peekFirst();
    }

    public E peekLast() {
        throw new UnsupportedOperationException();
    }

    public E poll() {
        throw new UnsupportedOperationException();
    }

    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    public E pop() {
        return removeFirst();
    }

    public void push(E e) {
        addFirst(e);
    }

    public E remove() {
        return removeFirst();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public E removeFirst() {
        if (DEBUG) check();
        if (myStack.isEmpty() && baseStack != null) {
            myStack = new LinkedList<E>(baseStack);
            baseStack = null;
            baseStackSize = 0;
        }
        return myStack.removeFirst();
    }

    public boolean removeFirstOccurrence(Object o) {
        throw new UnsupportedOperationException();
    }

    public E removeLast() {
        throw new UnsupportedOperationException();
    }

    public boolean removeLastOccurrence(Object o) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        if (DEBUG) check();
        return myStack.size() + baseStackSize;
    }

    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean isEmpty() {
        if (DEBUG) check();
        return size() == 0;
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public Object[] toArray() {
        // This method is used by copy constructors.
        if (DEBUG) check();
        if (baseStack == null) {
            return myStack.toArray();
        }
        else {
            int myStackSize = myStack.size();
            Object[] res = new Object[baseStackSize + myStackSize];
            myStack.toArray(res);
            Object[] tmp = baseStack.toArray();
            System.arraycopy(tmp, 0, res, myStackSize, baseStackSize);
            return res;
        }
    }

    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }
    
    private void check() {
        if (baseStack != null && baseStack.size() != baseStackSize) {
            throw new AssertionError("base stack has been updated!");
        }
    }
}
