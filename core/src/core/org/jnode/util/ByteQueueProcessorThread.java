/*
 * $Id$
 */
package org.jnode.util;

import org.jnode.system.BootLog;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ByteQueueProcessorThread extends Thread {

	/** The queue i'm processing */
	private final ByteQueue queue;
	/** The actual processor */
	private final ByteQueueProcessor processor;
	private boolean stop;

	/**
	 * Create a new instance
	 * 
	 * @param name
	 * @param queue
	 * @param processor
	 */
	public ByteQueueProcessorThread(String name, ByteQueue queue, ByteQueueProcessor processor) {
		super(name);
		this.queue = queue;
		this.processor = processor;
		this.stop = false;
	}

	/**
	 * Create a new instance. A new queue is automatically created.
	 * 
	 * @param name
	 * @param processor
	 * @see #getQueue()
	 */
	public ByteQueueProcessorThread(String name, ByteQueueProcessor processor) {
		this(name, new ByteQueue(), processor);
	}

	/**
	 * Stop the processor
	 */
	public void stopProcessor() {
		this.stop = true;
		//this.interrupt();
	}

	/**
	 * Handle an exception thrown during the processing of the object.
	 * 
	 * @param ex
	 */
	protected void handleException(Exception ex) {
		BootLog.error("Exception in QueueProcessor", ex);
	}

	/**
	 * Handle an exception thrown during the processing of the object.
	 * 
	 * @param ex
	 */
	protected void handleError(Error ex) {
		BootLog.error("Error in QueueProcessor", ex);
	}

	/**
	 * Thread runner
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (!stop) {
			try {
				final byte value = queue.pop();
				processor.process(value);
			} catch (Exception ex) {
				handleException(ex);
			} catch (Error ex) {
				handleError(ex);
			}
		}
	}

	/**
	 * Gets this queue this thread works on.
	 * @return the queue
	 */
	public ByteQueue getQueue() {
		return queue;
	}

}
