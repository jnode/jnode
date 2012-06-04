/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
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
 
package org.jnode.shell;

import java.io.PrintWriter;
import java.io.Reader;

/**
 * This is the API that a shell-based interpreter must implement.  It provides
 * methods for executing single command lines and scripts, together with methods
 * for command completion, argument escaping and incremental help.
 * 
 * @author crawley@jnode.org
 */
public interface CommandInterpreter {
    // FIXME ... change the Shell API so that we can replace occurrences of
    // CommandShell below with Shell.

    public interface Factory {
        CommandInterpreter create();

        String getName();
    }

    /**
     * Parse and execute commands read from a reader, returning the resulting return code.
     * 
     * @param shell the CommandShell that provides low-level command invocation,
     *        command history and so on.
     * @param reader the reader to be interpreted. <b>The implementation must close it.</b> 
     * @param script if {@code true}, the interpreter should read and process commands until
     * the EOF is reached, otherwise it should process just one complete command.
     * @param alias this will supply a script's notional command name to the interpreter.  If 
     * this parameter is {@code null}, no command name passed.
     * @param args optional command line arguments to be passed to the script.  If this parameter 
     * is {@code null}, no arguments are passed.
     * @return the return code.
     * @throws ShellException
     */
    int interpret(CommandShell shell, Reader reader, boolean script, String alias, String[] args) throws ShellException;
    
    /**
     * Parse a partial command line, returning the command line fragment to be
     * completed.  If the interpreter does not support completion, this method
     * should return <code>null</code>.
     * 
     * @param shell the current CommandShell.
     * @param partial a partial command line
     * @return the CommandLine represent the fragment of the supplied command
     *         input to be completed.
     * @throws ShellException 
     */
    Completable parsePartial(CommandShell shell, String partial) throws ShellException;

    /**
     * Get the interpreter's name
     * 
     * @return the name
     */
    String getName();

    /**
     * Add escape sequences (or quotes) to protect any characters in the
     * supplied word so that it can (for example) be appended to a partial
     * command line by the completer.
     * 
     * @param word the word to be escaped
     * @return the word with any necessary escaping or quoting added.
     */
    String escapeWord(String word);
    
    /**
     * Test if the interpreter supports multiline commands.
     * 
     * @return {@code true} if the interpreter supports multiline commands.
     */
    boolean supportsMultiline();

    /**
     * Get the command prompt.
     * 
     * @param shell the shell that is supplying command input.
     * @param continuation {@code true} if the interpreter is expecting a continuation line.
     * @return the command prompt
     */
    String getPrompt(CommandShell shell, boolean continuation);

    /**
     * Get incremental help for the partial command line.  If the interpreter
     * does not support incremental help, it should simply return <code>false</code>.
     * 
     * @param shell the current CommandShell.
     * @param partial a partial command line
     * @param pw the destination for any help information
     * @return <code>true</code> if useful help information was written to 'pw'
     * @throws ShellException 
     */
    boolean help(CommandShell shell, String partial, PrintWriter pw) throws ShellException;
}
