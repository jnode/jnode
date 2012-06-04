/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

import org.jnode.shell.help.HelpFactory;
import org.jnode.shell.syntax.CommandSyntaxException;
import org.jnode.shell.io.CommandIO;
import org.jnode.vm.VmExit;

/*
 * User: Sam Reid Date: Dec 20, 2003 Time: 1:20:33 AM Copyright (c) Dec 20, 2003
 * by Sam Reid
 */

/**
 * This CommandInvoker runs a command in the current thread, using the command
 * classes <code>public static void main(String[] args)</code> entry point.
 * The {@link #invokeAsynchronous(CommandLine)} method is not 
 * supported for this implementation of the CommandInvoker API.
 * 
 * @author Sam Reid
 * @author crawley@jnode.org
 */
public class DefaultCommandInvoker implements SimpleCommandInvoker {

    private final PrintWriter err;
    private final CommandShell shell;

    private static final Class<?>[] MAIN_ARG_TYPES = new Class[] {String[].class};

    public static final Factory FACTORY = new Factory() {
        public SimpleCommandInvoker create(CommandShell shell) {
            return new DefaultCommandInvoker(shell);
        }

        public String getName() {
            return "default";
        }
    };

    public DefaultCommandInvoker(CommandShell shell) {
        this.shell = shell;
        this.err = shell.resolveStream(CommandLine.DEFAULT_STDERR).getPrintWriter();
    }

    public String getName() {
        return "default";
    }
    
    /**
     * Invoke the command, running is by calling the entry point method from the
     * current thread.  No redirection is allowed.
     * 
     * @param cmdLine the command line.
     */
    public int invoke(CommandLine cmdLine) throws ShellException {
        CommandInfo cmdInfo = cmdLine.parseCommandLine(shell);
        String cmdName = cmdLine.getCommandName();
        if (cmdName == null) {
            return 0;
        }
        // FIXME ... the exception handling and error diagnosis could do with
        // some reworking.
        try {
            final CommandIO[] ios = cmdLine.getStreams();
            if (ios[0] != CommandLine.DEFAULT_STDIN ||
                    ios[1] != CommandLine.DEFAULT_STDOUT ||
                    ios[2] != CommandLine.DEFAULT_STDERR) {
                err.println("Warning: redirections ignored by the '"
                        + getName() + "' invoker");
            }
            final Method main = cmdInfo.getCommandClass().getMethod("main",
                    MAIN_ARG_TYPES);
            int modifiers = main.getModifiers();
            if ((modifiers & Modifier.STATIC) == 0 || (modifiers & Modifier.PUBLIC) == 0) {
                System.err.println("The 'main' method for " + cmdInfo.getCommandClass() +
                        " is not public static");
                return 1;
            }
            try {
                try {
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        public Void run() {
                            System.setOut(shell.resolvePrintStream(ios[1]));
                            System.setErr(shell.resolvePrintStream(ios[2]));
                            System.setIn(shell.resolveInputStream(ios[0]));
                            // We've checked the method access, and we must ignore the class access.
                            main.setAccessible(true);
                            return null;
                        }
                    });
                    AbstractCommand.saveCurrentCommand(cmdInfo.getCommandInstance());
                    final Object[] args = new Object[] {cmdLine.getArguments()};
                    AccessController.doPrivileged(new InvokeAction(main, null, args));
                    return 0;
                } catch (PrivilegedActionException ex) {
                    Exception ex2 = ex.getException();
                    if (ex2 instanceof InvocationTargetException) {
                        throw ex2.getCause();
                    } else {
                        throw ex2;
                    }
                }
            } catch (ShellControlException ex) {
                throw ex;
            } catch (CommandSyntaxException ex) {
                HelpFactory.getHelpFactory().getHelp(cmdName, cmdInfo).usage(err);
                err.println(ex.getMessage());
            } catch (VmExit ex) {
                return ex.getStatus();
            } catch (Exception ex) {
                err.println("Exception in command");
                stackTrace(ex);
            } catch (Throwable ex) {
                err.println("Fatal error in command");
                stackTrace(ex);
            }
        } catch (ShellControlException ex) {
            throw ex;
        } catch (NoSuchMethodException ex) {
            err.println("Alias class has no main method " + cmdName);
        } catch (ClassCastException ex) {
            err.println("Invalid command " + cmdName);
        } catch (Exception ex) {
            err.println("I FOUND AN ERROR: " + ex);
            stackTrace(ex);
        } finally {
            // This clears the current command to prevent leakage.
            AbstractCommand.retrieveCurrentCommand();
        }
        return 1;
    }
    
    /**
     * This method is not supported for the DefaultCommandInvoker.
     */
    public CommandThread invokeAsynchronous(CommandLine commandLine) throws ShellException {
        throw new UnsupportedOperationException();
    }

    private void stackTrace(Throwable ex) {
        if (ex != null && shell.isDebugEnabled()) {
            ex.printStackTrace(err);
        }
    }

    public boolean isDebugEnabled() {
        return shell.isDebugEnabled();
    }

}
