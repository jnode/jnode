/*
 * $Id$
 */
package org.jnode.shell.command.driver.console;

import javax.naming.NameNotFoundException;

import org.jnode.driver.console.AbstractConsoleManager;
import org.jnode.driver.console.ConsoleException;
import org.jnode.driver.console.ConsoleManager;
import org.jnode.driver.console.x86.RawTextConsole;
import org.jnode.driver.console.x86.ScrollableShellConsole;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.CommandShell;
import org.jnode.shell.ShellException;
import org.jnode.shell.ShellUtils;
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

    public static Help.Info HELP_INFO = new Help.Info(
            "console",
            new Syntax[] { new Syntax("Console administration",
                    new Parameter[] { ListParameter, NewUserConsoleParameter}),});

    /**
     * Displays the system date
     * 
     * @param args
     *            No arguments.
     */
    public static void main(String[] args) throws NameNotFoundException,
            ShellException {

        if (args.length > 0) {
            if (args[0].equals("-l")) {
                final ConsoleManager conMgr = (ConsoleManager) InitialNaming
                        .lookup(ConsoleManager.NAME);
                String[] consoles = conMgr.listConsoleNames();
                System.out.println("Nr. of registered consoles: "
                        + consoles.length);
                for (int i = 0; i < consoles.length; i++) {
                    System.out.println("      - " + consoles[ i]);
                }
            } else if (args[0].equals("-n")) {
                final ConsoleManager conMgr = (ConsoleManager) InitialNaming
                        .lookup(ConsoleManager.NAME);
                ScrollableShellConsole console;
                try {
                    console = ((AbstractConsoleManager) conMgr)
                            .createShellConsole();
                    CommandShell commandShell = new CommandShell(console);
                    new Thread(commandShell).start();

                    ShellUtils.getShellManager().registerShell(commandShell);
                    System.out.println("Console created with name:"
                            + console.getConsoleName());

                } catch (ConsoleException e) {
                    System.out.println(e.getMessage());
                }
            }
        } else {
        	System.out.println("test RawTextConsole");
            final ConsoleManager conMgr = (ConsoleManager) InitialNaming
            .lookup(ConsoleManager.NAME);
        	try {
        		RawTextConsole console = new RawTextConsole(conMgr,"testconsole");
				conMgr.registerConsole(console);
				conMgr.focus(console);
				console.clearScreen();
				
			} catch (ConsoleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }
    }
}
