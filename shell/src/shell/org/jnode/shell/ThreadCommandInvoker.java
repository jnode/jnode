/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.shell;

import gnu.java.security.action.InvokeAction;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.SyntaxErrorException;

import java.awt.event.KeyEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;

/**
 * User: Sam Reid Date: Dec 20, 2003 Time: 1:20:33 AM Copyright (c) Dec 20,
 * 2003 by Sam Reid
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */
public class ThreadCommandInvoker implements CommandInvoker, KeyboardListener {

    PrintStream err;

    CommandShell commandShell;

    private static final Class[] MAIN_ARG_TYPES = new Class[] { String[].class};
    private static final Class[] EXECUTE_ARG_TYPES = new Class[] { CommandLine.class,InputStream.class,PrintStream.class,PrintStream.class};

    private static final String MAIN_METHOD = "main";
    private static final String EXECUTE_METHOD = "execute";

    private boolean blocking;

    private Thread blockingThread;

    private String cmdName;

    public ThreadCommandInvoker(CommandShell commandShell) {
        this.commandShell = commandShell;
        this.err = commandShell.getErrorStream();
        commandShell.getConsole().addKeyboardListener(this);//listen for
        // ctrl-c
    }

    public void invoke(String cmdLineStr) {
        final CommandLine cmdLine = new CommandLine(cmdLineStr);
        if (!cmdLine.hasNext()) return;
        cmdName = cmdLine.next();

        commandShell.addCommandToHistory(cmdLineStr);
        try {
            CommandInfo cmdInfo = commandShell.getCommandClass(cmdName);
          Method method;
          CommandRunner cr;

          InputStream inputStream = System.in;  // will also be dynamic later
          PrintStream errStream = System.err;   // will also be dynamic later
          PrintStream outputStream = null;

          try
          {
            method = cmdInfo.getCommandClass().getMethod(EXECUTE_METHOD, EXECUTE_ARG_TYPES);

            if (cmdLine.sendToOutFile())
            {
              File file = new File(cmdLine.getOutFileName());

              try
              {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                outputStream = new PrintStream(fileOutputStream);
              }
              catch (SecurityException e)
              {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
              }
              catch (FileNotFoundException e)
              {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
              }
            }
            else
            {
              outputStream = System.out;
            }

            cr = new CommandRunner(cmdInfo.getCommandClass(), method, new Object[]{cmdLine,inputStream,outputStream,errStream});
          }
          catch (Exception e)
          {
            method = cmdInfo.getCommandClass().getMethod(MAIN_METHOD, MAIN_ARG_TYPES);
            cr = new CommandRunner(cmdInfo.getCommandClass(), method, new Object[]{cmdLine.getRemainder().toStringArray()});
          }
            try {

                if (cmdInfo.isInternal()) {
                	cr.run();
                } else {
                	Thread threadProcess = new Thread(cr, cmdName);
                	threadProcess.start();
                	//                cr.run();
                	this.blocking = true;
                	this.blockingThread = Thread.currentThread();
                	while (blocking) {
                		try {
                			Thread.sleep(6000);
                		} catch (InterruptedException interrupted) {
                			if (!blocking) {
                				//interruption was okay, break normally.
                			} else {
                				//abnormal interruption
                				interrupted.printStackTrace();
                			}
                		}
                	}


                }

                if (outputStream != null && outputStream != System.out)
                {
                  outputStream.close();
                }
                //                System.err.println("Finished invoke.");
            } catch (Exception ex) {
                err.println("Exception in command");
                ex.printStackTrace(err);
            } catch (Error ex) {
                err.println("Fatal error in command");
                ex.printStackTrace(err);
            }
        } catch (NoSuchMethodException ex) {
            err.println("Alias class has no main method " + cmdName);
        } catch (ClassNotFoundException ex) {
            err.println("Unknown alias class " + ex.getMessage());
        } catch (ClassCastException ex) {
            err.println("Invalid command " + cmdName);
        } catch (Exception ex) {
            err.println("Unknown error: " + ex.getMessage());
            ex.printStackTrace(err);
        }
    }

    public void keyPressed(KeyboardEvent ke) {
        if (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_C) {
            doCtrlC();
        }
    }

    private void doCtrlC() {
        System.err.println("ctrl-c: Returning focus to console. (" + cmdName
                + " is still running.)");
        unblock();
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

        private Class cx;

        Method method;

        Object[] args;

        boolean finished = false;

        public CommandRunner(Class cx, Method method, Object[] args) {
            this.cx = cx;
            this.method = method;
            this.args = args;
        }

        public void run() {
            try {
                //                System.err.println("Registering shell in new thread.");
                ShellUtils.getShellManager().registerShell(commandShell);//workaround
                // to
                // ensure
                // access
                // to
                // the
                // command
                // shell
                // in
                // this
                // new
                // thread?
                try {
                    AccessController.doPrivileged(new InvokeAction(method,
                            null, args));
                } catch (PrivilegedActionException ex) {
                    throw ex.getException();
                }
                if (!isBlocking()) {
                    //somebody already hit ctrl-c.
                } else {
                    finished = true;
                    //                    System.err.println("Finished invocation, notifying
                    // blockers.");
                    //done with invoke, stop waiting for a ctrl-c
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
