/*
 * $Id$
 */
package org.jnode.driver.console;


/**
 * @author epr
 */
public interface ConsoleManager {
	
	public static final String NAME = "ConsoleManager";

	/**
	 * Create and return a new console.
	 * @return Console
	 * @throws ConsoleException
	 */
	public Console createConsole() throws ConsoleException;

	/**
	 * Gets the console with the given index
	 * @param index
	 * @return The console
	 */
	public Console getConsole(int index);
	
	/**
	 * Gets the currently focused console.
	 * @return Console
	 */
	public Console getFocus();
	
	/**
	 * Focus the given console
	 * @param console
	 */
	public void focus(Console console);
}