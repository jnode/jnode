/*
 * $Id$
 */

package org.jnode.shell.command;

import java.util.Date;

import javax.naming.NameNotFoundException;

import org.jnode.shell.help.*;

/** 
 * A shell command to access the display the system date.
 * @author Matt Paine
 */
public class DateCommand {

        public static Help.Info HELP_INFO = new Help.Info(
		"date",
		"prints the current date"
	);

	/**
	 * Sets up any instance variables and processes the command arguments. 
	 * @param args The arguments to work with.
	 **/
	public DateCommand(String[] args) throws NameNotFoundException {
		System.out.println(new Date());
	}

	/** 
	 * Displays the system date
	 * @param args No arguments.
	 **/
	public static void main(String[] args) throws NameNotFoundException {
		new DateCommand(args);
	}

}
