/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
