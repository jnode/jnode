/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.shell;

/**
 * The CommandThread interface is used by JNode shells, interpreters and invokers 
 * when they need to interact with a thread or thread-like object for running a Command.
 * Its purpose is to hide the API mismatch between Threads and Isolates.
 * 
 * @author crawley@jnode.org
 */
public interface CommandThread {

    /**
     * Cause the command thread to start executing.  If the listener is not <code>null</code>,
     * its "notifyThreadExited" method will be called when the command thread exits normally
     * or with an exception.
     * 
     * @param listener the listener or <code>null</code>
     * @throws ShellInvocationException 
     */
    public void start(ThreadExitListener listener) throws ShellInvocationException;

    /**
     * Get the thread's return code.  This should only be called after the thread
     * has terminated.
     * 
     * @return zero for success, non-zero for failure
     */
    public int getReturnCode();

    /**
     * @return <code>true</code> if the thread is still executing.
     */
    public boolean isAlive();

    /**
     * Tell the thread to stop.  (Temporary API / unspecified semantics)
     * 
     * @param threadDeath
     */
    public void stop(ThreadDeath threadDeath);

    /**
     * The invoker will wait for this thread to terminate.
     * 
     * @throws ShellInvocationException 
     */
    public void waitFor() throws ShellInvocationException;
}
