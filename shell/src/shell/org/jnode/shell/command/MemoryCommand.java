/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.CommandLine;
import org.jnode.shell.Command;
import org.jnode.shell.help.Help;
import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MemoryCommand implements Command 
{

	public static Help.Info HELP_INFO = new Help.Info("memory", "View the current memory status");

	public static void main(String[] args) throws Exception {
		new MemoryCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}

	/**
	 * Execute this command
	 */
	public void execute(CommandLine cmdLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		final Runtime rt = Runtime.getRuntime();
		out.println("Total memory " + NumberUtils.size(rt.totalMemory()));
		out.println("Used memory  " + NumberUtils.size(rt.totalMemory() - rt.freeMemory()));
		out.println("Free memory  " + NumberUtils.size(rt.freeMemory()));
	}
}
