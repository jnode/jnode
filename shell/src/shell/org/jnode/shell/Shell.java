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

import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.syntax.SyntaxManager;
import org.jnode.driver.console.Console;
import org.jnode.driver.console.InputCompleter;
import org.jnode.driver.console.InputHistory;

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
     * Shell callback to register a list of choices for command line completion.
     */
    public void list(String[] items);

    /**
     * Gets the shell's command InputHistory object.  Unlike getInputHistory,
     * this method is not modal.
     */
    public InputHistory getCommandHistory();
    
    /**
     * Returns the console where the shell is running.
     * @return the console
     */
    public Console getConsole();

}
