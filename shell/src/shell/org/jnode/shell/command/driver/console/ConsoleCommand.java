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

package org.jnode.shell.command.driver.console;

import java.awt.event.KeyEvent;
import java.util.Set;

import javax.isolate.Isolate;
import javax.isolate.IsolateStartupException;
import javax.naming.NameNotFoundException;

import org.jnode.driver.console.Console;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.driver.console.textscreen.TextScreenConsoleManager;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellManager;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.Syntax;

/**
 * @author vali
 */
public class ConsoleCommand {

    private static Parameter ListParameter = new Parameter(new Argument("-l",
            "list all registered consoles", false), Parameter.OPTIONAL);

    private static Parameter NewUserConsoleParameter = new Parameter(
            new Argument("-n", "create new Shell Console", false),
            Parameter.OPTIONAL);

    private static Parameter IsolateNewConsoleParameter = new Parameter(
            new Argument("-i", "Isolate new Shell Console", false),
            Parameter.OPTIONAL);

    public static Help.Info HELP_INFO = new Help.Info("console",
            new Syntax[] { new Syntax("Console administration",
                    new Parameter[] { ListParameter, NewUserConsoleParameter,
                            IsolateNewConsoleParameter }), });

    /**
     * Displays the system date
     * 
     * @param args
     *            No arguments.
     */
    public static void main(String[] args) throws NameNotFoundException,
            ShellException {

        final ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
        final ConsoleManager conMgr = sm.getCurrentShell().getConsole().getManager();
        
        boolean listConsoles = false;
        boolean newConsole = false;
        boolean isolateNewConsole = false;
        
        System.out.print("args: ");
        for (String arg : args) {
            System.out.print(arg);
            if (arg.equals("-l")) {
                listConsoles = true;
            } else if (arg.equals("-n")) {
                newConsole = true;
            } else if (arg.equals("-i")) {
                isolateNewConsole = true;
            }
        }
        System.out.println();
        
        if (listConsoles) {
            conMgr.printConsoles(System.out);
            /*
            final Set<String> consoleNames = conMgr.getConsoleNames();
            System.out.println("Nr. of registered consoles: "
                    + consoleNames.size());
            for (String name : consoleNames) {
                final Console console = conMgr.getConsole(name);
                System.out.println("      - "
                        + name
                        + " ACCEL:"
                        + KeyEvent.getKeyText(console
                                .getAcceleratorKeyCode()));
            }
            */
        } else if (newConsole) {
            if (isolateNewConsole) {
                try {
                    Isolate newIsolate = new Isolate(
                    		ConsoleCommand.IsolatedConsole.class.getName(), 
                    		new String[0]);
                    newIsolate.start();
                    System.out.println("Started new isolated console");
                } catch (IsolateStartupException ex) {
                    System.out.println("Failed to start new isolated console");
                    ex.printStackTrace(System.err);
                }
            } else {
            	createConsoleWithShell(conMgr);
            }
        } else {
            System.out.println("test RawTextConsole");
            final TextConsole console = (TextConsole) conMgr.createConsole(
                    null, ConsoleManager.CreateOptions.TEXT | ConsoleManager.CreateOptions.NO_LINE_EDITTING);
            conMgr.registerConsole(console);
            conMgr.focus(console);
            console.clear();
        }
    }
    
    private static class IsolatedConsole {

		public static void main(String[] args) {
			try {
			    final ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
	            final ConsoleManager conMgr = sm.getCurrentShell().getConsole().getManager();
	            TextConsole console = createConsoleWithShell(conMgr);
	            System.setIn(console.getIn());
	            System.setOut(console.getOut());
	            System.setErr(console.getErr());
			}
			catch (Exception ex) {
				// FIXME
                System.out.println("Problem creating the isolated console");
                ex.printStackTrace(System.err);
			}
		}
    }
    
    private static TextConsole createConsoleWithShell(final ConsoleManager conMgr) 
    throws ShellException {
    	final TextConsole console = (TextConsole) conMgr.createConsole(
                null, ConsoleManager.CreateOptions.TEXT
                | ConsoleManager.CreateOptions.SCROLLABLE);
        CommandShell commandShell = new CommandShell(console);
        new Thread(commandShell, "command-shell").start();
        
        System.out.println("Console created with name:"
                + console.getConsoleName());
        return console;
    }
}
