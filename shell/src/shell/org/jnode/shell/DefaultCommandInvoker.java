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

import java.io.Closeable;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

import org.jnode.shell.help.Help;
import org.jnode.shell.help.SyntaxErrorException;

/*
 * User: Sam Reid Date: Dec 20, 2003 Time: 1:20:33 AM Copyright (c) Dec 20,
 * 2003 by Sam Reid
 */

/**
 * This CommandInvoker runs a command in the current thread, using the command classes
 * <code>public static void main(String[] args)</code> entry point.
 * The {@link invokeAsynchronous()} method is not supported.
 * 
 * @author Sam Reid
 * @author crawley@jnode.org
 */
public class DefaultCommandInvoker implements CommandInvoker {

    private final PrintStream err;
    private final CommandShell commandShell;

    private static final Class[] MAIN_ARG_TYPES = new Class[] { String[].class};

    static final Factory FACTORY = new Factory() {
		public CommandInvoker create(CommandShell shell) {
			return new DefaultCommandInvoker(shell);
		}
		public String getName() {
			return "default";
		}
    };

    public DefaultCommandInvoker(CommandShell commandShell) {
        this.commandShell = commandShell;
        this.err = commandShell.resolvePrintStream(CommandLine.DEFAULT_STDERR);
    }

    public String getName() {
    	return "default";
    }

    public int invoke(CommandLine cmdLine) {
    	String cmdName = cmdLine.getCommandName();
    	if (cmdName == null) {
    		return 0;
    	}
        try {
        	final Closeable[] streams = cmdLine.getStreams();
        	if (streams[0] != CommandLine.DEFAULT_STDIN || 
        		streams[1] != CommandLine.DEFAULT_STDOUT || 
        		streams[2] != CommandLine.DEFAULT_STDERR) {
        		err.println("Warning: redirections ignored by the '" + getName() + "' invoker");
        	}
            CommandInfo cmdInfo = commandShell.getCommandClass(cmdName);
            final Method main = cmdInfo.getCommandClass().getMethod("main", MAIN_ARG_TYPES);
            try {
                try {
                	AccessController.doPrivileged(new PrivilegedAction<Void>() {
        				public Void run() {
        					System.setOut(commandShell.resolvePrintStream(streams[1]));
        					System.setErr(commandShell.resolvePrintStream(streams[2]));
        					System.setIn(commandShell.resolveInputStream(streams[0]));
        					return null;
        				}
        			});
                    final Object[] args = new Object[] { cmdLine.getArguments() };
                    AccessController.doPrivileged(new InvokeAction(main, null, args));
                    return 0;
                } catch (PrivilegedActionException ex) {
                    throw ex.getException();
                }
            } catch (InvocationTargetException ex) {
                Throwable tex = ex.getTargetException();
                if (tex instanceof SyntaxErrorException) {
                    Help.getInfo(cmdInfo.getCommandClass()).usage();
                    err.println(tex.getMessage());
                } else {
                    err.println("Exception in command");
                    stackTrace(tex);
                }
            } catch (Exception ex) {
                err.println("Exception in command");
                stackTrace(ex);
            } catch (Error ex) {
                err.println("Fatal error in command");
                stackTrace(ex);
            }
        } catch (NoSuchMethodException ex) {
            err.println("Alias class has no main method " + cmdName);
        } catch (ClassNotFoundException ex) {
            err.println("Unknown alias class " + ex.getMessage());
        } catch (ClassCastException ex) {
            err.println("Invalid command " + cmdName);
        } catch (Exception ex) {
            err.println("I FOUND AN ERROR: " + ex);
            stackTrace(ex);
        }
        return 1;
    }

	public CommandThread invokeAsynchronous(CommandLine commandLine) {
		throw new UnsupportedOperationException();
	}
	
	private void stackTrace(Throwable ex) {
		if (ex != null && commandShell.isDebugEnabled()) {
			ex.printStackTrace(err);
        }
    }

}
