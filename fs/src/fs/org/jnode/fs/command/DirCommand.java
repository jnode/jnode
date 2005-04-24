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
import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.FileArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author epr
 * @author Andreas H\u00e4nel
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class DirCommand implements Command{

	static final FileArgument ARG_DIR = new FileArgument("directory", "the directory to list contents of");
	public static Help.Info HELP_INFO =
		new Help.Info(
			"dir",
			"List the entries of the given directory",
			new Parameter[] { new Parameter(ARG_DIR, Parameter.OPTIONAL)});

	public static void main(String[] args) throws Exception {
		new DirCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}
	
	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(commandLine.toStringArray());

        String dir_str = ARG_DIR.getValue(cmdLine);
        
		String userDir = System.getProperty("user.dir");
        
		if ( ((dir_str == null) && isRoot(userDir)) ||
             ((dir_str != null) && isRoot(dir_str)) ){
            File[] roots = File.listRoots();
			this.printList(roots,out);
		} else {
			File dir = ARG_DIR.getFile(cmdLine);
			if (dir==null) dir = new File(userDir);
			if (dir.exists() && dir.isDirectory()) {
				final File[] list = dir.listFiles();
				this.printList(list, out);
			} else if (dir.exists() && dir.isFile()) {
				this.printList(new File[] { dir },out);
			} else {
				err.println("No such directory " + dir);
			}
		}
			
	}

    private boolean isRoot(String dir)
    {
        return (dir != null) && dir.equals(File.separator);
    }
    
	private void printList(File[] list, PrintStream out) {
		if (list != null) {
			for (int i = 0; i < list.length; i++) {
				File f = list[i];
				if (f.isDirectory()) {
					out.print("[" + f.getName() + "]");
				} else {
					out.print(f.getName() + " " + f.length());
				}
				out.println();
			}
			out.println();
		}
	}
}
