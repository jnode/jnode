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

import java.io.InputStream;
import java.io.PrintStream;

import javax.isolate.Isolate;
import javax.isolate.IsolateStartupException;
import javax.naming.NameNotFoundException;

import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.TextConsole;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellManager;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * ConsoleCommand starts a new console.
 * 
 * @author vali
 * @author crawley@jnode.org
 */
public class ConsoleCommand extends AbstractCommand {

    private final FlagArgument FLAG_LIST = new FlagArgument(
            "list", Argument.OPTIONAL, "if set, list all registered consoles");

    private final FlagArgument FLAG_NEW = new FlagArgument(
            "new", Argument.OPTIONAL, "if set, create a new console");

    private final FlagArgument FLAG_ISOLATED = new FlagArgument(
            "isolated", Argument.OPTIONAL, "if set, create the new console in an isolate");

    private final FlagArgument FLAG_TEST = new FlagArgument(
            "test", Argument.OPTIONAL, "if set, create a raw text console (test)");

    
    public ConsoleCommand() {
        super("Console administration");
        registerArguments(FLAG_ISOLATED, FLAG_LIST, FLAG_NEW, FLAG_TEST);
    }

    public static void main(String[] args) throws Exception {
        new ConsoleCommand().execute(args);
    }

    @Override
    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) 
        throws NameNotFoundException, IsolateStartupException, ShellException {

        final ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
        final ConsoleManager conMgr = sm.getCurrentShell().getConsole().getManager();

        boolean listConsoles = FLAG_LIST.isSet();
        boolean newConsole = FLAG_NEW.isSet();
        boolean isolateNewConsole = FLAG_ISOLATED.isSet();
        boolean test = FLAG_TEST.isSet();

        if (listConsoles) {
            conMgr.printConsoles(System.out);
        } else if (newConsole) {
            if (isolateNewConsole) {
                try {
                    Isolate newIsolate = new Isolate(
                            ConsoleCommand.IsolatedConsole.class.getName(), 
                            new String[0]);
                    newIsolate.start();
                    out.println("Started new isolated console");
                } catch (IsolateStartupException ex) {
                    out.println("Failed to start new isolated console");
                    throw ex;
                }
            } else {
                createConsoleWithShell(conMgr, out);
            }
        } else if (test) {
            out.println("test RawTextConsole");
            final TextConsole console = (TextConsole) conMgr.createConsole(
                    null, ConsoleManager.CreateOptions.TEXT | ConsoleManager.CreateOptions.NO_LINE_EDITTING);
            conMgr.registerConsole(console);
            conMgr.focus(console);
            console.clear();
        }
    }

    private static class IsolatedConsole {
        /**
         * This will be the entry point for the isolate.
         * @param args
         */
        public static void main(String[] args) {
            try {
                final ShellManager sm = InitialNaming.lookup(ShellManager.NAME);
                final ConsoleManager conMgr = sm.getCurrentShell().getConsole().getManager();
                TextConsole console = createConsoleWithShell(conMgr, System.out);
                System.setIn(console.getIn());
                System.setOut(new PrintStream(console.getOut()));
                System.setErr(new PrintStream(console.getErr()));
            } catch (Exception ex) {
                // FIXME
                System.out.println("Problem creating the isolated console");
                ex.printStackTrace(System.err);
            }
        }
    }

    private static TextConsole createConsoleWithShell(final ConsoleManager conMgr, PrintStream out) 
        throws ShellException {
        final TextConsole console = (TextConsole) conMgr.createConsole(null,
                ConsoleManager.CreateOptions.TEXT | ConsoleManager.CreateOptions.SCROLLABLE);
        CommandShell commandShell = new CommandShell(console);
        new Thread(commandShell, "command-shell").start();

        out.println("New console created with name: " + console.getConsoleName());

        // FIXME we shouldn't be setting the invoker (and interpreter) via the System Properties
        // object because it is "global" in some operation modes, and we want to be able to 
        // control the invoker / interpreter on a per-console basis.
        String invokerName = System.getProperty(CommandShell.INVOKER_PROPERTY_NAME, "");
        // FIXME this is a temporary hack until we decide what to do about these invokers
        if ("thread".equals(invokerName) || "default".equals(invokerName)) {
            PrintStream err = new PrintStream(console.getErr());
            err.println(
                    "Warning: any commands run in this console via their main(String[]) will " +
                    "have the 'wrong' System.out and System.err.");
            err.println("The 'proclet' invoker should give better results.");
            err.println("To use it, type 'exit', run 'set jnode.invoker proclet' " +
                    "in the F1 console and run 'console -n' again.");
        }
        return console;
    }
}
