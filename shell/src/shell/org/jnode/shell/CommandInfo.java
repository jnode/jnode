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

import org.jnode.shell.syntax.ArgumentBundle;
import org.jnode.shell.syntax.SyntaxBundle;

/**
 * A CommandInfo object is a descriptor used by the CommandShell and CommandInvokers
 * to hold information about a command that is being prepared for execution.
 *
 * There are two basic types of command's that can be described. The first type is
 * a JNode command that implements {@link Command} by extending {@link AbstractCommand}.
 * The other type is generally refered to as a 'classic Java' command, and refers to
 * command that is executed via its {@code main} method. This type of command may have
 * an associated bare command definition in its descriptor. If such a descriptor was
 * supplied, an instance of this class will contain the {@link org.jnode.shell.syntax.ArgumentBundle}
 * for the command.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 * @author chris boertien
 */
public final class CommandInfo {
    
    private final Class<?> clazz;
    private final String commandName;
    private final SyntaxBundle syntaxBundle;
    private final ArgumentBundle argBundle;
    private final boolean internal;

    private Command instance;
    
    /**
     * Creates a CommandInfo object for a JNode command.
     *
     * @param clazz the designated {@code Class} for executing the command
     * @param commandName the name, or alias, for the command
     * @param syntaxBundle the syntax definition to parse the command line against
     */
    public CommandInfo(Class<?> clazz, String commandName, SyntaxBundle syntaxBundle, boolean internal) {
        this.clazz = clazz;
        this.internal = internal;
        this.commandName = commandName;
        this.syntaxBundle = syntaxBundle;
        this.argBundle = null;
    }
    
    /**
     * Creates a CommandInfo object for a bare command.
     *
     * If the bare command has a defined {@code ArgumentBundle} then
     * it will be used when parsing the command line.
     *
     * @param clazz the designated {@code Class} for executing the command
     * @param commandName the name, or alias, for the command
     * @param syntaxBundle the syntax definition to parse the command line against
     * @param argBundle the optional {@code ArgumentBundle} to parse the command line against
     */
    public CommandInfo(Class<?> clazz, String commandName, SyntaxBundle syntaxBundle, ArgumentBundle argBundle) {
        this.clazz = clazz;
        this.internal = false;
        this.commandName = commandName;
        this.syntaxBundle = syntaxBundle;
        this.argBundle = argBundle;
    }
    
    /**
     * Gets the {@code Class} for the command.
     *
     * @return the {@code Class} for the command
     */
    public final Class<?> getCommandClass() {
        return clazz;
    }
    
    /**
     * Checks whether this command is considered internal or not.
     *
     * @return true if this is an internal command
     * @see org.jnode.shell.alias.AliasManager#isInternal
     */
    public final boolean isInternal() {
        return internal;
    }
    
    /**
     * Retrieves the argument bundle for the command.
     *
     * If this instance was instantiated with an {@link ArgumentBundle}, then
     * that bundle will be returned. If not, and the target class is a type of
     * {@link Command} then the {@code Command}s {@code ArgumentBundle} will
     * be returned. Otherwise this method will return null.
     *
     * @return an {@code ArgumentBundle} for the command, or null if none can be found
     * @see #CommandInfo(Class,String,SyntaxBundle,ArgumentBundle)
     */
    public final ArgumentBundle getArgumentBundle() {
        if (argBundle == null) {
            if (Command.class.isAssignableFrom(clazz)) {
                try {
                    return ((Command) clazz.newInstance()).getArgumentBundle();
                } catch (Exception e) {
                    // fall through
                }
            }
        }
        return argBundle;
    }

    /**
     * Get the Command instance for this CommandInfo, instantiating it if necessary.
     * 
     * @return The Command instance to be used for binding argument and executing the command.
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public final Command createCommandInstance() throws InstantiationException, IllegalAccessException {
        if (instance == null) {
            if (Command.class.isAssignableFrom(clazz)) {
                instance = (Command) (clazz.newInstance());
            }
        }
        return instance;
    }

    /**
     * Get the Command instance for this CommandInfo, without instantiating one.
     *
     * @return The Command instance to be used for binding argument and executing 
     * the command, or <code>null</code>.
     */
    public Command getCommandInstance() {
        return instance;
    }
    
    /**
     * Gets the name/alias for this command.
     *
     * @return the command's alias
     */
    public String getCommandName() {
        return commandName;
    }
    
    /**
     * Perform command line argument parsing in preparation to invoking a command.
     * This locates the command's class and a suitable command line syntax, then
     * parses against the Syntax, binding the command arguments to Argument objects
     * in an ArgumentBundle object obtained from the Command object.
     *
     * If this CommandInfo object was created from a bare command, then the argBundle
     * will have been previously supplied to parse against.
     *
     * @param cmdLine the command line containing the tokens to parse against the argument bundle.
     * @throws ShellException for problems instantiating the command class, or problems parsing
     *         the command arguments.
     */
    public void parseCommandLine(CommandLine cmdLine) throws ShellException {
        try {
            ArgumentBundle bundle = argBundle;
            if (bundle == null) {
                Command cmd = createCommandInstance();
                if (cmd != null) {
                    bundle = cmd.getArgumentBundle();
                }
            }
            
            if (bundle != null) {
                // Do a full parse to bind the command line argument tokens to corresponding
                // command arguments
                bundle.parse(cmdLine, syntaxBundle);
            }
        } catch (InstantiationException ex) {
            throw new ShellException("Command class cannot be instantiated", ex);
        } catch (IllegalAccessException ex) {
            throw new ShellException("Command class cannot be instantiated", ex);
        }
    }
}
