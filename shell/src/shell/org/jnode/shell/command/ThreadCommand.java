/*
 * $Id$
 */
package org.jnode.shell.command;

import java.io.InputStream;
import java.io.PrintStream;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.ThreadNameArgument;
import org.jnode.vm.VmSystem;

/**
 * Shell command to view threads or a specific thread.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ThreadCommand {

	private static final ThreadNameArgument ARG_NAME = new ThreadNameArgument("threadName", "the name of the thread to view");
	private static final Parameter PAR_NAME = new Parameter(ARG_NAME, Parameter.OPTIONAL);
	public static Help.Info HELP_INFO = new Help.Info("thread", "View all or a specific threads", new Parameter[] { PAR_NAME });

	public static void main(String[] args) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);
		new ThreadCommand().execute(cmdLine, System.in, System.out, System.err);
	}

	/**
	 * Execute this command
	 */
	public void execute(ParsedArguments cmdLine, InputStream in, PrintStream out, PrintStream err) throws Exception {

		ThreadGroup grp = Thread.currentThread().getThreadGroup();
		while (grp.getParent() != null) {
			grp = grp.getParent();
		}
		if (PAR_NAME.isSet(cmdLine)) {
			showGroup(grp, out, ARG_NAME.getValue(cmdLine));
		} else {
			showGroup(grp, out, null);
		}
	}

	private void showGroup(ThreadGroup grp, PrintStream out, String threadName) {
		if (threadName != null) {
			out.println("Group " + grp.getName());
		}
		final int max = grp.activeCount() * 2;
		final Thread[] ts = new Thread[max];
		grp.enumerate(ts);
		for (int i = 0; i < max; i++) {
			final Thread t = ts[i];
			if (t != null) {
				if ((threadName == null) || threadName.equals(t.getName())) {
					out.print("\t");
					out.println(t.getName() + ", " + t.getPriority() + ", " + t.getVmThread().getThreadStateName());
					if (threadName != null) {
						final Object[] trace = VmSystem.getStackTrace(t.getVmThread());
						final int traceLen = trace.length;
						for (int k = 0; k < traceLen; k++) {
							out.println("\t\t" + trace[k]);
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
