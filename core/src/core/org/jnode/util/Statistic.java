/*
 * $Id$
 */
package org.jnode.util;

import org.jnode.vm.VmSystemObject;

/**
 * @author epr
 */
public abstract class Statistic extends VmSystemObject {
	
	private final String name;
	private final String description;
	
	public Statistic(String name) {
		this(name, null);
	}

	public Statistic(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public abstract Object getValue();
	
	/**
	 * Gets the name of this statistic
	 * @return The name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Convert to a String representation
	 * @see java.lang.Object#toString()
	 * @return String
	 */
	public String toString() {
		return name;
	}
	
	/**
	 * Gets the description of this statistic
	 * @return The description
	 */
	public String getDescription() {
		return description;
	}
}
