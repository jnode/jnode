/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.shell.command;

import java.util.Date;

import javax.naming.NameNotFoundException;

import org.jnode.shell.help.Help;

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
