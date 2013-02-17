/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
import java.util.TreeMap;

import org.jnode.driver.console.Console;
import org.jnode.driver.console.InputCompleter;
import org.jnode.driver.console.InputHistory;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.syntax.SyntaxManager;

/**
 * This is the interface is implemented by JNode command shells. 
 * 
 * @author epr
 * @author crawley@jnode.org
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
     * the shell decides which interpreter to use.
     * <p>
     * A command alias and arguments are passed to the interpreter, but it is up to the
     * interpreter to decide what if anything to do with them.  Some interpreters ignore them.
     * 
     * @param file the command file
     * @param alias the command alias used to launch the script.  If this parameter is
     * {@code null}, the command file name will be used.
     * @param args command line arguments to be passed to the script.  If this parameter 
     * is {@code null}, no arguments are passed.
     * @throws ShellException 
     */
    public int runCommandFile(File file, String alias, String[] args) throws ShellException;

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
     * @return the escaped word.
     */
    public String escapeWord(String word);
    
    /**
     * Set a shell property.  Some properties have special meaning to a Shell
     * and may cause its behavior to change.
     * 
     * @param key the name of the property
     * @param value the property value
     * @throws ShellException This may be thrown if the name / value pair is
     *     not acceptable.
     */
    public void setProperty(String key, String value) throws ShellException;
    
    /**
     * Get the current value of a shell property.  
     * 
     * @param key the property name.
     * @return the property value or {@code null}
     */
    public String getProperty(String key);
    
    /**
     * Remove a shell property.  Special properties typically may not be removed,
     * 
     * @param key the name of the property
     * @throws ShellException This may be thrown if the property cannot be removed.
     */
    public void removeProperty(String key) throws ShellException;
    
    /**
     * Get the shell properties for this shell instance.  The result is a copy
     * of the shell properties object; i.e. changes to the result Map object
     * have no effect on the shell.
     * <p>
     * Note that shell properties are 
     * not the same as UNIX-style shell variables.  An interpreter that supports
     * shell variables may mirror some of them in the properties, but it is not
     * required to.  The recommended place for publishing (exported) shell variables 
     * is the "environment variables"; e.g. in {@link System#getenv()}.
     * 
     * @return a copy of the shell properties.
     */
    public TreeMap<String, String> getProperties();

}
