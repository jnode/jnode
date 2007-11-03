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


import java.awt.event.KeyEvent;
import java.io.Closeable;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;

/**
 * User: Sam Reid Date: Dec 20, 2003 Time: 1:20:33 AM Copyright (c) Dec 20, 2003
 * by Sam Reid
 *
 * @author Sam Reid
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author crawley@jnode.org
 */
public abstract class AsyncCommandInvoker implements CommandInvoker, KeyboardListener {

    PrintStream err;

    CommandShell commandShell;

    static final Class[] MAIN_ARG_TYPES = new Class[] { String[].class };

    static final Class[] EXECUTE_ARG_TYPES = new Class[] {
            CommandLine.class, InputStream.class, PrintStream.class,
            PrintStream.class };

    static final String MAIN_METHOD = "main";

    static final String EXECUTE_METHOD = "execute";

    boolean blocking;

    Thread blockingThread;
    Thread threadProcess = null;
    String cmdName;


    public AsyncCommandInvoker(CommandShell commandShell) {
        this.commandShell = commandShell;
        this.err = commandShell.resolvePrintStream(CommandLine.DEFAULT_STDERR);
        commandShell.getConsole().addKeyboardListener(this);// listen for
        // ctrl-c
    }

    public int invoke(CommandLine cmdLine) throws ShellException {
    	CommandInfo cmdInfo = lookupCommand(cmdLine);
    	if (cmdInfo == null) {
    		return 0;
    	}
    	CommandRunner cr = setup(cmdLine, cmdInfo);
    	return runIt(cmdLine, cmdInfo, cr);
    }

    public CommandThread invokeAsynchronous(CommandLine cmdLine) 
    throws ShellException {
    	CommandInfo cmdInfo = lookupCommand(cmdLine);
    	if (cmdInfo == null) {
    		return null;
    	}
    	CommandRunner cr = setup(cmdLine, cmdInfo);
    	return forkIt(cmdLine, cmdInfo, cr);
	}

    private CommandInfo lookupCommand(CommandLine cmdLine) throws ShellInvocationException {
    	String cmdName = cmdLine.getCommandName();
    	if (cmdName == null) {
    		return null;
    	}
                	try {
    		return commandShell.getCommandClass(cmdName);
    	}
    	catch (ClassNotFoundException ex) {
    		throw new ShellInvocationException("Cannot resolve command '" + cmdName + "'");
                }
                }

    private CommandRunner setup(CommandLine cmdLine, CommandInfo cmdInfo) throws ShellException {
        Method method;
        CommandRunner cr = null;

        Closeable[] streams = cmdLine.getStreams();
        InputStream in;
        PrintStream out, err;
        try {
        	in = commandShell.resolveInputStream(streams[0]);
        	out = commandShell.resolvePrintStream(streams[1]);
        	err = commandShell.resolvePrintStream(streams[2]);
        }
        catch (ClassCastException ex) {
        	throw new ShellFailureException("streams array broken", ex);
                }
                try {
                	method = cmdInfo.getCommandClass().getMethod(
                			EXECUTE_METHOD, EXECUTE_ARG_TYPES);
        	if ((method.getModifiers() & Modifier.STATIC) == 0) {
                    cr = createRunner(cmdInfo.getCommandClass(), method,
        				new Object[]{cmdLine, in, out, err},
        				in, out, err);
        	}
                } catch (NoSuchMethodException e) {
        	// continue;
        }
        if (cr == null) {
        	try {
        		method = cmdInfo.getCommandClass().getMethod(MAIN_METHOD, MAIN_ARG_TYPES);
        		if ((method.getModifiers() & Modifier.STATIC) != 0) {
        			if (streams[0] != CommandLine.DEFAULT_STDIN || 
        				streams[1] != CommandLine.DEFAULT_STDOUT || 
        				streams[2] != CommandLine.DEFAULT_STDERR) {
        				throw new ShellInvocationException(
        						"Entry point method for " + cmdInfo.getCommandClass() + 
        						" does not allow redirection or pipelining");
        			}
                    cr = createRunner(cmdInfo.getCommandClass(), method,
        					new Object[]{cmdLine.getArguments()},
        					in, out, err);
                }
        	} catch (NoSuchMethodException e) {
        		// continue;
        	}
        }
        if (cr == null) {
        	throw new ShellInvocationException(
        			"No suitable entry point method for " + cmdInfo.getCommandClass());
        }
        // THese are now the real streams ...
        	cmdLine.setStreams(new Closeable[]{in, out, err});
        return cr;
    }
    
    public int runIt(CommandLine cmdLine, CommandInfo cmdInfo, CommandRunner cr) 
    throws ShellInvocationException {
                try {
                	if (cmdInfo.isInternal()) {
                        cr.run();
                    } else {
    			try {
    				threadProcess = createThread(cmdLine, cr);
    			} catch (Exception ex) {
    				throw new ShellInvocationException("Exception while creating command thread", ex);
    			} 
                        threadProcess.start();

                        this.blocking = true;
                        this.blockingThread = Thread.currentThread();
    			this.cmdName = cmdLine.getCommandName();
    			while (this.blocking) {
                            try {
                                Thread.sleep(6000);
                            } catch (InterruptedException interrupted) {
                                if (!blocking) {
                                    // interruption was okay, break normally.
                                } else {
    						throw new ShellFailureException("unexpected interrupt", interrupted);
                                }
                            }
                        }
                    }
    		return cr.getRC();
                } catch (Exception ex) {
    		throw new ShellInvocationException("Uncaught Exception in command", ex);
                } catch (Error ex) {
    		throw new ShellInvocationException("Fatal Error in command", ex);
            }
            finally {
			this.blockingThread = null;
			this.blocking = false;
            }
        }

    public CommandThread forkIt(CommandLine cmdLine, CommandInfo cmdInfo, CommandRunner cr) 
    throws ShellInvocationException {
    	if (cmdInfo.isInternal()) {
    		throw new ShellFailureException("unexpected internal command");
    	}
    	try {
    		return createThread(cmdLine, cr);
    	} catch (Exception ex) {
    		throw new ShellInvocationException("Exception while creating command thread", ex);
    	} 
    }

    abstract CommandThread createThread(CommandLine cmdLine, CommandRunner cr);

	public void keyPressed(KeyboardEvent ke) {
        //disabling Ctrl-C since currently we have no safe method for killing a thread
        /*
        if (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_C) {
            doCtrlC();
            ke.consume();
        }
        */
        if (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_Z) {
            doCtrlZ();
            ke.consume();
        }
    }

    private void doCtrlZ() {
        if (blockingThread != null && blockingThread.isAlive()) {        
            System.err.println("ctrl-z: Returning focus to console. (" + cmdName
                    + " is still running)");
            unblock();
        }
    }

    @SuppressWarnings("deprecation")
    private void doCtrlC() {
        if (threadProcess != null && threadProcess.isAlive() &&
                blockingThread != null && blockingThread.isAlive()) {
            System.err.println("ctrl-c: Returning focus to console. (" + cmdName
                + " has been killed)");
            unblock();

            AccessController.doPrivileged(new PrivilegedAction<Void>(){
                public Void run() {
                    threadProcess.stop(new ThreadDeath());
                    return null;
                }});
        }
    }

    final void unblock() {
        blocking = false;
        blockingThread.interrupt();
    }

    final boolean isBlocking() {
        return blocking;
    }


    public void keyReleased(KeyboardEvent event) {
    }
    
    abstract CommandRunner createRunner(Class cx, Method method, Object[] args, 
        		InputStream commandIn, PrintStream commandOut, PrintStream commandErr);
}
