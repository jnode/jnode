/*
 * $Id$
 */
package org.jnode.net.ipv4.tcp;

import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPTimer extends Thread {

	private final Logger log = Logger.getLogger(getClass());
	private final TCPControlBlockList cbList;
	private boolean stop = false;
	private long counter;
	private static int autoNr = 0;
	
	/** 
	 * Create a new instance
	 * @param cbList
	 */
	public TCPTimer(TCPControlBlockList cbList) {
		super(autoName());
		this.cbList = cbList;
	}
	
	/**
	 * Keep calling timeout forever.
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (!stop) {
			try {
				cbList.timeout();
				counter += TCPConstants.TCP_TIMER_PERIOD;
			} catch (Throwable ex) {
				log.error("Error in TCP timer", ex);
			}
			try {
			Thread.sleep(TCPConstants.TCP_TIMER_PERIOD);
			} catch (InterruptedException ex) {
				// Ignore
			}
		}
	}
	
	/**
	 * @return Returns the counter.
	 */
	public final long getCounter() {
		return this.counter;
	}
	
	private static synchronized String autoName() {
		return "tcp-timer-" + (autoNr++);
	}
}
