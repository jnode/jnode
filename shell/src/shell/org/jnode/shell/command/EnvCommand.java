/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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

import gnu.java.security.action.GetPropertiesAction;

import java.io.InputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;

/**
 * @author epr
 */
public class EnvCommand {

        public static Help.Info HELP_INFO = new Help.Info(
		"env",
		"Print the system properties"
	);

	public static void main(String[] args)
	throws Exception {
		new EnvCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}

	/**
	 * Execute this command
	 */
	public void execute(
		CommandLine cmdLine,
		InputStream in,
		PrintStream out,
		PrintStream err)
		throws Exception {

	    final Properties ps = (Properties)AccessController.doPrivileged(new GetPropertiesAction());
	    final TreeMap sortedPs = new TreeMap(ps);
		for (Iterator i = sortedPs.entrySet().iterator(); i.hasNext(); ) {
			final Map.Entry entry = (Map.Entry)i.next();
			final String key = entry.getKey().toString();
			final String value = entry.getValue().toString();

			out.print(key);
			out.print('=');
			out.println(value);
		}
	}

}
