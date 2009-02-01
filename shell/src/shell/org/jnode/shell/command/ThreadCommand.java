/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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

import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.ThreadNameArgument;
import org.jnode.vm.scheduler.VmThread;

/**
 * Shell command to print information about all threads or a specific thread.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author crawley@jnode.org
 */
public class ThreadCommand extends AbstractCommand {
    private static final String SEPARATOR = ", ";
    private static final String SLASH_T = "\t";
    private static final String GROUP = "Group ";
    private static final String TRACE = "Stack trace";

    private final ThreadNameArgument ARG_NAME = 
        new ThreadNameArgument("threadName", Argument.OPTIONAL, "the name of a specific thread to be printed");
    private final FlagArgument FLAG_GROUP_DUMP =
        new FlagArgument("groupDump", Argument.OPTIONAL, "if set, output a ThreadGroup dump");

    public ThreadCommand() {
        super("View all or a specific threads");
        registerArguments(ARG_NAME, FLAG_GROUP_DUMP);
    }

    public static void main(String[] args) throws Exception {
        new ThreadCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute() throws Exception {
        // If threadName is null, we'll print all threads
        String threadName = (ARG_NAME.isSet()) ? ARG_NAME.getValue() : null;
        boolean dump = FLAG_GROUP_DUMP.isSet();

        // Find the root of the ThreadGroup tree
        ThreadGroup grp = Thread.currentThread().getThreadGroup();
        while (grp.getParent() != null) {
            grp = grp.getParent();
        }

        if (dump) {
            // Produce an ugly (but useful) ThreadGroup dump.  Unfortunately,
            // it goes to System.out, and we cannot fix it w/o changing a Java
            // standard API.
            grp.list();
        } else {
            // Show the threads in the ThreadGroup tree.
            showThreads(grp, getOutput().getPrintWriter(), threadName);
        }
    }

    /**
     * Traverse the ThreadGroups threads and its child ThreadGroups printing
     * information for each thread found.  If 'threadName' is non-null, only
     * print information for the thread that matches the name.
     * 
     * @param grp the ThreadGroup to traverse
     * @param out the destination for output
     * @param threadName if non-null, only display this thread.
     */
    private void showThreads(ThreadGroup grp, PrintWriter out, String threadName) {
        if (threadName == null) {
            out.println(GROUP + grp.getName());
        }

        final int max = grp.activeCount() * 2;
        final Thread[] ts = new Thread[max];
        grp.enumerate(ts);

        for (int i = 0; i < max; i++) {
            final Thread t = ts[i];
            if (t != null) {
                if ((threadName == null) || threadName.equals(t.getName())) {
                    VmThread vmThread = AccessController
                    .doPrivileged(new PrivilegedAction<VmThread>() {
                        public VmThread run() {
                            return t.getVmThread();
                        }
                    });
                    out.println(SLASH_T + t.getId() + SEPARATOR + t.getName() + SEPARATOR +
                            t.getPriority() + SEPARATOR + vmThread.getThreadStateName());
                    if (threadName != null) {
                        final Object[] trace = VmThread.getStackTrace(vmThread);
                        final int traceLen = trace.length;
                        out.println(SLASH_T + SLASH_T + TRACE);
                        for (int k = 0; k < traceLen; k++) {
                            out.println(SLASH_T + SLASH_T + trace[k]);
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
                showThreads(tg, out, threadName);
            }
        }
    }
}
