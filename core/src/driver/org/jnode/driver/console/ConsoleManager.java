/*
 * $Id$
 */
package org.jnode.driver.console;


/**
 * @author epr
 */
public interface ConsoleManager {
	
	public static final Class NAME = ConsoleManager.class;//"ConsoleManager";
	public static final String ShellConsoleName = "ShellConsole";
	public static final int MAX_SHELL_CONSOLE_NR = 6;

	/**
	 * Gets the console with the given index
	 * @param name
	 * @return The console
	 */
	public Console getConsole(String name);
	
	public Console getShellConsole(int index);
	
	public Console getConsoleWithAccelerator(int keyCode);

	public String[] listConsoleNames();
	
	public void registerConsole(Console console);
	public void unregisterConsole(Console console);
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