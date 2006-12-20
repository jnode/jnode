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
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.argument.ThreadNameArgument;
import org.jnode.vm.scheduler.VmThread;

/**
 * Shell command to view threads or a specific thread.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */

public class ThreadCommand  implements Command
{
	private static final ThreadNameArgument ARG_NAME = new ThreadNameArgument("threadName", "the name of the thread to view");
	private static final Parameter PAR_NAME = new Parameter(ARG_NAME, Parameter.OPTIONAL);

	public static Help.Info HELP_INFO = new Help.Info("thread", "View all or a specific threads", new Parameter[] { PAR_NAME });

  private final static String seperator = ", ", slash_t = "\t", group = "Group ";

	public static void main(String[] args) throws Exception {

		new ThreadCommand().execute(new CommandLine(args), System.in, System.out, System.err);
	}

	/**
	 * Execute this command
	 */
	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
    ParsedArguments parsedArguments = HELP_INFO.parse(commandLine.toStringArray());

		ThreadGroup grp = Thread.currentThread().getThreadGroup();

		while (grp.getParent() != null) {
			grp = grp.getParent();
		}

		if (PAR_NAME.isSet(parsedArguments)) {
			showGroup(grp, out, ARG_NAME.getValue(parsedArguments));
		} else {
			showGroup(grp, out, null);
		}
	}

	private void showGroup(ThreadGroup grp, PrintStream out, String threadName) {
    StringBuffer stringBuffer;

    	if (threadName == null
		// preserve compatible behavior when piped
				&& out == System.out) {
			grp.list();
			return;
		}
    	
		if (threadName != null) {
			out.print(group);
			out.println(grp.getName());
		}

		final int max = grp.activeCount() * 2;
		final Thread[] ts = new Thread[max];
		grp.enumerate(ts);



		for (int i = 0; i < max; i++) {
			final Thread t = ts[i];
			if (t != null) {
				if ((threadName == null) || threadName.equals(t.getName())) {
					out.print(slash_t);
          stringBuffer = new StringBuffer();

          stringBuffer.append(t.getId());
					stringBuffer.append(seperator);
					stringBuffer.append(t.getName());
					stringBuffer.append(seperator);
					stringBuffer.append(t.getPriority());
					stringBuffer.append(seperator);
					stringBuffer.append(t.getVmThread().getThreadStateName());

          out.println(stringBuffer.toString());
          stringBuffer = null;

					if (threadName != null) {
						final Object[] trace = VmThread.getStackTrace(t.getVmThread());
						final int traceLen = trace.length;
						for (int k = 0; k < traceLen; k++) {
              stringBuffer = new StringBuffer();
              stringBuffer.append(slash_t);
              stringBuffer.append(slash_t);
              stringBuffer.append(trace[k]);

              out.println(stringBuffer.toString());

              stringBuffer = null;
						}

						return;
					}
				}
			}
		}

		final int gmax = grp.activeGroupCount() * 2;
		final ThreadGroup[] tgs = new ThreadGroup[gmax];
		grp.enumerate(tgs);
		for (int i = 0; i < gmax; i++) {
			final ThreadGroup tg = tgs[i];
			if (tg != null) {
				showGroup(tg, out, threadName);
			}
		}
	}

}
