/**
 * $Id$
 */
package org.jnode.util;

import java.util.ArrayList;

/**
 * @author epr
 */
public class Queue {

	/** The actual queue */
	private final ArrayList queue = new ArrayList();
	private boolean closed = false;

	/**
	 * Gets the first element out of the queue. Blocks until an element is available
	 * and the returns element is remove from the queue.
	 * @param ignoreInteruptions If true, InterruptedException's are ignore, otherwise
	 *    and InterruptedException results in a return of <code>null</code>.
	 * @param timeout to wait for an object in the queue. On timeout, null is returned. A value of 0 means wait for ever.
	 * @return Object The first object in the queue, or null if the queue has
	 * 	been closed, a timeout occurs, or the current thread is interrupted (and ignoreInterruptions is false). 
	 */
	public synchronized Object get(boolean ignoreInteruptions, long timeout) {
		while (queue.isEmpty()) {
			if (closed) {
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
		Object result = queue.get(0);
		queue.remove(0);
		return result;
	}

	/**
	 * Gets the first element out of the queue. Blocks until an element is available
	 * and the returns element is remove from the queue.
	 * @param timeout to wait for an object in the queue. On timeout, null is returned. A value of 0 means wait for ever.
	 * @return Object The first object in the queue, or null if the queue has
	 * 	been closed, or a timeout occurs. 
	 */
	public Object get(long timeout) {
		return get(true, timeout);
	}

	/**
	 * Gets the first element out of the queue. Blocks until an element is available
	 * and the returns element is remove from the queue.
	 * @param ignoreInteruptions If true, InterruptedException's are ignore, otherwise
	 *    and InterruptedException results in a return of <code>null</code>.
	 * @return Object The first object in the queue, or null if the queue has
	 * 	been closed, or the current thread is interrupted (and ignoreInterruptions is false). 
	 */
	public Object get(boolean ignoreInteruptions) {
		return get(ignoreInteruptions, 0);
	}
	
	/**
	 * Gets the first element out of the queue. Blocks until an element is available
	 * and the returns element is remove from the queue.
	 * @return Object
	 */
	public Object get() {
		return get(true, 0);
	}

	/**
	 * Add an element to this queue.
	 * @param object
	 * @throws SecurityException If the queue has been closed.
	 */
	public synchronized void add(Object object) 
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
	 * @param object
	 * @throws SecurityException If the queue has been closed.
	 */
	public synchronized void remove(Object object) 
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
	 * @param object
	 * @return boolean
	 */
	public boolean contains(Object object) {
		return queue.contains(object);
	}

	/**
	 * Gets the number of elements in this queue.
	 * @return int
	 */
	public int size() {
		return queue.size();
	}
	
	/**
	 * Is this queue empty.
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
}

