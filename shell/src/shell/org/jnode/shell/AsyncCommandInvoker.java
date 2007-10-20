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

import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
        this.err = commandShell.getErrorStream();
        commandShell.getConsole().addKeyboardListener(this);// listen for
        // ctrl-c
    }

    public void invoke(String cmdLineStr) {
        commandShell.addCommandToHistory(cmdLineStr);

        InputStream inputStream = commandShell.getInputStream();
        InputStream nextInputStream = null;
        PrintStream errStream = commandShell.getErrorStream();
        PrintStream outputStream = null;
        boolean mustCloseOutputStream = false;

        CommandLine cmdLine;
        Method method;
        Runnable cr;
        CommandInfo cmdInfo;

        String[] commands = cmdLineStr.split("\\|");
        String command;
        ByteArrayOutputStream byteArrayOutputStream = null;

        for (int i = 0; i < commands.length; i++) {
            command = commands[i].trim();
            cmdLine = new CommandLine(command);

            if (!cmdLine.hasNext())
                continue;

            cmdName = cmdLine.next();

            try {
                cmdInfo = commandShell.getCommandClass(cmdName);

                if (cmdLine.sendToOutFile()) {
                	File file = new File(cmdLine.getOutFileName());

                	try {
                		FileOutputStream fileOutputStream = new FileOutputStream(
                				file);
                		outputStream = new PrintStream(fileOutputStream);
                		mustCloseOutputStream = true;
                	} catch (SecurityException e) {
                		e.printStackTrace();
                		return;  // FIXME
                	} catch (FileNotFoundException e) {
                		e.printStackTrace();
                		return;  // FIXME
                	}
                } else if (i + 1 < commands.length) {
                	byteArrayOutputStream = new ByteArrayOutputStream();
                	outputStream = new PrintStream(byteArrayOutputStream);
                } else {
                	outputStream = commandShell.getOutputStream();
                }

                if (byteArrayOutputStream != null) {
                	nextInputStream = new ByteArrayInputStream(
                			byteArrayOutputStream.toByteArray());
                }

                if (nextInputStream != null)
                	inputStream = nextInputStream;

                CommandLine commandLine = null;

                if (inputStream.available() > 0) {
                	// FIXME we shouldn't do this.  It consumes keyboard typeahead
                	// that should be delivered to the command's standard input!!
                	commandLine = new CommandLine(inputStream);
                } else {
                	commandLine = cmdLine.getRemainder();
                }

                commandLine.setOutFileName(cmdLine.getOutFileName());
                try {
                	method = cmdInfo.getCommandClass().getMethod(
                			EXECUTE_METHOD, EXECUTE_ARG_TYPES);

                    cr = createRunner(cmdInfo.getCommandClass(), method,
                            new Object[] { commandLine, inputStream,
                                    outputStream, errStream },
                            inputStream, outputStream, errStream);
                } catch (NoSuchMethodException e) {
                    method = cmdInfo.getCommandClass().getMethod(MAIN_METHOD,
                            MAIN_ARG_TYPES);
                    cr = createRunner(cmdInfo.getCommandClass(), method,
                            new Object[] { cmdLine.getRemainder().toStringArray() },
                            commandShell.getInputStream(), commandShell.getOutputStream(),
                            commandShell.getErrorStream());
                }
                try {
                	if (cmdInfo.isInternal()) {
                        cr.run();
                    } else {
                        threadProcess = createThread(cr, inputStream,
                        		outputStream, errStream);
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
                                    return;  // FIXME
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    err.println("Exception in command");
                    ex.printStackTrace(err);
                    return;  // FIXME
                } catch (Error ex) {
                    err.println("Fatal error in command");
                    ex.printStackTrace(err);
                    return;  // FIXME
                }
            } catch (NoSuchMethodException ex) {
                err.println("Alias class has no main method " + cmdName);
                return;  // FIXME
            } catch (ClassNotFoundException ex) {
                err.println("Unknown alias class " + ex.getMessage());
                return;  // FIXME
            } catch (ClassCastException ex) {
                err.println("Invalid command " + cmdName);
                return;  // FIXME
            } catch (Exception ex) {
                err.println("Unknown error: " + ex.getMessage());
                ex.printStackTrace(err);
                return;  // FIXME
            }
            finally {
            	if (mustCloseOutputStream) {
                    outputStream.close();
                    mustCloseOutputStream = false;
                }
            }
        }

        nextInputStream = null;
    }

    abstract Thread createThread(Runnable cr, InputStream inputStream, 
    		PrintStream outputStream, PrintStream errStream);

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
        if(blockingThread != null && blockingThread.isAlive()) {        
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
    
    abstract Runnable createRunner(Class cx, Method method, Object[] args, 
        		InputStream commandIn, PrintStream commandOut, PrintStream commandErr);

}
