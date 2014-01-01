/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.command.system;

import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.Comparator;
import java.util.TreeSet;
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
 * @author Levente S\u00e1ntha
 */
public class ThreadCommand extends AbstractCommand {
    
    private static final String help_name = "the name of a specific thread to be printed";
    private static final String help_group = "output a ThreadGroup dump";
    private static final String help_verbose = "show all threads in thread groups";
    private static final String help_super = "View info about all threads, or a specific thread";
    
    private static final String SEPARATOR = ", ";
    private static final String SLASH_T = "\t";
    private static final String GROUP = "Group ";
    private static final String TRACE = "Stack trace";

    private final ThreadNameArgument argName;
    private final FlagArgument argDump;
    private final FlagArgument argVerbose;

    public ThreadCommand() {
        super(help_super);
        argName = new ThreadNameArgument("threadName", Argument.OPTIONAL, help_name);
        argDump = new FlagArgument("groupDump", Argument.OPTIONAL, help_group);
        argVerbose = new FlagArgument("verbose", Argument.OPTIONAL, help_verbose);
        registerArguments(argName, argVerbose, argDump);
    }

    public static void main(String[] args) throws Exception {
        new ThreadCommand().execute(args);
    }

    /**
     * Execute this command
     */
    public void execute() throws Exception {
        // If threadName is null, we'll print all threads
        String threadName = (argName.isSet()) ? argName.getValue() : null;
        boolean dump = argDump.isSet();

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
            if (!argVerbose.isSet() && !argName.isSet()) {
                showDefaultInfo(grp);
            } else {
                // Show the threads in the ThreadGroup tree.
                showThreads(grp, getOutput().getPrintWriter(), threadName);
            }
        }
    }

    private void showDefaultInfo(ThreadGroup grp) {
        TreeSet<Thread> threadSet = new TreeSet<Thread>(new Comparator<Thread>() {
            @Override
            public int compare(Thread t1, Thread t2) {
                return Long.valueOf(t1.getId()).compareTo(t2.getId());
            }
        });
        findThreads(grp, threadSet);

        PrintWriter out = getOutput().getPrintWriter();
        for (final Thread thread : threadSet) {
            VmThread vmThread = AccessController.doPrivileged(new PrivilegedAction<VmThread>() {
                public VmThread run() {
                    return ThreadHelper.getVmThread(thread);
                }
            });
            out.println(" " + thread.getId() + SEPARATOR + thread.getName() + SEPARATOR + thread.getPriority() +
                SEPARATOR + vmThread.getThreadStateName());
        }
    }

    private void findThreads(ThreadGroup grp, TreeSet<Thread> threadSet) {
        final int max = grp.activeCount() * 2;
        final Thread[] ts = new Thread[max];
        grp.enumerate(ts);
        for (int i = 0; i < max; i++) {
            final Thread t = ts[i];
            if (t != null) {
                threadSet.add(t);
            }
        }
        final int gmax = grp.activeGroupCount() * 2;
        final ThreadGroup[] tgs = new ThreadGroup[gmax];
        grp.enumerate(tgs);
        for (int i = 0; i < gmax; i++) {
            final ThreadGroup tg = tgs[i];
            if (tg != null) {
                findThreads(tg, threadSet);
            }
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
                            return ThreadHelper.getVmThread(t);
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
