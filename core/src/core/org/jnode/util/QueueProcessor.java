/*
 * $Id$
 */
package org.jnode.util;

/**
 * @author epr
 */
public interface QueueProcessor {

	/**
	 * Process the given object from the queue.
	 * @param object
	 * @throws Exception
	 */
	public abstract void process(Object object)
	throws Exception;
	
}
