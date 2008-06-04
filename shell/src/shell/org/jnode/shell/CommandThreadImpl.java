/*
 * $Id: ThreadCommandInvoker.java 3374 2007-08-02 18:15:27Z lsantha $
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

package org.jnode.shell;

/**
 * The CommandThreadImpl class extends Thread with to implement CommandThread API.
 * 
 * @author crawley@jnode.org
 */
public class CommandThreadImpl extends Thread implements CommandThread {

    private ThreadExitListener listener;
    private Runnable runner;

    /**
     * @param group the parent group for the thread
     * @param runner the runnable that implements the command
     * @param name a thread name
     * @param size the threads stack size
     */
    public CommandThreadImpl(ThreadGroup group, Runnable runner, String name,
            long size) {
        super(group, runner, name, size);
        this.runner = runner;
    }

    /**
     * @param group the parent group for the thread
     * @param runner the runnable that implements the command
     */
    public CommandThreadImpl(ThreadGroup group, Runnable runner) {
        super(group, runner);
        this.runner = runner;
    }

    /**
     * @param runner the runnable that implements the command
     * @param name a thread name
     */
    public CommandThreadImpl(Runnable runner, String name) {
        super(runner, name);
        this.runner = runner;
    }

    @Override
    public void run() {
        try {
            super.run();
        } finally {
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
        super.stop(threadDeath);
    }
}
