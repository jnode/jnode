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

import java.util.ArrayList;

/**
 * @author epr
 */
public class Queue<T> {
    
    public static final int NO_WAIT = -1;

    /**
     * The actual queue
     */
    private final ArrayList<T> queue = new ArrayList<T>();
    private boolean closed = false;

    /**
     * Gets the first element out of the queue. Blocks until an element is available
     * and the returns element is remove from the queue.
     *
     * @param ignoreInteruptions If true, InterruptedException's are ignore, otherwise
     *                           and InterruptedException results in a return of <code>null</code>.
     * @param timeout            to wait for an object in the queue. On timeout, null is returned.
     * A value of 0 means wait for ever.
     * @return Object The first object in the queue, or null if the queue has
     *         been closed, a timeout occurs, or the current thread is interrupted (and ignoreInterruptions is false).
     */
    public synchronized T get(boolean ignoreInteruptions, long timeout) {
        while (queue.isEmpty()) {
            if (closed || timeout == NO_WAIT) {
                return null;
            }
            try {
                wait(timeout);
            } catch (InterruptedException ex) {
                if (!ignoreInteruptions) {
                    return null;
                }
                /* ignore */
            }
            if ((timeout != 0) && (queue.isEmpty())) {
                return null;
            }
        }
        return queue.remove(0);
    }

    /**
     * Gets the first element out of the queue. Blocks until an element is available
     * and the returns element is remove from the queue.
     *
     * @param timeout to wait for an object in the queue. On timeout, null is returned.
     * A value of 0 means wait for ever.
     * @return Object The first object in the queue, or null if the queue has
     *         been closed, or a timeout occurs.
     */
    public T get(long timeout) {
        return get(true, timeout);
    }

    /**
     * Gets the first element out of the queue. Blocks until an element is available
     * and the returns element is remove from the queue.
     *
     * @param ignoreInteruptions If true, InterruptedException's are ignore, otherwise
     *                           and InterruptedException results in a return of <code>null</code>.
     * @return Object The first object in the queue, or null if the queue has
     *         been closed, or the current thread is interrupted (and ignoreInterruptions is false).
     */
    public T get(boolean ignoreInteruptions) {
        return get(ignoreInteruptions, 0);
    }

    /**
     * Gets the first element out of the queue. Blocks until an element is available
     * and the returns element is remove from the queue.
     *
     * @return Object
     */
    public T get() {
        return get(true, 0);
    }

    /**
     * Add an element to this queue.
     *
     * @param object
     * @throws SecurityException If the queue has been closed.
     */
    public synchronized void add(T object)
        throws SecurityException {
        if (closed) {
            throw new SecurityException("Cannot add to a closed queue.");
        } else {
            queue.add(object);
            notifyAll();
        }
    }

    /**
     * Remove an element from this queue.
     *
     * @param object
     * @throws SecurityException If the queue has been closed.
     */
    public synchronized void remove(T object)
        throws SecurityException {
        if (closed) {
            throw new SecurityException("Cannot remove from a closed queue.");
        } else {
            queue.remove(object);
            notifyAll();
        }
    }

    /**
     * Does this queue contain a given object?
     *
     * @param object
     * @return boolean
     */
    public boolean contains(T object) {
        return queue.contains(object);
    }

    /**
     * Gets the number of elements in this queue.
     *
     * @return int
     */
    public int size() {
        return queue.size();
    }

    /**
     * Is this queue empty.
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Close this queue. All thread blocks in the <code>get</code> method
     * will return with a null value.
     */
    public synchronized void close() {
        this.closed = true;
        notifyAll();
    }
    
    /**
     * Is this queue closed.
     * @return {@code true} if the queue is closed, {@code false} otherwise.
     */
    public boolean isClosed() {
        return closed;
    }
}

