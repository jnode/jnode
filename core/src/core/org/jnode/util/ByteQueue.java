/*
 * $Id$
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
	
	public boolean isEmpty() {
		return (top == bottom);
	}
}
