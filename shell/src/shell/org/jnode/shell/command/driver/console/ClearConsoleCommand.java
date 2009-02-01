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
 
package org.jnode.shell.command.driver.console;

import org.jnode.driver.console.TextConsole;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;

/**
 * This command clears the console.
 * 
 * @author Jacob Kofod
 */
public class ClearConsoleCommand extends AbstractCommand {

    public ClearConsoleCommand() {
        super("Clear console screen");
    }

    /**
     * Clear console screen
     * 
     * @param args no arguments.
     */
    public static void main(String[] args) throws Exception {
        new ClearConsoleCommand().execute(args);
    }
    
    @Override
    public void execute() throws Exception {
        final Shell shell = ShellUtils.getCurrentShell();
        TextConsole tc = (TextConsole) shell.getConsole();
        tc.clear();
        tc.setCursor(0, 0);
    }
}
