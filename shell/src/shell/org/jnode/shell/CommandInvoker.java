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

package org.jnode.shell;

/*
 * User: Sam Reid Date: Dec 20, 2003 Time: 1:18:31 AM Copyright (c) Dec 20, 2003
 * by Sam Reid
 */

/**
 * This is the common API for the various mechanisms for running 'commands'.
 * 
 * @author Sam Reid
 * @author crawley@jnode.org
 */
public interface CommandInvoker {

    public interface Factory {
        CommandInvoker create(CommandShell shell);

        String getName();
    }

    /**
     * Run a command synchronously, passing back the resulting return code.
     * 
     * @param commandLine this provides the command name (alias), the command
     *        arguments and (where relevant) the command's i/o stream context.
     * @param cmdInfo a CommandInfo descriptor for the command to be run.
     * @return an integer return code, with zero indicating command success,
     *         non-zero indicating command failure.
     * @throws ShellException if there was some problem launching the command.
     */
    int invoke(CommandLine commandLine, CommandInfo cmdInfo) 
        throws ShellException;

    /**
     * Create a thread for running a command asynchronously. This can be used
     * for running and for assembling command pipelines.
     * 
     * @param commandLine this provides the command name (alias), the command
     *        arguments and (where relevant) the command's i/o stream context.
     * @param cmdInfo a CommandInfo descriptor for the command to be run.
     * @return the thread for the command. Calling
     *         {@link java.lang.Thread#start()} will cause the command to
     *         execute.
     * @throws ShellException if there was some problem launching the command.
     */
    CommandThread invokeAsynchronous(CommandLine commandLine, CommandInfo cmdInfo) 
        throws ShellException;

    /**
     * Get the invoker's name.
     * 
     * @return the name.
     */
    String getName();

    /**
     * This method is called to tell the invoker to unblock.
     * FIXME - this is a temporary hack.
     */
    void unblock();

    boolean isDebugEnabled();

    void setDebugEnabled(boolean enabled);
}
