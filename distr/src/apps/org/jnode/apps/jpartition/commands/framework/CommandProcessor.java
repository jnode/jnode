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
 
package org.jnode.apps.jpartition.commands.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.Context;

/**
 * Command processor which handle a stack of commands to execute.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class CommandProcessor {
    private static final Logger log = Logger.getLogger(CommandProcessor.class);

    /**
     * The context to use.
     */
    private final Context context;

    /**
     * The stack of commands.
     */
    private Stack<Command> commands = new Stack<Command>();
    
    /**
     * The listeners of this command processor.
     */
    private List<CommandProcessorListener> listeners = new ArrayList<CommandProcessorListener>();

    /**
     * Are we actually executing the commands in our stack ? 
     */
    private boolean running = false;

    /**
     * Cosntructor.
     * @param context The context to use.
     */
    public CommandProcessor(Context context) {
        this.context = context;
    }

    /**
     * Get the pending commands.
     * @return The pending commands.
     */
    public List<Command> getPendingCommands() {
        return new ArrayList<Command>(commands);
    }

    /**
     * Main loop processing all commands in the stack.
     */
    public synchronized void process() {
        try {
            if (!running) {
                running = true;

                boolean quit = false;
                while (!commands.isEmpty() && !quit) {
                    quit = processCommand();
                }

                running = false;
            }
        } catch (Throwable t) {
            context.getErrorReporter().reportError(log, this, t);
        }
    }

    private boolean processCommand() {
        boolean quit = false;
        Command command = null;
        try {
            command = peekCommand();

            command.execute(this);
        } catch (CommandException e) {
            log.error("error in command processing", e);
            quit = true;
        } catch (Throwable t) {
            log.error("unexpected error in command processing", t);
            quit = true;
        } finally {
            if (command != null) {
                for (CommandProcessorListener l : listeners) {
                    l.commandFinished(this, command);
                }

                try {
                    removeCommand();
                } catch (Throwable t) {
                    log.error("error in removeCommand", t);
                }
            }
        }

        return quit;
    }

    /**
     * Add a command to the stack of pending commands. 
     * @param command The command to add.
     */
    public void addCommand(Command command) {
        if (command.getStatus() != CommandStatus.NOT_RUNNING) {
            throw new IllegalArgumentException("command must be in status NOT_RUNNING");
        }

        commands.push(command);

        for (CommandProcessorListener l : listeners) {
            l.commandAdded(this, command);
        }
    }

    protected Command peekCommand() throws Exception {
        Command command = commands.peek();
        if (command.getStatus() != CommandStatus.NOT_RUNNING) {
            throw new Exception("command already started : " + command);
        }
        return command;
    }

    protected void removeCommand() {
        Command command = commands.pop();

        for (CommandProcessorListener l : listeners) {
            l.commandRemoved(this, command);
        }
    }

    /**
     * Callback method used to notify the command processor that a command 
     * has started to execute.
     * @param command The command that has started.
     */
    public void commandStarted(Command command) {
        for (CommandProcessorListener l : listeners) {
            l.commandStarted(this, command);
        }
    }

    /**
     * Add a listener of command processor events.
     * @param listener The listener to add.
     */
    public void addListener(CommandProcessorListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener of command processor events.
     * @param listener The listener to remove.
     */
    public void removeListener(CommandProcessorListener listener) {
        listeners.remove(listener);
    }

    /**
     * @return
     */
    public Context getContext() {
        return context;
    }
}
