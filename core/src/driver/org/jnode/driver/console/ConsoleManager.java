/*
 * $Id$
 */
package org.jnode.driver.console;


/**
 * @author epr
 */
public interface ConsoleManager {
	
	public static final Class NAME = ConsoleManager.class;//"ConsoleManager";
	public static final String UserConsoleName = "UserConsole";
	public static final int MAX_USER_CONSOLE_NR = 6;
	
	/**
	 * Create and return a new console.
	 * @return Console
	 * @throws ConsoleException
	 */
	public Console createConsole(String name) throws ConsoleException;

	/**
	 * Gets the console with the given index
	 * @param index
	 * @return The console
	 */
	public Console getConsole(String name);
	
	public Console getUserConsole(int index);
	
	public Console createUserConsole() throws ConsoleException;

	public Console getConsoleWithAccelerator(int keyCode);
	
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