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

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.Command;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.argument.IntegerArgument;

/**
 * @author Andreas H\u00e4nel
 */
public class KillCommand implements Command{
	
	 static IntegerArgument ARG_THREADID = new IntegerArgument("id", "the id of the thread to kill");
	
	 public static Help.Info HELP_INFO = new Help.Info(
			"kill", new Syntax[] {
				new Syntax("kill the Thread with given id",
					new Parameter[] {
						new Parameter(ARG_THREADID, Parameter.MANDATORY)
					}
				)
			});
	 
	public static void main(String[] args) throws Exception {
	    new KillCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}
	
	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		ParsedArguments parsedArguments = HELP_INFO.parse(commandLine.toStringArray());
		out.print("going to kill Thread with id : ");
		out.println(ARG_THREADID.getInteger(parsedArguments));
	      // kill Thread
			ThreadGroup grp = Thread.currentThread().getThreadGroup();
			while (grp.getParent() != null) {
				grp = grp.getParent();
			}
			kill(grp,ARG_THREADID.getInteger(parsedArguments),out);
	    
	}
	
    @SuppressWarnings("deprecation")
	private void kill(ThreadGroup grp,int id,PrintStream out){
		final int max = grp.activeCount() * 2;
		final Thread[] ts = new Thread[max];
		grp.enumerate(ts);
		
		for (int i = 0; i < max; i++) {
			final Thread t = ts[i];
			if (t != null) {
				if (t.getId()==id){
					out.print("found the thread : ");
					out.println(id);
					t.stop(new ThreadDeath());
					out.println("killed it...");
				}
			}
		}

		final int gmax = grp.activeGroupCount() * 2;
		final ThreadGroup[] tgs = new ThreadGroup[gmax];
		grp.enumerate(tgs);
		for (int i = 0; i < gmax; i++) {
			final ThreadGroup tg = tgs[i];
			if (tg != null) {
				kill(tg, id, out);
			}
		}
	
	}
	
}
