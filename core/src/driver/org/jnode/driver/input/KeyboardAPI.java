/*
 * $Id$
 */
package org.jnode.driver.input;

import org.jnode.driver.DeviceAPI;
import org.jnode.driver.DriverPermission;

/**
 * @author epr
 */
public interface KeyboardAPI extends DeviceAPI {

	/** Permission */
	public static final DriverPermission SET_PREFERRED_LISTENER_PERMISSION = new DriverPermission("setPreferredListener");

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
	 * Claim to be the preferred listener.
	 * The given listener must have been added by addKeyboardListener.
	 * If there is a security manager, this method will call
	 * <code>checkPermission(new DriverPermission("setPreferredListener"))</code>.
	 * @param l
	 */
	public abstract void setPreferredListener(KeyboardListener l);
	
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
