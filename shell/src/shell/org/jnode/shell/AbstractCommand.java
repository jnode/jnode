/*
 * $Id: Command.java 3772 2008-02-10 15:02:53Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

import java.io.InputStream;
import java.io.PrintStream;

import javax.naming.NameNotFoundException;

import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.NoSuchAliasException;
import org.jnode.shell.io.CommandIO;
import org.jnode.shell.io.CommandInput;
import org.jnode.shell.io.CommandOutput;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.vm.VmExit;

/**
 * This a base class for JNode native command objects.  It provides default implementations 
 * of the 'execute' entry points, and other methods defined by the Command API.  It also
 * provides 'getter' methods for retrieving the CommandLine and CommandIO stream
 * objects, and an 'exit' method.
 * <p>
 * The class also provides some infrastructure that allows native commands to implement
 * a lightweight 'public static void main(String[])' entry point.  This allows them to be
 * executed by the minimalist 'DefaultCommandInvoker', and (in the future) will allow them
 * to be run on a classic JVM.
 * 
 * @author crawley@jnode.org
 */
public abstract class AbstractCommand implements Command {

    private ArgumentBundle bundle;
    private CommandLine commandLine;
    private CommandIO[] ios;

    /**
     * A child class that uses this constructor won't have an argument
     * bundle (in the first instant); see getArgumentBundle.
     */
    public AbstractCommand() {
        this.bundle = null;
    }

    /**
     * A child class that uses this constructor will have an initially
     * empty argument; see getArgumentBundle.
     */
    public AbstractCommand(String description) {
        this.bundle = new ArgumentBundle(description);
    }

    @SuppressWarnings("deprecation")
    public final void execute(String[] args) throws Exception {
        // The following code is needed to deal with the new-style syntax mechanism.
        // This will have created an instance of the command class and bound the 
        // command-line arguments to it.  But the "static void main(...)" method that
        // presumably called us will have created another instance; i.e. this one.
        Command command = retrieveCurrentCommand();
        if (command != null) {
            // We'll ignore the instance created in the static void main method and
            // use the one that the invoker saved for us earlier.
        } else if (bundle != null) {
            // It appears that this class is designed to use the new-style syntax mechanism
            // but we've somehow been called without having a Command instance recorded.
            // We'll do our best to build a Command and parse the arguments based on the 
            // information we have to hand.  (We have no way of knowing what alias was
            // supplied by the user.)
            command = prepareCommand(args);
        } else {
            // The command does not use the new syntax mechanism, so we can just run
            // it and let it deal with the arguments itself.
            command = this;
        }
        CommandIO[] myIOs = new CommandIO[] {
            new CommandInput(System.in), 
            new CommandOutput(System.out), 
            new CommandOutput(System.err), 
        };
        command.initialize(new CommandLine(args), myIOs);
        command.execute();
    }

    /**
     * Attempt to create a command instance and parse the command line arguments.
     * 
     * @param args the command arguments
     * @return the command with arguments bound by the parser.
     * @throws Exception
     */
    private Command prepareCommand(String[] args) throws Exception {
        String className = this.getClass().getCanonicalName();
        String commandName = getProbableAlias(className);
        if (commandName == null) {
            commandName = className;
        }
        CommandLine cmdLine = new CommandLine(commandName, args);
        // This will be problematic in a classic JVM ...
        CommandInfo ci = cmdLine.parseCommandLine((CommandShell) ShellUtils.getCurrentShell());
        return ci.getCommandInstance();
    }
    
    /**
     * Get our best guess as to what the alias was.
     * 
     * @param canonicalName the class name
     * @return the intuited alias name
     * @throws NameNotFoundException
     */
    private String getProbableAlias(String canonicalName) throws NameNotFoundException {
        // This will be problematic in a classic JVM ...
        AliasManager mgr = ShellUtils.getAliasManager();
        for (String alias : mgr.aliases()) {
            try {
                if (mgr.getAliasClassName(alias).equals(canonicalName)) {
                    return alias;
                }
            } catch (NoSuchAliasException e) {
                // This can only occur if an alias is removed while we are working.
                // There's not much we can do about it ... so ignore this.
            }
        }
        return null;
    }

    /**
     * This method causes the command to 'exit' with the supplied return code.
     * This method will never return.  (The current implementation works by throwing
     * a subclass of 'java.lang.Error', so an application command must allow
     * 'Error' or 'Throwable' exceptions to propagate if it wants 'exit' to work.)
     * 
     * @param rc the return code.
     */
    protected void exit(int rc) {
        throw new VmExit(rc);
    }

    /**
     * Get the bundle comprising the command's registered Arguments.  If
     * this method returns <code>null</null>, it indicates to the command
     * shell that this command does not use the (new) command syntax parser.
     */
    public final ArgumentBundle getArgumentBundle() {
        return bundle;
    }

    /**
     * A child command class should call this method to register Arguments
     * for use by the Syntax system.
     * 
     * @param args Argument objects to be registered for use in command syntax
     * parsing and completion.
     */
    protected final void registerArguments(Argument<?> ... args) {
        if (bundle == null) {
            bundle = new ArgumentBundle();
        }
        for (Argument<?> arg : args) {
            bundle.addArgument(arg);
        }
    }
    
    /**
     * The default implementation of the 'execute()' entry point delegates to 
     * the older 'execute(...)' entry point.  A new command class should override
     * this method.
     */
    @Override
    public void execute() throws Exception {
        execute(commandLine, 
                ((CommandInput) ios[0]).getInputStream(),
                ((CommandOutput) ios[1]).getPrintStream(),
                ((CommandOutput) ios[2]).getPrintStream());
    }
    
    /**
     * The default implementation of the 'execute(...)' entry point complains that
     * you haven't implemented it.  A command class must override either this method
     * or (ideally) the 'execute()' entry point method.
     */
    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err)
        throws Exception {
        throw new UnsupportedOperationException(
                "A JNode command class MUST implement one of the 'execute(...)' methods.");
    }
    
    /**
     * Get the CommandLine object representing the command name and arguments
     * in String form.
     * 
     * @return the CommandLine object.
     */
    public final CommandLine getCommandLine() {
        return commandLine;
    }

    /**
     * Get the CommandIO object representing the 'error' stream; i.e. fd '2'.
     * 
     * @return the CommandIO for the command's error stream.
     */
    public final CommandOutput getError() {
        return (CommandOutput) ios[2];
    }

    /**
     * Get the CommandIO object representing the 'input' stream; i.e. fd '0'.
     * 
     * @return the CommandIO for the command's input stream.
     */
    public final CommandInput getInput() {
        return (CommandInput) ios[0];
    }
    
    /**
     * Get the Command's stream indexed by the 'fd' number.
     * 
     * @param fd a non-negative 'file descriptor' number.
     * @return the requested CommandIO object.
     */
    public final CommandIO getIO(int fd) {
        return ios[fd];
    }
    
    /**
     * Get the CommandIO object representing the 'output' stream; i.e. fd '1'.
     * 
     * @return the CommandIO for the command's output stream.
     */
    public final CommandOutput getOutput() {
        return (CommandOutput) ios[1];
    }

    @Override
    public final void initialize(CommandLine commandLine, CommandIO[] ios) {
        this.commandLine = commandLine;
        this.ios = ios;
    }


    static ThreadLocal<Command> currentCommand = new ThreadLocal<Command>();

    static void saveCurrentCommand(Command command) {
        currentCommand.set(command);
    }

    static Command retrieveCurrentCommand() {
        Command res = currentCommand.get();
        currentCommand.set(null);
        return res;
    }

}
