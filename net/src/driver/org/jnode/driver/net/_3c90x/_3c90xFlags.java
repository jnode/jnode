/*
 * $Id$
 */
package org.jnode.driver.net._3c90x;

import org.jnode.plugin.ConfigurationElement;

/**
 * @author epr
 */
public class _3c90xFlags {
	
	private final String name;
	
	/**
	 * Create a new instance
	 */
	public _3c90xFlags(ConfigurationElement config) {
		this(config.getAttribute("name")); 
	}

	/**
	 * Create a new instance
	 * @param name
	 */
	public _3c90xFlags(String name) {
		this.name = name; 
	}

	/**
	 * Gets the name of the device
	 */
	public String getName() {
		return name;
	}

}
