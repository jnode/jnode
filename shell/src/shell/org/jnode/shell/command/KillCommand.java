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
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;

/**
 * @author Andreas H\u00e4nel
 * @author crawley@jnode.org
 */
public class KillCommand extends AbstractCommand {
    
    private final IntegerArgument ARG_THREADID = 
        new IntegerArgument("threadId", Argument.MANDATORY, "the id of the thread to be kill");
    private final FlagArgument FLAG_DEBUG = 
        new FlagArgument("debug", Argument.OPTIONAL, "if set, print debug information");
    
    public KillCommand() {
        super("kill the thread with the id supplied");
        registerArguments(ARG_THREADID, FLAG_DEBUG);
    }
    
	public static void main(String[] args) throws Exception {
	    new KillCommand().execute(args);
	}
	
	@SuppressWarnings("deprecation")
    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) 
	throws Exception {
	    boolean debug = FLAG_DEBUG.isSet();
	    int threadId = ARG_THREADID.getValue();
	    if (debug) {
	        out.print("preparing to kill thread with id " + threadId);
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
                out.print("found the thread: ");
            }
            out.println(threadId);
            // FIXME ... this is bad.  Killing a thread this way could in theory bring down the
            // entire system if we do it at a point where the application thread is executing
            // a method that is updating OS data structures.
            t.stop(new ThreadDeath());
            out.println("Killed thread " + threadId);
	    }
	    else {
            out.println("Thread " + threadId + " not found");
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
