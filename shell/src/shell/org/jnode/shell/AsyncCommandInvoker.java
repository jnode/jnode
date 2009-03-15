/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Properties;

import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.shell.io.CommandIO;

/*
 * User: Sam Reid Date: Dec 20, 2003 Time: 1:20:33 AM Copyright (c) Dec 20, 2003
 * by Sam Reid
 * 
 */

/**
 * This is a base class for command invokers that support launching of
 * asynchronous commands
 * 
 * @author Sam Reid
 * @author Martin Husted Hartvig (hagar@jnode.org)
 * @author crawley@jnode.org
 */
public abstract class AsyncCommandInvoker implements SimpleCommandInvoker,
        KeyboardListener {

    CommandShell commandShell;

    static final Class<?>[] MAIN_ARG_TYPES = new Class[] {String[].class};

    static final Class<?>[] EXECUTE_ARG_TYPES = new Class[] {
        CommandLine.class, InputStream.class, PrintStream.class,
        PrintStream.class
    };

    static final String MAIN_METHOD = "main";

    static final String EXECUTE_METHOD = "execute";

    boolean blocking;
    
    boolean debugEnabled;

    Thread blockingThread;
    CommandThread threadProcess = null;
    String cmdName;

    public AsyncCommandInvoker(CommandShell commandShell) {
        this.commandShell = commandShell;
        
        // listen for ctrl-c
        if (commandShell.getConsole() != null) {
            commandShell.getConsole().addKeyboardListener(this);
        }
        // FIXME ... we need to figure out when / how to detach the listener.
        // At the moment they probably stay attached for ever.  That is not
        // great if lots of invokers are created.
    }

    public int invoke(CommandLine cmdLine, CommandInfo cmdInfo) throws ShellException {
        CommandRunner cr = setup(cmdLine, cmdInfo);
        return runIt(cmdLine, cmdInfo, cr);
    }

    public CommandThread invokeAsynchronous(CommandLine cmdLine, CommandInfo cmdInfo)
        throws ShellException {
        CommandRunner cr = setup(cmdLine, cmdInfo);
        return forkIt(cmdLine, cmdInfo, cr);
    }

    protected CommandRunner setup(CommandLine cmdLine, CommandInfo cmdInfo)
        throws ShellException {
        return setup(cmdLine, cmdInfo, null, null);
    }

    protected CommandRunner setup(CommandLine cmdLine, CommandInfo cmdInfo,
            Properties sysProps, Map<String, String> env)
        throws ShellException {
        Method method;
        CommandRunner cr = null;

        CommandIO[] ios = cmdLine.getStreams();
        boolean redirected = ios[Command.STD_IN] != CommandLine.DEFAULT_STDIN ||
            ios[Command.STD_OUT] != CommandLine.DEFAULT_STDOUT ||
            ios[Command.STD_ERR] != CommandLine.DEFAULT_STDERR;
        try {
            commandShell.resolveStreams(ios);
        } catch (ClassCastException ex) {
            throw new ShellFailureException("streams array broken", ex);
        }
        Command command;
        try {
            command = cmdInfo.createCommandInstance();
        } catch (Exception ex) {
            throw new ShellInvocationException("Problem while creating command instance", ex);
        }
        if (command != null) {
            cr = new CommandRunner(this, cmdInfo, cmdLine, ios, sysProps, env);
        } else {
            try {
                method = cmdInfo.getCommandClass().getMethod(MAIN_METHOD, MAIN_ARG_TYPES);
                int modifiers = method.getModifiers();
                if ((modifiers & Modifier.STATIC) == 0 || (modifiers & Modifier.PUBLIC) == 0) {
                    new ShellInvocationException("The 'main' method for " + 
                            cmdInfo.getCommandClass() + " is not public static");
                }
                if (redirected) {
                    throw new ShellInvocationException(
                            "The 'main' method for " + cmdInfo.getCommandClass() +
                            " does not allow redirection or pipelining");
                }
                // We've checked the method access, and we must ignore the class access.
                method.setAccessible(true);
                cr = new CommandRunner(
                        this, cmdInfo, cmdInfo.getCommandClass(), method,
                        new Object[] {cmdLine.getArguments()}, ios, sysProps, env);
            } catch (NoSuchMethodException e) {
                // continue;
            }
            if (cr == null) {
                throw new ShellInvocationException(
                        "No entry point method found for " + cmdInfo.getCommandClass());
            }
        }
        return cr;
    }

    protected int runIt(CommandLine cmdLine, CommandInfo cmdInfo, CommandRunner cr)
        throws ShellInvocationException {
        try {
            if (cmdInfo.isInternal()) {
                cr.run();
            } else {
                try {
                    threadProcess = createThread(cr);
                } catch (Exception ex) {
                    throw new ShellInvocationException(
                            "Exception while creating command thread", ex);
                }
                this.blocking = true;
                this.blockingThread = Thread.currentThread();
                this.cmdName = cmdLine.getCommandName();

                threadProcess.start(null);
                threadProcess.waitFor();
            }
            return cr.getRC();
        } catch (Exception ex) {
            throw new ShellInvocationException("Uncaught Exception in command",
                    ex);
        } catch (Error ex) {
            throw new ShellInvocationException("Fatal Error in command", ex);
        } finally {
            this.blockingThread = null;
            this.blocking = false;
        }
    }

    protected CommandThread forkIt(CommandLine cmdLine, CommandInfo cmdInfo,
            CommandRunner cr) throws ShellInvocationException {
        if (cmdInfo.isInternal()) {
            throw new ShellFailureException("unexpected internal command");
        }
        try {
            return createThread(cr);
        } catch (Exception ex) {
            throw new ShellInvocationException(
                    "Exception while creating command thread", ex);
        }
    }

    protected abstract CommandThread createThread(CommandRunner cr) 
        throws ShellInvocationException;

    public void keyPressed(KeyboardEvent ke) {
        // FIXME - use KeyEventBindings, etc to make the ^C, ^Z keys soft
        if (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_C) {
            // This seems to 'work' now: at least is it not causing kernel
            // panics all of the time.  But if it proves to be unreliable
            // it should be disabled again.
            doCtrlC(); 
            ke.consume(); 
        } else if (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_Z) {
            doCtrlZ();
            ke.consume();
        }
    }

    private void doCtrlZ() {
        // FIXME - this should suspend the current command, not put it into the
        // background.  It is a BAD IDEA to put a command that is reading from 
        // the keyboard into the background because it will compete with the shell
        // for input.  If we figure out how to prevent this, then ^Z could mean
        // "background, losing control of keyboard input".
        if (blockingThread != null && blockingThread.isAlive()) {
            System.err.println("ctrl-z: Returning focus to console. ("
                    + cmdName + " is still running)");
            unblock();
        }
    }

    private void doCtrlC() {
        if (threadProcess != null && threadProcess.isAlive()
                && blockingThread != null && blockingThread.isAlive()) {
            System.err.println("ctrl-c: Returning focus to console. ("
                    + cmdName + " has been killed)");
            unblock();

            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    threadProcess.stop(new ThreadDeath());
                    return null;
                }
            });
        }
    }

    /**
     * Unblock the thread that is waiting for the invoker to finish.  This should
     * only be used in response to 'job control' actions ("^C", etc), due to the
     * "nasty" way we currently implement it.
     */
    private final void unblock() {
        blocking = false;
        if (blockingThread != null) {
            // FIXME - this is wrong. The thread may not actually be waiting yet,
            // and the interrupt may interfere with whatever is is doing; e.g. in the
            // 'start' method for IsolateCommandThreadImpl.
            blockingThread.interrupt();
        }
    }

    final boolean isBlocking() {
        return blocking;
    }

    public void keyReleased(KeyboardEvent event) {
    }

    @Override
    public boolean isDebugEnabled() {
        return this.debugEnabled;
    }

    @Override
    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }
}
