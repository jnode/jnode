/*
 * $Id$
 */
package org.jnode.driver.net.lance;

/**
 * @author epr
 */
public class LanceFlags {
	
	/**
	 * Create a new instance
	 */
	public LanceFlags() {
	}
	
	public boolean mustUnreset() {
		return false;
	}
	
	public boolean isAutoSelectEnabled() {
		return true;
	}

}
