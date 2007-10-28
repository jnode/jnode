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
 * The CommandThread class enhances Thread with a simple mechanism for recording
 * a 'command's return code.
 * 
 * @author crawley@jnode.org
 */
public class CommandThread extends Thread {
	
	private int rc;
	private ThreadExitListener listener;
	
	/**
	 * @param group the parent group for the thread
	 * @param target the runnable that implements the command
	 * @param name a thread name
	 * @param size the threads stack size
	 * @param invoker the invoker to be notified of the thread's exit
	 */
	public CommandThread(ThreadGroup group, Runnable target, String name, long size) {
		super(group, target, name, size);
	}

	/**
	 * @param group the parent group for the thread
	 * @param target the runnable that implements the command
	 * @param invoker the invoker to be notified of the thread's exit
	 */
	public CommandThread(ThreadGroup group, Runnable target) {
		super(group, target);
	}

	/**
	 * @param target the runnable that implements the command
	 * @param name a thread name
	 * @param invoker the invoker to be notified of the thread's exit
	 */
	public CommandThread(Runnable target, String name) {
		super(target, name);
	}

	@Override
	public void run() {
		try {
		super.run();
		}
		finally {
		if (listener != null) {
			listener.notifyThreadExitted(this);
		}
	}
	}

	/**
	 * This overload for start registers an optional thread exit listener
	 * to be notified of the thread's exit
	 * 
	 * @param listener the listener or <code>null</code>
	 */
	public void start(ThreadExitListener listener) {
		this.listener = listener;
		super.start();
	}

	public final int getReturnCode() {
		return rc;
	}

	public final void setReturnCode(int rc) {
		this.rc = rc;
	}
	
	public static final void setRC(int rc) throws ClassCastException {
        Thread thread = Thread.currentThread();
        //todo this is not true for "internal" commands like 'classpath', why?
        if(thread instanceof CommandThread)
            ((CommandThread) thread).setReturnCode(rc);
	}
}
