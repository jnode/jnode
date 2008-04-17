/*
 * $Id: ConsoleCommand.java 3580 2007-11-03 20:31:24Z lsantha $
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

package org.jnode.shell.command.driver.console;

import javax.naming.NameNotFoundException;

import org.jnode.driver.console.TextConsole;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellManager;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.Syntax;

/**
 * @author Jacob Kofod
 */
public class ClearConsoleCommand {

    public static Help.Info HELP_INFO = 
        new Help.Info("clear", new Syntax[] { new Syntax("Clear console screen", new Parameter[] {}), }
        );

    /**
     * Clear console screen
     * 
     * @param args no arguments.
     */
    public static void main(String[] args) 
    throws NameNotFoundException, ShellException {
        final ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
        TextConsole tc = (TextConsole) sm.getCurrentShell().getConsole();
        tc.clear();
        tc.setCursor(0, 0);
    }
}
