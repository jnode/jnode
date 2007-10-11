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

import gnu.java.security.action.InvokeAction;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.SyntaxErrorException;

/**
 * User: Sam Reid Date: Dec 20, 2003 Time: 1:20:33 AM Copyright (c) Dec 20,
 * 2003 by Sam Reid
 */
public class DefaultCommandInvoker implements CommandInvoker {

    PrintStream err;

    CommandShell commandShell;

    private static final Class[] MAIN_ARG_TYPES = new Class[] { String[].class};

    public DefaultCommandInvoker(CommandShell commandShell) {
        this.commandShell = commandShell;
        this.err = commandShell.getErrorStream();
    }

    public void invoke(String cmdLineStr) {
        final CommandLine cmdLine = new CommandLine(cmdLineStr);
        if (!cmdLine.hasNext()) return;
        String cmdName = cmdLine.next();

        commandShell.addCommandToHistory(cmdLineStr);
        //        System.err.println("Got command: "+cmdLineStr+", name="+cmdName);
        try {
            CommandInfo cmdInfo = commandShell.getCommandClass(cmdName);
            //            System.err.println("CmdClass="+cmdClass);
            final Method main = cmdInfo.getCommandClass().getMethod("main", MAIN_ARG_TYPES);
            //            System.err.println("main="+main);
            try {
                //                System.err.println("Invoking...");
                try {
                	AccessController.doPrivileged(new PrivilegedAction<Void>() {
        				public Void run() {
        					System.setOut(commandShell.getOutputStream());
        					System.setErr(commandShell.getErrorStream());
        					System.setIn(commandShell.getInputStream());
        					return null;
        				}
        			});
                    final Object[] args = new Object[] { cmdLine.getRemainder()
                            .toStringArray()};
                    AccessController.doPrivileged(new InvokeAction(main, null, args));
                } catch (PrivilegedActionException ex) {
                    throw ex.getException();
                }
                
                //main.invoke(null, );
                //                System.err.println("Finished invoke.");
            } catch (InvocationTargetException ex) {
                Throwable tex = ex.getTargetException();
                if (tex instanceof SyntaxErrorException) {
                    Help.getInfo(cmdInfo.getCommandClass()).usage();
                    err.println(tex.getMessage());
                } else {
                    err.println("Exception in command");
                    tex.printStackTrace(err);
                }
            } catch (Exception ex) {
                err.println("Exception in command");
                ex.printStackTrace(err);
            } catch (Error ex) {
                err.println("Fatal error in command");
                ex.printStackTrace(err);
            }
        } catch (NoSuchMethodException ex) {
            err.println("Alias class has no main method " + cmdName);
        } catch (ClassNotFoundException ex) {
            err.println("Unknown alias class " + ex.getMessage());
        } catch (ClassCastException ex) {
            err.println("Invalid command " + cmdName);
        } catch (Exception ex) {
            err.println("I FOUND AN ERROR: " + ex);
        }
    }

}
