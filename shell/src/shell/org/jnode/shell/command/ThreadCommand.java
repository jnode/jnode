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

import org.jnode.shell.AbstractCommand;
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

public class ThreadCommand extends AbstractCommand
{
	private static final ThreadNameArgument ARG_NAME = new ThreadNameArgument("threadName", "the name of the thread to view");
	private static final Parameter PAR_NAME = new Parameter(ARG_NAME, Parameter.OPTIONAL);

	public static Help.Info HELP_INFO = new Help.Info("thread", "View all or a specific threads", new Parameter[] { PAR_NAME });

    private final static String SEPARATOR = ", ", SLASH_T = "\t", GROUP = "Group ";


	public static void main(String[] args) throws Exception {
		new ThreadCommand().execute(args);
	}

	/**
	 * Execute this command
	 */
	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) 
	throws Exception {
		ParsedArguments parsedArguments = HELP_INFO.parse(commandLine);

		if (PAR_NAME.isSet(parsedArguments)) {
			execute(out, ARG_NAME.getValue(parsedArguments));
		} else {
			execute(out, null);
		}
	}

	public void execute(PrintStream out, String threadName) {
		ThreadGroup grp = Thread.currentThread().getThreadGroup();
	
		while (grp.getParent() != null) {
			grp = grp.getParent();
		}
		showGroup(grp, out, threadName);
	}

	private void showGroup(ThreadGroup grp, PrintStream out, String threadName) {
    	if (threadName == null
		// preserve compatible behavior when piped
				&& out == System.out) {
			grp.list();
			return;
		}

		if (threadName != null) {
			out.print(GROUP);
			out.println(grp.getName());
		}

		final int max = grp.activeCount() * 2;
		final Thread[] ts = new Thread[max];
		grp.enumerate(ts);


        for (int i = 0; i < max; i++) {
            final Thread t = ts[i];
            if (t != null) {
                if ((threadName == null) || threadName.equals(t.getName())) {
                    out.print(SLASH_T);
                    StringBuilder buffer = new StringBuilder();

                    buffer.append(t.getId());
                    buffer.append(SEPARATOR);
                    buffer.append(t.getName());
                    buffer.append(SEPARATOR);
                    buffer.append(t.getPriority());
                    buffer.append(SEPARATOR);
                    buffer.append(t.getVmThread().getThreadStateName());

                    out.println(buffer.toString());
                    if (threadName != null) {
                        final Object[] trace = VmThread.getStackTrace(t.getVmThread());
                        final int traceLen = trace.length;
                        for (int k = 0; k < traceLen; k++) {
                            buffer = new StringBuilder();
                            buffer.append(SLASH_T);
                            buffer.append(SLASH_T);
                            buffer.append(trace[k]);

                            out.println(buffer.toString());
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
