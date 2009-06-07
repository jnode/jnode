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

    protected final CommandShell shell;

    boolean blocking;

    Thread blockingThread;
    CommandThread threadProcess = null;
    String cmdName;

    public AsyncCommandInvoker(CommandShell shell) {
        this.shell = shell;
        
        // listen for ctrl-c
        if (shell.getConsole() != null) {
            shell.getConsole().addKeyboardListener(this);
        }
        // FIXME ... we need to figure out when / how to detach the listener.
        // At the moment they probably stay attached for ever.  That is not
        // great if lots of invokers are created.
    }

    public int invoke(CommandLine commandLine) throws ShellException {
        CommandRunner cr = setup(commandLine);
        return runIt(commandLine, cr);
    }

    public CommandThread invokeAsynchronous(CommandLine commandLine) throws ShellException {
        CommandRunner cr = setup(commandLine);
        return forkIt(commandLine, cr);
    }

    protected CommandRunner setup(CommandLine cmdLine)
        throws ShellException {
        return setup(cmdLine, null, null);
    }

    protected CommandRunner setup(CommandLine cmdLine, Properties sysProps, Map<String, String> env)
        throws ShellException {
        CommandIO[] ios = cmdLine.getStreams();
        boolean redirected = ios[Command.STD_IN] != CommandLine.DEFAULT_STDIN ||
            ios[Command.STD_OUT] != CommandLine.DEFAULT_STDOUT ||
            ios[Command.STD_ERR] != CommandLine.DEFAULT_STDERR;
        try {
            shell.resolveStreams(ios);
        } catch (ClassCastException ex) {
            throw new ShellFailureException("streams array broken", ex);
        }
        // Make sure that the command info is set
        cmdLine.getCommandInfo(shell);
        return new CommandRunner(this, cmdLine, ios, sysProps, env, redirected);
    }

    protected int runIt(CommandLine cmdLine, CommandRunner cr) throws ShellException {
        Throwable terminatingException = null;
        int rc = -1;
        try {
            if (cr.isInternal()) {
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
            terminatingException = cr.getTerminatingException();
            rc = cr.getRC();
        } catch (ShellInvocationException ex) {
            throw ex;
        } catch (Throwable ex) {
            terminatingException = ex;
        } finally {
            this.blockingThread = null;
            this.blocking = false;
        }
        if (terminatingException != null) {
            if (terminatingException instanceof ShellControlException) {
                throw (ShellControlException) terminatingException;
            }
            shell.diagnose(terminatingException, cmdLine);
        }
        return rc;
    }

    protected CommandThread forkIt(CommandLine cmdLine, CommandRunner cr) throws ShellInvocationException {
        if (cr.isInternal()) {
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
        return shell.isDebugEnabled();
    }
}
