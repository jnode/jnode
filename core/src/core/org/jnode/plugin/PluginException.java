/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * Generic exception of the plugin framework.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginException extends Exception {

	/**
	 * 
	 */
	public PluginException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PluginException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public PluginException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public PluginException(String s) {
		super(s);
	}
}
