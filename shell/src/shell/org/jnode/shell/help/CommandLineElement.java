/*
 * $Id$
 */

package org.jnode.shell.help;

/**
 * @author qades
 */
abstract class CommandLineElement {
	private final String name;
	private final String description;

	public CommandLineElement(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public abstract String format();
	public abstract void describe(Help help);
	public abstract String complete(String partial);

	/** 
	 * Indicates if the element is satisfied.
	 * I.e. not taking any more values.
	 * @return <code>false</code> if the element takes more argument values;
	 * <code>false</code> otherwise
	 */
	public abstract boolean isSatisfied();

}
