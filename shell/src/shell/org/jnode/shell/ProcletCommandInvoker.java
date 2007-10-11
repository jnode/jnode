/*
 * $Id: ThreadCommandInvoker.java 3374 2007-08-02 18:15:27Z lsantha $
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
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.shell.proclet.ProcletContext;
import org.jnode.shell.proclet.ProcletIOContext;
import org.jnode.vm.VmExit;
import org.jnode.vm.VmSystem;

/**
 * User: Sam Reid Date: Dec 20, 2003 Time: 1:20:33 AM Copyright (c) Dec 20, 2003
 * by Sam Reid
 *
 * @author Sam Reid
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author crawley@jnode.org
 */
public class ProcletCommandInvoker extends AsyncCommandInvoker {

    public ProcletCommandInvoker(CommandShell commandShell) {
        super(commandShell);
    }
    
    Thread createThread(Runnable cr, InputStream inputStream, 
    		PrintStream outputStream, PrintStream errStream) {
        VmSystem.switchToExternalIOContext(new ProcletIOContext());
        return ProcletContext.createProclet(
        		cr, null, null,
        		new Object[]{inputStream, outputStream, errStream},
        		cmdName);
    }

    Runnable createRunner(Class cx, Method method, Object[] args, 
    		InputStream commandIn, PrintStream commandOut, PrintStream commandErr) {
		return new CommandRunner(cx, method, args,commandIn, commandOut, commandErr);
	}

	class CommandRunner implements Runnable {
		private final InputStream commandIn;
		private final PrintStream commandOut;
		private final PrintStream commandErr;
        private Class cx;

        Method method;

        Object[] args;
        
        boolean finished = false;

        public CommandRunner(Class cx, Method method, Object[] args, 
        		InputStream commandIn, PrintStream commandOut, PrintStream commandErr) {
            this.cx = cx;
            this.method = method;
            this.args = args;
            this.commandIn = commandIn;
            this.commandOut = commandOut;
            this.commandErr = commandErr;
        }

        public void run() {
            try {
            	try {
            		AccessController.doPrivileged(new PrivilegedAction<Void>() {
        				public Void run() {
        					System.setOut(commandOut);
        					System.setErr(commandErr);
        					System.setIn(commandIn);
        					return null;
        				}
        			});
            		Object obj = null;
                	if(!Modifier.isStatic(method.getModifiers())) {
                		obj = cx.newInstance();
                	}
                		AccessController.doPrivileged(new InvokeAction(method,
                            obj, args));
                } catch (PrivilegedActionException ex) {
                    throw ex.getException();
                }
                if (!isBlocking()) {
                    // somebody already hit ctrl-c.
                } else {
                    finished = true;
                    // System.err.println("Finished invocation, notifying
                    // blockers.");
                    // done with invoke, stop waiting for a ctrl-c
                    unblock();
                }
            } catch (InvocationTargetException ex) {
                Throwable tex = ex.getTargetException();
                if (tex instanceof SyntaxErrorException) {
                    try {
                        Help.getInfo(cx).usage();
                    } catch (HelpException ex1) {
                        // Don't care
                        ex1.printStackTrace();
                    }
                    err.println(tex.getMessage());
                    unblock();
                } else if (tex instanceof VmExit) {
                    err.println(tex.getMessage());
                    unblock();
                } else {
                    err.println("Exception in command");
                    tex.printStackTrace(err);
                    unblock();
                }
            } catch (Exception ex) {
                err.println("Exception in command");
                ex.printStackTrace(err);
                unblock();
            }
            finished = true;
        }
    }
}
