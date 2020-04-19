/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
import java.util.ArrayList;
import java.util.List;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.ThreadArgument;

import static org.jnode.shell.syntax.Argument.MANDATORY;
import static org.jnode.shell.syntax.Argument.MULTIPLE;

/**
 * @author Andreas H\u00e4nel
 * @author crawley@jnode.org
 */
public class KillCommand extends AbstractCommand {

    private static final String help_tid = "the id or name of the threads to kill";
    private static final String help_debug = "if set, print debug information";
    private static final String help_super = "Kill the threads with the supplied ID or name";
    private static final String fmt_prep_kill = "Preparing to kill threads with ID or name(%s)%n";
    private static final String str_found = "Found the Threads: %s%n";
    private static final String fmt_killed = "Killed thread %s%n";
    private static final String fmt_not_found = "Threads %s not found%n";

    private final ThreadArgument argThreadID;
    private final FlagArgument argDebug;

    public KillCommand() {
        super(help_super);
        argThreadID =
            new ThreadArgument("threadNameOrId", MANDATORY | MULTIPLE, help_tid, ThreadArgument.Option.NAME_OR_ID);
        argDebug    = new FlagArgument("debug", Argument.OPTIONAL, help_debug);
        registerArguments(argThreadID, argDebug);
    }

    public static void main(String[] args) throws Exception {
        new KillCommand().execute(args);
    }
    
    @SuppressWarnings("deprecation")
    public void execute() throws Exception {
        PrintWriter out = getError().getPrintWriter();
        boolean debug = argDebug.isSet();
        String[] threadIdOrNames = argThreadID.getValues();
        if (debug) {
            out.format(fmt_prep_kill, threadIdOrNames);
        }
        // In order to kill the threads, we need to traverse the thread group tree, looking
        // for the threads whose 'id' or name matches the supplied ones.  First, find the tree root.
        ThreadGroup grp = Thread.currentThread().getThreadGroup();
        while (grp.getParent() != null) {
            grp = grp.getParent();
        }
        // Next search the tree
        List<Thread> threads = new ArrayList<Thread>();
        StringBuilder threadList = new StringBuilder();
        for (String idOrName : threadIdOrNames) {
            Thread t = findThread(grp, idOrName);
            if (t != null) {
                threads.add(t);

                if (threadList.length() > 0) {
                    threadList.append(", ");
                }
                threadList.append(argThreadID.toString(t));
            }
        }

        // Finally, kill the threads if we found ones.
        if (!threads.isEmpty()) {
            if (debug) {
                out.format(str_found, threadList.toString());
            }

            for (Thread t : threads) {
                // FIXME ... this is bad.  Killing a thread this way could in theory bring down the
                // entire system if we do it at a point where the application thread is executing
                // a method that is updating OS data structures.
                t.stop(new ThreadDeath());
                out.format(fmt_killed, argThreadID.toString(t));
            }
        } else {
            out.format(fmt_not_found, threadList.toString());
            exit(1);
        }
    }

    /**
     * Search thread group 'grp' and its dependents groups for a given thread.
     * @param grp the thread group to search
     * @param idOrName the id or name of the thread we are looking for
     * @return the Thread found or <code>null</code>
     */
    private Thread findThread(ThreadGroup grp, String idOrName) {
        // Search the current thread group
        final int max = grp.activeCount() * 2;
        final Thread[] ts = new Thread[max];
        grp.enumerate(ts);
        for (int i = 0; i < max; i++) {
            final Thread t = ts[i];
            if (t != null) {
                if (argThreadID.accept(t, idOrName)) {
                    return t;
                }
            }
        }
        // Recursively search the child thread groups
        final int gmax = grp.activeGroupCount() * 2;
        final ThreadGroup[] tgs = new ThreadGroup[gmax];
        grp.enumerate(tgs);
        for (int i = 0; i < gmax; i++) {
            final ThreadGroup tg = tgs[i];
            if (tg != null) {
                Thread t = findThread(tg, idOrName);
                if (t != null) {
                    return t;
                }
            }
        }
        // Didn't find it here
        return null;
    }
}
