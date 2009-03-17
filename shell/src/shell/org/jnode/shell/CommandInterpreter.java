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

import java.io.File;
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
     * Parse and execute a command file, returning the resulting return code.
     * 
     * @param shell the CommandShell that provides low-level command invocation,
     *        command history and so on.
     * @param file the file to be interpreted
     * @param alias this will supply the script's notional command name to the interpreter.
     * @param args command line arguments to be passed to the script.  If this parameter 
     * is {@code null}, no arguments are passed.
     * @return the return code.
     * @throws ShellException
     */
    int interpret(CommandShell shell, File file, String alias, String[] args) throws ShellException;

    /**
     * Parse and execute a command file, returning the resulting return code.
     * 
     * @param shell the CommandShell that provides low-level command invocation,
     *        command history and so on.
     * @param reader the reader to be interpreted. <b>The implementation must close it.</b> 
     * @param alias this will supply the script's notional command name to the interpreter.
     * @param args command line arguments to be passed to the script.  If this parameter 
     * is {@code null}, no arguments are passed.
     * @return the return code.
     * @throws ShellException
     */
    int interpret(CommandShell shell, Reader reader, String alias, String[] args) throws ShellException;
    
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

    /**
     * This method should <code>true</code> if the interpreter supports continuation lines.  If so,
     * it should throw IncompleteCommandException if it is expecting more input from the
     * user.  The shell will respond by reading the next line from the user, appending it
     * to the previous input, and attempting to interpret the line again.  Obviously, the
     * interpreter needs to be side-effect free prior to throwing the exception.
     * <p>
     * If this method returns <code>false</code>, the interpreter will treat IncompleteCommandException
     * as a regular ShellSyntaxException.
     * 
     * @return <code>true</code> if this interpreter supports continuation lines.
     */
    boolean supportsMultilineCommands();
}
