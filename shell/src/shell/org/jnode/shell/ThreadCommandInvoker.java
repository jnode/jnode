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

import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;

import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.vm.VmExit;

/**
 * User: Sam Reid Date: Dec 20, 2003 Time: 1:20:33 AM Copyright (c) Dec 20, 2003
 * by Sam Reid
 * 
 * @author Sam Reid
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class ThreadCommandInvoker implements CommandInvoker, KeyboardListener {
	
	private class StreamHolder {
		InputStream inputStream, nextInputStream;
        PrintStream errStream, outputStream;
        ByteArrayOutputStream byteArrayOutputStream;
    	
    	public StreamHolder(InputStream inputStream, PrintStream outputStream, PrintStream errStream) {
			this.inputStream = inputStream;
			this.errStream = errStream;
			this.outputStream = outputStream;
		}
	}
	
	private class NoGo extends Exception {
		NoGo(Throwable cause) {
			super(cause);
		}
	}

    PrintStream err;

    CommandShell commandShell;

    private static final Class[] MAIN_ARG_TYPES = new Class[] { String[].class };

    private static final Class[] EXECUTE_ARG_TYPES = new Class[] {
            CommandLine.class, InputStream.class, PrintStream.class,
            PrintStream.class };

    private static final String MAIN_METHOD = "main";

    private static final String EXECUTE_METHOD = "execute";

    private boolean blocking;

    private Thread blockingThread;

    private Thread threadProcess = null;

    private String cmdName;

    public ThreadCommandInvoker(CommandShell commandShell) {
        this.commandShell = commandShell;
        this.err = commandShell.getErrorStream();
        commandShell.getConsole().addKeyboardListener(this);// listen for
        // ctrl-c
    }

    public void invoke(String cmdLineStr) {
        commandShell.addCommandToHistory(cmdLineStr);
        StreamHolder streams = new StreamHolder(System.in, null, System.err);

        CommandLine cmdLine;
        CommandRunner cr;
        CommandInfo cmdInfo;

        String[] commands = cmdLineStr.split("\\|");
        String command;

        for (int i = 0; i < commands.length; i++) {
            command = commands[i].trim();
            cmdLine = new CommandLine(command);

            if (!cmdLine.hasNext())
                continue;

            cmdName = cmdLine.next();

            try {
                cmdInfo = commandShell.getCommandClass(cmdName);

                cr = buildExecuteRunner(cmdName, cmdLine, cmdInfo, 
                    		streams, i != commands.length - 1);
                if (cr == null) {
                	cr = buildMainRunner(cmdName, cmdLine, cmdInfo);
                }
                if (cr == null) {
                	err.println("Class " + cmdInfo.getCommandClass().getName() + 
                			" has no suitable 'execute' or 'main' method");
                	break;
                }
                try {
                    if (cmdInfo.isInternal()) {
                        cr.run();
                    } else {
                        threadProcess = new Thread(cr, cmdName);
                        threadProcess.start();

                        this.blocking = true;
                        this.blockingThread = Thread.currentThread();
                        while (blocking) {
                            try {
                                Thread.sleep(6000);
                            } catch (InterruptedException interrupted) {
                                if (!blocking) {
                                    // interruption was okay, break normally.
                                } else {
                                    // abnormal interruption
                                    interrupted.printStackTrace();
                                }
                            }
                        }
                    }

                    if (streams.outputStream != null && streams.outputStream != System.out) {
                        streams.outputStream.close();
                    }
                    // System.err.println("Finished invoke.");
                } catch (Exception ex) {
                    err.println("Exception in command");
                    ex.printStackTrace(err);
                    break;
                } catch (Error ex) {
                    err.println("Fatal error in command");
                    ex.printStackTrace(err);
                    break;
                }
            } catch (NoGo ex) {
            	err.println("Exception occurred while prepareing to run command");
            	ex.getCause().printStackTrace(err);
            } catch (ClassNotFoundException ex) {
                err.println("Unknown alias class " + ex.getMessage());
            } catch (ClassCastException ex) {
                err.println("Invalid command " + cmdName);
            } catch (Exception ex) {
                err.println("Unknown error: " + ex.getMessage());
                ex.printStackTrace(err);
            }
        }
    }
    
    private CommandRunner buildExecuteRunner(String cmdName, 
    		CommandLine cmdLine, CommandInfo cmdInfo, StreamHolder streams, boolean last) 
    throws NoGo {
    	Method method;
    	Object target;
    	try {
    		method = cmdInfo.getCommandClass().getMethod(
    				EXECUTE_METHOD, EXECUTE_ARG_TYPES);
    		if ((method.getModifiers() & Modifier.STATIC) == 0) {
    			return null;
    		}
    	}
    	catch (NoSuchMethodException ex) {
    		return null;
    	}

    	try {
    		target = cmdInfo.getCommandClass().newInstance();
    	}
    	catch (Exception ex) {
    		throw new NoGo(ex);
    	}
    	
    	if (cmdLine.sendToOutFile()) {
            File file = new File(cmdLine.getOutFileName());

            try {
                FileOutputStream fileOutputStream = new FileOutputStream(
                        file);
                streams.outputStream = new PrintStream(fileOutputStream);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else if (!last) {
            streams.byteArrayOutputStream = new ByteArrayOutputStream();
            streams.outputStream = new PrintStream(streams.byteArrayOutputStream);
        } else {
        	streams.outputStream = System.out;
        }

        if (streams.byteArrayOutputStream != null) {
        	streams.nextInputStream = new ByteArrayInputStream(
        			streams.byteArrayOutputStream.toByteArray());
        }

        if (streams.nextInputStream != null)
        	streams.inputStream = streams.nextInputStream;

        CommandLine commandLine;
        try {
        	commandLine = (streams.inputStream.available() > 0) ?
        			new CommandLine(streams.inputStream) : cmdLine.getRemainder();
        }
        catch (IOException ex) {
        	throw new NoGo(ex);
        }
        		
        commandLine.setOutFileName(cmdLine.getOutFileName());

        return new CommandRunner(cmdInfo.getCommandClass(), method, target, 
                new Object[] { commandLine, streams.inputStream,
        					   streams.outputStream, streams.errStream });
    }
    
    private CommandRunner buildMainRunner(String cmdName, 
    		CommandLine cmdLine, CommandInfo cmdInfo) {
    	try {
    		Method method = 
    			cmdInfo.getCommandClass().getMethod(MAIN_METHOD, MAIN_ARG_TYPES);
    		if ((method.getModifiers() & Modifier.STATIC) == 0) {
    			return null;
    		}
    		return new CommandRunner(cmdInfo.getCommandClass(), method, null,
    				new Object[] {cmdLine.getRemainder().toStringArray()});
    	}
    	catch (NoSuchMethodException ex) {
    		return null;
    	}
    }

    public void keyPressed(KeyboardEvent ke) {
        if (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_C) {
            doCtrlC();
        }
        if (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_Z) {
            doCtrlZ();
        }
    }

    private void doCtrlZ() {
        System.err.println("ctrl-z: Returning focus to console. (" + cmdName
                + " is still running)");
        unblock();
    }

    @SuppressWarnings("deprecation")
    private void doCtrlC() {
        System.err.println("ctrl-c: Returning focus to console. (" + cmdName
                + " has been killed)");

        if (threadProcess != null) {
            unblock();

            AccessController.doPrivileged(new PrivilegedAction(){
                public Object run() {
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

    class CommandRunner implements Runnable {

        private final Class cx;
        private final Method method;
        private final Object target;
        private final Object[] args;

        boolean finished = false;

        public CommandRunner(Class cx, Method method, Object target, Object[] args) {
            this.cx = cx;
            this.method = method;
            this.args = args;
            this.target = target;
        }

        public void run() {
            try {
                // System.err.println("Registering shell in new thread.");
                try {

                    AccessController.doPrivileged(
                    		new InvokeAction(method, target, args));
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
            } catch (Error ex) {
            	err.println("Error in command");
            	ex.printStackTrace(err);
            	unblock();
            }
            finished = true;
        }

    }
}
