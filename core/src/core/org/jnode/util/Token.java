/*
 * $Id$
 */
package org.jnode.util;

/**
 * @author epr
 */
public class Token {

	private boolean locked;
	
	/**
	 * Create a new instance
	 */
	public Token() {
		this.locked = false;
	}
	
	/**
	 * Claim ownership of this token
	 * @param owner
	 * @param timeout
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public synchronized void claim(Object owner, long timeout) 
	throws InterruptedException, TimeoutException {
		final long start = System.currentTimeMillis();
		while (locked) {
			wait(timeout);
			if (locked) {
				if (System.currentTimeMillis() > start + timeout) {
					throw new TimeoutException();
				}
			}
		}
		this.locked = true;
	}

	/**
	 * Release ownership of this token
	 */
	public synchronized void release() {
		this.locked = false;
		notifyAll();
	}
}
