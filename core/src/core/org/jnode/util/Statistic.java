/*
 * $Id$
 */
package org.jnode.util;

/**
 * @author epr
 */
public class Statistic {
	
	private final String name;
	private final String description;
	private int counter;
	
	public Statistic(String name) {
		this(name, null);
	}

	public Statistic(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * Gets the counter of this statistic
	 * @return the counter
	 */
	public int get() {
		return counter;
	}

	/**
	 * Gets the name of this statistic
	 * @return The name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Increment the counter of this statistic by 1.
	 */
	public void inc() {
		counter++;
	}
	
	/**
	 * Convert to a String representation
	 * @see java.lang.Object#toString()
	 * @return String
	 */
	public String toString() {
		return name + "=" + counter;
	}
	
	/**
	 * Gets the description of this statistic
	 * @return The description
	 */
	public String getDescription() {
		return description;
	}
}
