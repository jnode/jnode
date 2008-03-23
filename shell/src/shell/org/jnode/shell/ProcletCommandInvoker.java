/*
 * $Id: ThreadCommandInvoker.java 3374 2007-08-02 18:15:27Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2003-2007 JNode.org
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

import java.io.Closeable;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

import org.jnode.shell.proclet.ProcletContext;
import org.jnode.shell.proclet.ProcletIOContext;
import org.jnode.vm.VmExit;
import org.jnode.vm.VmSystem;

/*
 * User: Sam Reid Date: Dec 20, 2003 Time: 1:20:33 AM Copyright (c) Dec 20, 2003
 * by Sam Reid
 */

/**
 * This command invoker runs commands in their own proclet, giving each one its
 * own stdin,out,err etcetera.
 * 
 * @author Sam Reid
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author crawley@jnode.org
 */
public class ProcletCommandInvoker extends AsyncCommandInvoker {

    public static final Factory FACTORY = new Factory() {
        public CommandInvoker create(CommandShell shell) {
            return new ProcletCommandInvoker(shell);
        }

        public String getName() {
            return "proclet";
        }
    };

    public ProcletCommandInvoker(CommandShell commandShell) {
        super(commandShell);
    }

    public String getName() {
        return "proclet";
    }

    CommandThread createThread(CommandLine cmdLine, CommandRunner cr) {
        Closeable[] streams = cmdLine.getStreams();
        VmSystem.switchToExternalIOContext(new ProcletIOContext());
        return ProcletContext.createProclet(cr, null, null, new Object[] {
                streams[0], streams[1], streams[2] }, cmdLine.getCommandName());
    }

    CommandRunner createRunner(Class<?> cx, Method method, Object[] args,
            InputStream commandIn, PrintStream commandOut,
            PrintStream commandErr) {
        return new ProcletCommandRunner(cx, method, args, commandIn,
                commandOut, commandErr);
    }
    
    CommandRunner createRunner(Command command, CommandLine cmdLine,
            InputStream in, PrintStream out, PrintStream err) {
        return new ProcletCommandRunner(command, cmdLine, in, out, err);
    }

    class ProcletCommandRunner extends CommandRunner {
        private final InputStream in;
        private final PrintStream out;
        private final PrintStream err;
        private final Class<?> cx;
        private final Method method;
        private final Object[] args;
        private final Command command;
        private final CommandLine commandLine;

        private boolean finished = false;

        public ProcletCommandRunner(Class<?> cx, Method method, Object[] args,
                InputStream in, PrintStream out, PrintStream err) {
            super(commandShell);
            this.cx = cx;
            this.method = method;
            this.command = null;
            this.commandLine = null;
            this.args = args;
            this.in = in;
            this.out = out;
            this.err = err;
        }

        public ProcletCommandRunner(Command command, CommandLine commandLine, 
                InputStream in, PrintStream out, PrintStream err) {
            super(commandShell);
            this.cx = null;
            this.method = null;
            this.args = null;
            this.command = command;
            this.commandLine = commandLine;
            this.in = in;
            this.out = out;
            this.err = err;
        }

        public void run() {
            try {
                try {
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        public Void run() {
                            System.setOut(out);
                            System.setErr(err);
                            System.setIn(in);
                            return null;
                        }
                    });
                    if (command == null) {
                        Object obj = Modifier.isStatic(method.getModifiers()) ? null
                                : cx.newInstance();
                        AccessController.doPrivileged(new InvokeAction(method, obj,
                                args));
                    }
                    else {
                        command.execute(commandLine, in, out, err);
                    }
                } catch (PrivilegedActionException ex) {
                    throw ex.getException();
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
