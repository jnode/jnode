/*Copyright, Sam Reid, 2003.*/
package org.jnode.shell;

import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.SyntaxError;

import java.awt.event.KeyEvent;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * User: Sam Reid
 * Date: Dec 20, 2003
 * Time: 1:20:33 AM
 * Copyright (c) Dec 20, 2003 by Sam Reid
 */
public class ThreadCommandInvoker implements CommandInvoker, KeyboardListener {
    PrintStream err;
    CommandShell commandShell;
    private static final Class[] MAIN_ARG_TYPES = new Class[]{String[].class};
    private boolean blocking;
    private Thread blockingThread;
    private String cmdName;

    public ThreadCommandInvoker(CommandShell commandShell) {
        this.commandShell = commandShell;
        this.err = commandShell.getErrorStream();
        commandShell.getConsole().addKeyboardListener(this);//listen for ctrl-c
    }

    public void invoke(String cmdLineStr) {
        final CommandLine cmdLine = new CommandLine(cmdLineStr);
        if (!cmdLine.hasNext())
            return;
        cmdName = cmdLine.next();

        commandShell.addCommandToHistory(cmdLineStr);
//        System.err.println("Got command: " + cmdLineStr + ", name=" + cmdName);
        String[]args=cmdLine.getRemainder().toStringArray();
//        System.out.println("args.length = " + args.length);
//        for (int i = 0; i < args.length; i++) {
//            String arg = args[i];
//            System.out.println("arg["+i+"] = " + arg);
//        }
        try {
            Class cmdClass = commandShell.getCommandClass(cmdName);

//            System.err.println("CmdClass=" + cmdClass);
            final Method main = cmdClass.getMethod("main", MAIN_ARG_TYPES);
//            System.err.println("main=" + main);
            try {
//                System.err.println("Invoking...");
                CommandRunner cr = new CommandRunner(cmdClass, main, new Object[]{args});
                Thread threadProcess = new Thread(cr,cmdName);
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
            err.println("I FOUND AN ERROR: " + ex);
        }
    }

    public void keyPressed(KeyboardEvent ke) {
        if (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_C) {
            doCtrlC();
        }
    }

    private void doCtrlC() {
        System.err.println("ctrl-c: Returning focus to console. ("+cmdName+" is still running.)");
        unblock();
    }

    private void unblock() {

        blocking = false;
        blockingThread.interrupt();
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
                ShellUtils.getShellManager().registerShell(commandShell);//workaround to ensure access to the command shell in this new thread?
                method.invoke(null, args);
                if (!blocking) {
                    //somebody already hit ctrl-c.
                } else {
                    finished = true;
//                    System.err.println("Finished invocation, notifying blockers.");
                    blocking = false;//done with invoke, stop waiting for a ctrl-c
                    blockingThread.interrupt();
                }
            } catch (InvocationTargetException ex) {
                Throwable tex = ex.getTargetException();
                if (tex instanceof SyntaxError) {
                    Help.getInfo(cx).usage();
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
