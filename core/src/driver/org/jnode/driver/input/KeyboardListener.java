/*
 * $Id$
 */
package org.jnode.driver.input;

/**
 * @author epr
 */
public interface KeyboardListener {
	
	/**
	 * A key has been pressed.
	 * @param event
	 */
	public void keyPressed(KeyboardEvent event);
	
	/**
	 * A key has been released.
	 * @param event
	 */
	public void keyReleased(KeyboardEvent event);
}
