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

import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.vm.VmExit;

/*
 * User: Sam Reid Date: Dec 20, 2003 Time: 1:20:33 AM Copyright (c) Dec 20, 2003
 * by Sam Reid
 */

/**
 * This invoker launches commands in regular threads. These will typically share
 * stdin,out,err etc with other commands in the current isolate. Hence
 * stream redirection should be forbidden for commands launched via the 'main' 
 * entry point, and a command launched via Command.execute will need to use
 * the execute in,out,err arguments to access the command's redirected streams.
 * 
 * @author Sam Reid
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author crawley@jnode.org
 */
public class ThreadCommandInvoker extends AsyncCommandInvoker {

    static final Factory FACTORY = new Factory() {
        public CommandInvoker create(CommandShell shell) {
            return new ThreadCommandInvoker(shell);
        }

        public String getName() {
            return "thread";
        }
    };

    public ThreadCommandInvoker(CommandShell commandShell) {
        super(commandShell);
    }

    public String getName() {
        return "thread";
    }

    CommandThread createThread(CommandLine cmdLine, CommandRunner cr) {
        return new CommandThread(cr, cmdLine.getCommandName());
    }

    CommandRunner createRunner(Class<?> cx, Method method, Object[] args,
            InputStream in, PrintStream out,
            PrintStream err) {
        return new ThreadCommandRunner(cx, method, args, in, out, err);
    }

    CommandRunner createRunner(Command command, CommandLine cmdLine,
            InputStream in, PrintStream out, PrintStream err) {
        return new ThreadCommandRunner(command, cmdLine, in, out, err);
    }

    class ThreadCommandRunner extends CommandRunner {
        private final Class<?> cx;
        private final Method method;
        private final Object[] args;
        private final CommandLine cmdLine;
        private final Command command;
        private final InputStream in;
        private final PrintStream out;
        private final PrintStream err;
        
        private boolean finished = false;

        public ThreadCommandRunner(Class<?> cx, Method method, Object[] args,
                InputStream in, PrintStream out, PrintStream err) {
            super(commandShell);
            this.cx = cx;
            this.method = method;
            this.args = args;
            this.command = null;
            this.cmdLine = null;
            this.in = in;
            this.out = out;
            this.err = err;
        }

        public ThreadCommandRunner(Command command, CommandLine cmdLine,
                InputStream in, PrintStream out, PrintStream err) {
            super(commandShell);
            this.cx = null;
            this.method = null;
            this.args = null;
            this.command = command;
            this.cmdLine = cmdLine;
            this.in = in;
            this.out = out;
            this.err = err;
        }

        public void run() {
            try {
                if (command == null) {
                    Object obj = Modifier.isStatic(method.getModifiers()) ? null
                            : cx.newInstance();
                    try {
                        AccessController.doPrivileged(new InvokeAction(method, obj,
                                args));
                    } catch (PrivilegedActionException ex) {
                        throw ex.getException();
                    }
                }
                else {
                    command.execute(cmdLine, in, out, err);
                }

                if (!isBlocking()) {
                    // somebody already hit ctrl-c.
                } else {
                    finished = true;
                    // done with invoke, stop waiting for a ctrl-c
                    unblock();
                }
            } catch (VmExit ex) {
                setRC(ex.getStatus());
                unblock();
            } catch (Exception ex) {
                err.println("Exception in command");
                stackTrace(ex);
                unblock();
            } catch (Error ex) {
                err.println("Fatal error in command");
                stackTrace(ex);
                unblock();
            }
            finished = true;
        }
    }
}
