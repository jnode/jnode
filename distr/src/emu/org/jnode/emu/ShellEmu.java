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
 
package org.jnode.emu;

import java.io.File;

import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.swing.SwingTextScreenConsoleManager;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.shell.CommandShell;

/**
 * @author Levente S\u00e1ntha
 */
public class ShellEmu extends Emu {
    
    public ShellEmu(File root) throws EmuException {
        super(root);
    }

    public static void main(String[] argv) throws Exception {
        if (argv.length > 0 && argv[0].startsWith("-")) {
            System.err.println("Usage: shellemu [<jnode-home>]");
            return;
        }
        new ShellEmu(argv.length > 0 ? new File(argv[0]) : null).run();
    }

    private void run() throws Exception {
        TextScreenConsoleManager cm = new SwingTextScreenConsoleManager();
        new Thread(new CommandShell(cm.createConsole(
            "Console 1",
            (ConsoleManager.CreateOptions.TEXT |
                ConsoleManager.CreateOptions.SCROLLABLE)))).start();
    }
}
