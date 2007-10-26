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
 
package org.jnode.shell;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;

/**
 * @author epr
 */
public class ShellUtils {

	/**
	 * Get the current shell manager.
	 * @return The current shell manager
	 * @throws NameNotFoundException
	 */
	public static ShellManager getShellManager()
	throws NameNotFoundException {
		return InitialNaming.lookup(ShellManager.NAME);
	}

	public static void registerCommandInvoker(CommandInvoker.Factory factory) 
	throws NameNotFoundException {
		getShellManager().registerInvokerFactory(factory);
	}
	
	public static void registerCommandInterpreter(CommandInterpreter.Factory factory) 
	throws NameNotFoundException {
		getShellManager().registerInterpreterFactory(factory);
	}
	
	public static CommandInvoker createInvoker(String name, CommandShell shell) 
	throws IllegalArgumentException {
		try {
			return getShellManager().createInvoker(name, shell);
		}	
		catch (NameNotFoundException ex) {
			throw new ShellFailureException("no shell manager", ex);
		}
	}

	public static CommandInterpreter createInterpreter(String name) 
	throws IllegalArgumentException, ShellFailureException {
		try {
			return getShellManager().createInterpreter(name);
		}
		catch (NameNotFoundException ex) {
			throw new ShellFailureException("no shell manager", ex);
		}
	}
}
