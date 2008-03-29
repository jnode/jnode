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

import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.SyntaxErrorException;
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

    CommandRunner createRunner(CommandInfo cmdInfo, Method method, Object[] args,
            InputStream commandIn, PrintStream commandOut,
            PrintStream commandErr) {
        return new ProcletCommandRunner(
                cmdInfo, cmdInfo.getCommandClass(), method, args, commandIn,
                commandOut, commandErr);
    }
    
    CommandRunner createRunner(CommandInfo cmdInfo, CommandLine cmdLine,
            InputStream in, PrintStream out, PrintStream err) {
        return new ProcletCommandRunner(cmdInfo, cmdLine, in, out, err);
    }

    class ProcletCommandRunner extends CommandRunner {
        private final InputStream in;
        private final PrintStream out;
        private final PrintStream err;
        private final Class<?> cx;
        private final Method method;
        private final Object[] args;
        private final CommandInfo cmdInfo;
        private final CommandLine commandLine;

        public ProcletCommandRunner(CommandInfo cmdInfo, Class<?> cx, Method method, Object[] args,
                InputStream in, PrintStream out, PrintStream err) {
            super(commandShell);
            this.cx = cx;
            this.method = method;
            this.cmdInfo = cmdInfo;
            this.commandLine = null;
            this.args = args;
            this.in = in;
            this.out = out;
            this.err = err;
        }

        public ProcletCommandRunner(CommandInfo cmdInfo, CommandLine commandLine, 
                InputStream in, PrintStream out, PrintStream err) {
            super(commandShell);
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
                    AccessController.doPrivileged(new PrivilegedAction<Void>() {
                        public Void run() {
                            System.setOut(out);
                            System.setErr(err);
                            System.setIn(in);
                            return null;
                        }
                    });
                    if (method != null) {
                        Object obj = Modifier.isStatic(method.getModifiers()) ? null
                                : cx.newInstance();
                        AccessController.doPrivileged(new InvokeAction(method, obj,
                                args));
                    }
                    else {
                        cmdInfo.getCommandInstance().execute(commandLine, in, out, err);
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
                if (!isBlocking()) {
                    // somebody already hit ctrl-c.
                } else {
                    // done with invoke, stop waiting for a ctrl-c
                    unblock();
                }
            } catch (SyntaxErrorException ex) {
                try {
                    Help.getInfo(cmdInfo.getCommandClass()).usage();
                    err.println(ex.getMessage());
                } catch (HelpException e) {
                    err.println("Exception while trying to get the command usage");
                    stackTrace(ex);
                }
                unblock();
            } catch (VmExit ex) {
                setRC(ex.getStatus());
                unblock();
            } catch (Exception ex) {
                err.println("Exception in command");
                stackTrace(ex);
                unblock();
            } catch (Throwable ex) {
                err.println("Fatal error in command");
                stackTrace(ex);
                unblock();
            }
        }
    }
}
