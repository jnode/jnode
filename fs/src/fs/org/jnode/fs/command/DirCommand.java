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
 
package org.jnode.fs.command;

import java.io.File;

import org.jnode.shell.help.FileArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author epr
 */
public class DirCommand {

	static final FileArgument ARG_DIR = new FileArgument("directory", "the directory to list contents of");
	public static Help.Info HELP_INFO =
		new Help.Info(
			"dir",
			"List the entries of the given directory",
			new Parameter[] { new Parameter(ARG_DIR, Parameter.OPTIONAL)});

	public static void main(String[] args) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		File dir = ARG_DIR.getFile(cmdLine);
		if (dir == null) {
			dir = new File(System.getProperty("user.dir"));
		}

		if (dir.exists() && dir.isDirectory()) {
			final File[] list = dir.listFiles();
			printList(list);
		} else if("/".equals(dir.getCanonicalPath())) {
			File[] roots = File.listRoots();
			printList(roots);
		} else if (dir.exists() && dir.isFile()) {
		    printList(new File[] { dir });
		} else {
			System.err.println("No such directory " + dir);
		}
	}

	private static void printList(File[] list) {
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				File f = list[i];
				if (f.isDirectory()) {
					System.out.print("[" + f.getName() + "]");
				} else {
					System.out.print(f.getName() + " " + f.length());
				}
				System.out.println();
			}
			System.out.println();
		}
	}
}
