/*
 * $Id$
 *
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
 
package org.jnode.shell;

/**
 * The CommandThreadImpl class extends Thread with to implement CommandThread API.
 * 
 * @author crawley@jnode.org
 */
public class CommandThreadImpl extends Thread implements CommandThread {

    private ThreadExitListener listener;
    private CommandRunnable runner;

    /**
     * @param group the parent group for the thread
     * @param runner the runnable that will run the command
     * @param name a thread name
     * @param size the threads stack size
     */
    public CommandThreadImpl(ThreadGroup group, CommandRunnable runner, String name,
            long size) {
        super(group, runner, name, size);
        this.runner = runner;
    }

    /**
     * @param group the parent group for the thread
     * @param runner the Runnable that will run the command
     * @param name the thread name
     */
    public CommandThreadImpl(ThreadGroup group, CommandRunnable runner, String name) {
        super(group, runner, name);
        this.runner = runner;
    }

    /**
     * @param runner the Runnable that will run the command
     * @param name the thread name
     */
    public CommandThreadImpl(CommandRunnable runner, String name) {
        super(runner, name);
        this.runner = runner;
    }

    @Override
    public void run() {
        try {
            super.run();
        } finally {
            runner.flushStreams();
            if (listener != null) {
                listener.notifyThreadExited(this);
            }
        }
    }

    public void start(ThreadExitListener listener) {
        this.listener = listener;
        super.start();
    }

    public Runnable getRunner() {
        return this.runner;
    }

    public int getReturnCode() {
        if (this.runner instanceof CommandRunner) {
            return ((CommandRunner) this.runner).getRC();
        } else {
            return 0;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void stop(ThreadDeath threadDeath) {
        // FIXME - This is unsafe because the thread being killed could be in the
        // middle of updating some critical system data structure.  We should
        // probably throw an exception.
        super.stop(threadDeath);
    }

    public void waitFor() {
        try {
            join();
        } catch (InterruptedException ie) {
            //ignore
        }
    }
}
