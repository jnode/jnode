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

/**
 * ByteQueue.java
 *
 * a simple fixed-length Queue.
 * @author epr
 */
public class ByteQueue {

	final static int Q_SIZE = 10;

	private final byte[] data;
	private final int size;
	private int top = 0;
	private int bottom = 0;

	public ByteQueue() {
		this(Q_SIZE);
	} 

	public ByteQueue(int size) {
		this.data = new byte[size + 1];
		this.size = size;
	} 

	public synchronized void push(byte o) {
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

	public synchronized byte pop() {
		while (top == bottom) { /* Q is empty */
			try {
				wait();
			} catch (InterruptedException ie) {
				// TODO: better throw a NoSuchElementException or alike!!!
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
	
	public synchronized byte pop(long timeout) 
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
	 * Wait until there is data in the queue and return the first
	 * element, without removing it.
	 * @param timeout
	 * @return
	 * @throws TimeoutException
	 * @throws InterruptedException
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
	
	public boolean isEmpty() {
		return (top == bottom);
	}
}
