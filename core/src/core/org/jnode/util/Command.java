/*
 * $Id$
 */
package org.jnode.util;


/**
 * @author epr
 */
public abstract class Command {

	/** Is this command finished yet? */
	private boolean finished = false;

	/**
	 * Has this command finished?
	 * @return boolean
	 */
	public final boolean isFinished() {
		return finished;
	}
	
	/**
	 * Mark this command as finished.
	 * Notify all waiting threads.
	 */
	protected final synchronized void notifyFinished() {
		finished = true;
		notifyAll();
	}
	
	/**
	 * Block the current thread, until this command has finished.
	 * @param timeout
	 * @throws InterruptedException This thread was interrupted
	 * @throws TimeoutException A timeout occurred.
	 */
	public synchronized void waitUntilFinished(long timeout) 
	throws InterruptedException, TimeoutException {
		final long start = System.currentTimeMillis();
		while (!finished) {
			wait(timeout);
			if ((timeout > 0) && (!finished)) {
				if (System.currentTimeMillis() >= start+timeout) {
					throw new TimeoutException("timeout");
				}
			}
		}
	}
}
