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

/**
 * A simple fixed-length Queue for buffering bytes.  This queue is designed to
 * never block on the input side.  If the queue is full when 'push' is called, 
 * a byte will be discarded from the head of the queue to make space at the 
 * tail for the new byte.
 *
 * @author epr
 */
public class ByteQueue {
    // FIXME ... Looking at the way this class is used, I think it may needs an 
    // atomic drain operation and/or a close operation.
    // FIXME ... Make the ByteQueue API and behavior mirror the Queue API and behavior.
    
    /**
     * The default queue size.
     */
    static final int Q_SIZE = 10;

    private final byte[] data;
    private final int size;
    private int top = 0;
    private int bottom = 0;

    /**
     * Create a queue with the default size.
     */
    public ByteQueue() {
        this(Q_SIZE);
    }

    /**
     * Create a queue with the supplied size.
     * @param size the queue size in bytes; should be &gt;= 1.
     */
    public ByteQueue(int size) {
        this.data = new byte[size + 1];
        this.size = size;
    }

    /**
     * Add a byte at the tail of the queue.  This method does not block.
     * If the queue is full when 'push' is called, space for the byte is
     * made by removing (and discarding) the byte at the head of the queue.
     * @param o the byte to be added to the queue.
     */
    public synchronized void enQueue(byte o) {
        data[bottom] = o;
        bottom++;
        if (bottom >= size) {
            /* overflow */
            bottom = 0;
        }
        /* if wrapped around, advance top so it
             points to the old value */
        if (top == bottom) {
            top++;
        }
        notifyAll();
    }

    /**
     * Remove a byte from the head of the queue, blocking until one becomes
     * available.  If the thread calling this method is interrupted while waiting
     * for data, the method returns a zero byte.
     * 
     * @return the byte removed, or zero if the method call was interrupted.
     */
    public synchronized byte deQueue() {
        while (top == bottom) { /* Q is empty */
            try {
                wait();
            } catch (InterruptedException ie) {
                // FIXME ... this approach to handling interrupts is broken.  The
                // exception should be allowed to propagate 
                return 0;
            }
        } /* wait for push to fill Q */
        byte r = data[top];

        top++;
        if (top >= size) {
            top = 0;
        } /* end overflow */

        return r;
    }

    /**
     * Remove a byte from the head of the queue, blocking with a timeout if data is
     * not immediately available.  Unlike {@link #deQueue()}, this method does <b>not</b>
     * return zero when interrupted!
     * 
     * @param timeout the maximum time (in milliseconds) to wait for data to become 
     * available.  If zero, the method will wait as long as necessary.
     * @return the byte removed from the queue.
     * @throws InterruptedException if the method call is interrupted.
     * @throws TimeoutException if no data is available within the required time.
     */
    public synchronized byte deQueue(long timeout)
        throws TimeoutException, InterruptedException {
        while (top == bottom) { /* Q is empty */
            wait(timeout);
            if ((timeout > 0) && (top == bottom)) {
                throw new TimeoutException();
            }
        } /* wait for push to fill Q */
        byte r = data[top];

        top++;
        if (top >= size) {
            top = 0;
        } /* end overflow */

        return r;
    }

    /**
     * Return the byte at the head of the queue without removing it.   If data is
     * not immediately available, the method will block (with a timeout) until 
     * data is available.  Unlike {@link #deQueue()}, this method does <b>not</b>
     * return zero when interrupted!
     *
     * @param timeout the maximum time (in milliseconds) to wait for data to become 
     * available.  If zero, the method will wait as long as necessary.
     * @return the byte removed from the queue.
     * @throws InterruptedException if the method call is interrupted.
     * @throws TimeoutException if no data is available within the required time.
     */
    public synchronized byte peek(long timeout)
        throws TimeoutException, InterruptedException {
        while (top == bottom) { /* Q is empty */
            wait(timeout);
            if ((timeout > 0) && (top == bottom)) {
                throw new TimeoutException();
            }
        } /* wait for push to fill Q */
        return data[top];
    }

    /**
     * Test if there is no data in the queue.
     * @return {@code true} if the queue is empty, {@code false} otherwise.
     */
    public synchronized boolean isEmpty() {
        // FIXME ... this should be synchronized.
        return (top == bottom);
    }

    /**
     * Clears the queue.
     */
    public synchronized void clear() {
        top = bottom = 0;
    }
}
