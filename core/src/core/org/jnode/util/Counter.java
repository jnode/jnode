/*
 * $Id$
 */
package org.jnode.util;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class Counter extends Statistic {

    private int counter;
	
	public Counter(String name) {
		super(name, null);
	}

	public Counter(String name, String description) {
	    super(name, description);
	}

	/**
	 * Gets the counter of this statistic
	 * @return the counter
	 */
	public int get() {
		return counter;
	}
	
	public Object getValue() {
	    return new Integer(counter);
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
		return getName() + "=" + counter;
	}
}
