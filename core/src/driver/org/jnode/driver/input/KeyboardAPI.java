/*
 * $Id$
 */
package org.jnode.driver.input;

import org.jnode.driver.DeviceAPI;

/**
 * @author epr
 */
public interface KeyboardAPI extends DeviceAPI {

	/**
	 * Add a keyboard listener
	 * @param l
	 */
	public abstract void addKeyboardListener(KeyboardListener l);
	
	/**
	 * Remove a keyboard listener
	 * @param l
	 */
	public abstract void removeKeyboardListener(KeyboardListener l);
	
	/**
	 * @return KeyboardInterpreter
	 */
	public abstract KeyboardInterpreter getKbInterpreter();
	
	/**
	 * Sets the kbInterpreter.
	 * @param kbInterpreter The kbInterpreter to set
	 */
	public abstract void setKbInterpreter(KeyboardInterpreter kbInterpreter);
}
