/*
 * $Id$
 */
package org.jnode.util;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ByteQueueProcessor {

	/**
	 * Process the given byte from the queue.
	 * @param value
	 * @throws Exception
	 */
	public abstract void process(byte value)
	throws Exception;
	
}
