/*
 * $Id: CommandLine.java 3580 2007-11-03 20:31:24Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007 JNode.org
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

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.vm.VmExit;

/**
 * A CommandRunner is a Runnable with a field to record a command's return code.  It also 
 * provides a convenience method for conditionally printing a command stack-trace.
 * 
 * @author crawley@jnode.org
 *
 */
class CommandRunner implements Runnable {
    private final CommandShell shell;
    private final CommandInvoker invoker;
    final InputStream in;
    final PrintStream out;
    final PrintStream err;
    final Class<?> cx;
    final Method method;
    final Object[] args;
    final CommandInfo cmdInfo;
    final CommandLine commandLine;

    private int rc;

    public CommandRunner(CommandShell shell, CommandInvoker invoker,
            CommandInfo cmdInfo, Class<?> cx, Method method, Object[] args,
            InputStream in, PrintStream out, PrintStream err) {
        this.shell = shell;
        this.invoker = invoker;
        this.cx = cx;
        this.method = method;
        this.cmdInfo = cmdInfo;
        this.commandLine = null;
        this.args = args;
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public CommandRunner(CommandShell shell, CommandInvoker invoker, 
            CommandInfo cmdInfo, CommandLine commandLine, 
            InputStream in, PrintStream out, PrintStream err) {
        this.shell = shell;
        this.invoker = invoker;
        this.cx = null;
        this.method = null;
        this.args = null;
        this.cmdInfo = cmdInfo;
        this.commandLine = commandLine;
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public void run() {
        try {
            try {
                if (method != null) {
                    try {
                        AbstractCommand.saveCurrentCommand(cmdInfo.getCommandInstance());
                        Object obj = Modifier.isStatic(method.getModifiers()) ? null
                                : cx.newInstance();
                        AccessController.doPrivileged(new InvokeAction(method, obj,
                                args));
                    }
                    finally {
                        // This clears the current command to prevent leakage.  (This
                        // is only necessary if the current thread could be used to
                        // execute other commands.  But we'll do it anyway ... for now.)
                        AbstractCommand.retrieveCurrentCommand();
                    }
                }
                else {
                    cmdInfo.createCommandInstance().execute(commandLine, in, out, err);
                }
            } catch (PrivilegedActionException ex) {
                Exception ex2 = ex.getException();
                if (ex2 instanceof InvocationTargetException) {
                    throw ex2.getCause();
                }
                else {
                    throw ex2;
                }
            }
            invoker.unblock();
        } catch (SyntaxErrorException ex) {
            try {
                Help.getInfo(cmdInfo.getCommandClass()).usage();
                shell.getConsole().getErr().println(ex.getMessage());
            } catch (HelpException e) {
                shell.getConsole().getErr().println("Exception while trying to get the command usage");
                stackTrace(ex);
            }
            invoker.unblock();
        } catch (VmExit ex) {
            setRC(ex.getStatus());
            invoker.unblock();
        } catch (Exception ex) {
            shell.getConsole().getErr().println("Exception in command");
            stackTrace(ex);
            invoker.unblock();
        } catch (Throwable ex) {
            shell.getConsole().getErr().println("Fatal error in command");
            stackTrace(ex);
            invoker.unblock();
        }
    }

    public int getRC() {
        return rc;
    }

    void setRC(int rc) {
        this.rc = rc;
    }

    boolean isDebugEnabled() {
        return shell.isDebugEnabled();
    }

    void stackTrace(Throwable ex) {
        if (ex != null && isDebugEnabled()) {
            ex.printStackTrace(shell.getConsole().getErr());
        }
    }

}