/*
 * $Id$
 */
package org.jnode.driver.input;


/**
 * API implemented by devices that can trigger a system inspection
 * function such as a debugger.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface SystemTriggerAPI {

	/**
	 * Add a listener
	 * @param l
	 */
	public void addSystemTriggerListener(SystemTriggerListener l);
	
	/**
	 * Remove a listener
	 * @param l
	 */
	public void removeSystemTriggerListener(SystemTriggerListener l);
	
}
