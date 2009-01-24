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

import java.io.File;
import java.io.Writer;

import org.jnode.driver.console.Console;
import org.jnode.driver.console.InputCompleter;
import org.jnode.driver.console.InputHistory;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.syntax.SyntaxManager;

/**
 * @author epr
 */
public interface Shell extends InputCompleter {

    /**
     * Gets the alias manager for this shell
     */
    public AliasManager getAliasManager();
    
    /**
     * Gets the syntax manager for this shell
     */
    public SyntaxManager getSyntaxManager();

    /**
     * Gets the shell's command InputHistory object.  Unlike getInputHistory,
     * this method is not modal.
     */
    public InputHistory getCommandHistory();
    
    /**
     * Record all console output from the shell and commands launched by the shell.
     * 
     * @param writer The stream for recording output.
     */
    public void addConsoleOuputRecorder(Writer writer);
    
    /**
     * Returns the console where the shell is running.
     * @return the console
     */
    public Console getConsole();
    
    /**
     * Run a command file in the shell.  If the first line of the file is of the form
     * "#!&lt;interpreter&gt;", where "&lt;interpreter&gt;" has been registered with
     * the ShellManager, the file will be run using the nominated interpreter.  Otherwise,
     * the shell gets to decide which interpreter to use.
     * 
     * @param file the command file
     * @throws ShellException 
     */
    public int runCommandFile(File file) throws ShellException;

    /**
     * Resolve a command name to a CommandInfo object.
     * 
     * @param commmandName this could be a command class name, an alias or some other
     * supported by the shell.
     * @return the resolved CommandInfo or <code>null</code>.
     * @throws ShellException 
     */
    public CommandInfo getCommandInfo(String commmandName) throws ShellException;

    /**
     * Add quoting or escape sequences to a word using the escaping conventions of 
     * the shell's current interpreter.
     * 
     * @param word the word to be escaped.
     * @return the 
     */
    public String escapeWord(String word);

}
