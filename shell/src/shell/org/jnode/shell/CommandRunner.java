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

import gnu.java.security.action.InvokeAction;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Map;
import java.util.Properties;

import org.jnode.shell.help.HelpException;
import org.jnode.shell.help.HelpFactory;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.shell.io.CommandIO;
import org.jnode.shell.io.CommandOutput;
import org.jnode.vm.VmExit;

/**
 * A CommandRunner is a Runnable with a field to record a command's return code.  It also 
 * provides a convenience method for conditionally printing a command stack-trace.
 * 
 * @author crawley@jnode.org
 */
public class CommandRunner implements CommandRunnable {

    static final Class<?>[] MAIN_ARG_TYPES = new Class[] {String[].class};

    static final Class<?>[] EXECUTE_ARG_TYPES = new Class[] {
        CommandLine.class, InputStream.class, PrintStream.class,
        PrintStream.class
    };

    static final String MAIN_METHOD = "main";

    static final String EXECUTE_METHOD = "execute";
    
    private final SimpleCommandInvoker invoker;
    private final CommandIO[] ios;
    final CommandLine commandLine;
    final PrintWriter shellErr;
    final Properties sysProps;
    final Map<String, String> env;
    final boolean redirected;
    final CommandInfo cmdInfo;
    
    Class<?> targetClass;
    Method method;
    Object[] args;
    private int rc;
    

    public CommandRunner(SimpleCommandInvoker invoker, CommandLine commandLine, CommandInfo cmdInfo, CommandIO[] ios,
            Properties sysProps, Map<String, String> env, boolean redirected) {
        this.invoker = invoker;
        this.redirected = redirected;
        this.commandLine = commandLine;
        this.cmdInfo = cmdInfo;
        this.ios = ios;
        this.shellErr = ios[Command.SHELL_ERR].getPrintWriter();
        this.env = env;
        this.sysProps = sysProps;
    }

    public void run() {
        try {
            prepare();
            execute();
        } catch (SyntaxErrorException ex) {
            try {
                HelpFactory.getHelpFactory().getHelp(commandLine.getCommandName(), cmdInfo).usage(shellErr);
                shellErr.println(ex.getMessage());
            } catch (HelpException e) {
                shellErr.println("Exception while trying to get the command usage");
                stackTrace(ex);
            }
        } catch (VmExit ex) {
            setRC(ex.getStatus());
        } catch (Exception ex) {
            shellErr.println("Exception in command");
            stackTrace(ex);
        } catch (Throwable ex) {
            shellErr.println("Fatal error in command");
            stackTrace(ex);
        }
    }
    
    /**
     * Prepare the command for execution.
     *
     * If the command is not a type of Command then the main method will
     * be prepared for invoking with arguments supplied from the command
     * line.
     *
     * This will fail to complete if the command line parsing fails, or if
     * the main method for the class could not be found.
     */
    private void prepare() throws ShellException {
        Command command;
        try {
            command = cmdInfo.createCommandInstance();
        } catch (Exception ex) {
            throw new ShellInvocationException("Problem while creating command instance", ex);
        }
        // make this happen before setting up the main() method call for
        // bare commands. For commands without a bare command definition
        // this will return silently having done nothing.
        cmdInfo.parseCommandLine(commandLine);
        if (command == null) {
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
                targetClass = cmdInfo.getCommandClass();
                args = new Object[] {commandLine.getArguments()};
            } catch (NoSuchMethodException e) {
                throw new ShellInvocationException(
                        "No entry point method found for " + cmdInfo.getCommandClass());
            }
        }
    }
    
    /**
     * Executes the command.
     *
     * For a JNode command, this will initialie and execute the command. For
     * other commands the main method will be invoked via reflection with the
     * set of arguments supplied on the command line.
     */
    private void execute() throws Throwable {
        try {
            if (method != null) {
                try {
                    // This saves the Command instance that has the command line state
                    // associated in a thread-local so that the Abstract.execute(String[])
                    // method can get hold of it.  This is the magic that allows a command
                    // that implements 'main' as "new MyCommand().execute(args)" to get the
                    // parsed command line arguments, etc.
                    AbstractCommand.saveCurrentCommand(cmdInfo.getCommandInstance());

                    // Call the command's entry point method reflectively
                    Object obj = Modifier.isStatic(method.getModifiers()) ? null
                            : targetClass.newInstance();
                    AccessController.doPrivileged(new InvokeAction(method, obj,
                            args));
                } finally {
                    // This clears the current command to prevent possible leakage of
                    // commands arguments, etc to the next command.
                    AbstractCommand.retrieveCurrentCommand();
                }
            } else {
                // For a command that implements the Command API, call the 'new'
                // execute method.  If it is not 'execute()' is not overridden by the
                // command class, the default implementation from AbstractCommand will
                // bounce us to the older execute(CommandLine, InputStream, PrintStream,
                // PrintStream) method.
                Command cmd = cmdInfo.createCommandInstance();
                cmd.initialize(commandLine, getIOs());
                cmd.execute();
            }
        } catch (PrivilegedActionException ex) {
            Exception ex2 = ex.getException();
            if (ex2 instanceof InvocationTargetException) {
                throw ex2.getCause();
            } else {
                throw ex2;
            }
        }
    }

    public int getRC() {
        return rc;
    }

    void setRC(int rc) {
        this.rc = rc;
    }

    void stackTrace(Throwable ex) {
        if (ex != null && invoker.isDebugEnabled()) {
            ex.printStackTrace(shellErr);
        }
    }
    
    public Method getMethod() {
        return method;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Object[] getArgs() {
        return args;
    }

    public CommandIO[] getIOs() {
        return ios;
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public String getCommandName() {
        return commandLine != null ? commandLine.getCommandName() : null;
    }

    public Properties getSysProps() {
        return sysProps;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public CommandInfo getCommandInfo() {
        return cmdInfo;
    }

    public void flushStreams() {
        for (CommandIO io : ios) {
            if (io instanceof CommandOutput) {
                try {
                    ((CommandOutput) io).flush();
                } catch (IOException ex) {
                    // Ignore for now.
                }
            }
        }
    }
}
