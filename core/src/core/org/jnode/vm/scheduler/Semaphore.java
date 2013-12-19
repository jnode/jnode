package org.jnode.vm.scheduler;

/**
 * Simple counting semaphore.
 * @author ewout
 * @see http://en.wikipedia.org/wiki/Semaphore_(programming)
 */
public final class Semaphore {

	private int count;

	/**
	 * create and initialise semaphore to n
	 */
	public Semaphore(int n) {
		this.count = n;
	}

	/**
	 * Decrements the value of semaphore by 1. If the value becomes negative,
	 * the process executing wait() is blocked. This is also called "wait".
	 */
	public synchronized void down() {
		while (count == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		count--;
	}

	/**
	 * Increment the value of the semaphore by 1.
	 * Unlock waiting threads. 
	 * This operation is also called
	 * "signal"
	 */
	public synchronized void up() {
		count++;
		notify(); // notify blocked processes that we're done
	}
}
