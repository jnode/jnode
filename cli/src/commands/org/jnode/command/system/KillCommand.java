/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;

/**
 * @author Andreas H\u00e4nel
 * @author crawley@jnode.org
 */
public class KillCommand extends AbstractCommand {
    
    private static final String help_tid = "the id of the thread to kill";
    private static final String help_debug = "if set, print debug information";
    private static final String help_super = "Kill the thread with the supplied ID";
    private static final String fmt_prep_kill = "Preparing to kill thread with ID(%d)%n";
    private static final String str_found = "Found the Thread: ";
    private static final String fmt_killed = "Killed thread %d%n";
    private static final String fmt_not_found = "Thread %d not found%n";
    
    private final IntegerArgument argThreadID;
    private final FlagArgument argDebug;

    public KillCommand() {
        super(help_super);
        argThreadID = new IntegerArgument("threadId", Argument.MANDATORY, help_tid);
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
        int threadId = argThreadID.getValue();
        if (debug) {
            out.format(fmt_prep_kill, threadId);
        }
        // In order to kill the thread, we need to traverse the thread group tree, looking
        // for the thread whose 'id' matches the supplied one.  First, find the tree root.
        ThreadGroup grp = Thread.currentThread().getThreadGroup();
        while (grp.getParent() != null) {
            grp = grp.getParent();
        }
        // Next search the tree
        Thread t = findThread(grp, threadId);
        // Finally, kill the thread if we found one.
        if (t != null) {
            if (debug) {
                out.print(str_found);
            }
            out.println(threadId);
            // FIXME ... this is bad.  Killing a thread this way could in theory bring down the
            // entire system if we do it at a point where the application thread is executing
            // a method that is updating OS data structures.
            t.stop(new ThreadDeath());
            out.format(fmt_killed, threadId);
        } else {
            out.format(fmt_not_found, threadId);
            exit(1);
        }
    }

    /**
     * Search thread group 'grp' and its dependents groups for a given thread.
     * @param grp the thread group to search
     * @param id the id of the thread we are looking for
     * @return the Thread found or <code>null</code>
     */
    private Thread findThread(ThreadGroup grp, int id) {
        // Search the current thread group
        final int max = grp.activeCount() * 2;
        final Thread[] ts = new Thread[max];
        grp.enumerate(ts);
        for (int i = 0; i < max; i++) {
            final Thread t = ts[i];
            if (t != null) {
                if (t.getId() == id) {
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
                Thread t = findThread(tg, id);
                if (t != null) {
                    return t;
                }
            }
        }
        // Didn't find it here
        return null;
    }
}
