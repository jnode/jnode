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
 * This is the API that a shell-based interpreter must implement.
 * 
 * @author crawley@jnode.org
 */
public interface CommandInterpreter {

    public interface Factory {
        CommandInterpreter create();

        String getName();
    }

    /**
     * Parse and execute a command line, and return the resulting return code.
     * 
     * @param shell the CommandShell that provides low-level command invocation,
     *        command history and so on.
     * @param line the line of input to be interpreted.
     * @return the return code.
     * @throws ShellException
     */
    int interpret(CommandShell shell, String line) throws ShellException;

    /**
     * Parse a partial command line, returning the command line fragment to be
     * completed.
     * 
     * @param shell the current CommandShell.
     * @param partial a input to the interpreter.
     * @return the CommandLine represent the fragment of the supplied command
     *         input to be completed.
     * @throws ShellException
     */
    Completable parsePartial(CommandShell shell, String partial)
        throws ShellSyntaxException;

    /**
     * Get the interpreter's name
     * 
     * @return the name
     */
    String getName();
}
