package org.jnode.apps.jpartition.commands.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.ErrorReporter;

public class CommandProcessor {
    private static final Logger log = Logger.getLogger(CommandProcessor.class);

    private final ErrorReporter errorReporter;

    private Stack<Command> commands = new Stack<Command>();
    private List<CommandProcessorListener> listeners = new ArrayList<CommandProcessorListener>();

    private boolean running = false;

    public CommandProcessor(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    public List<Command> getPendingCommands() {
        return new ArrayList<Command>(commands);
    }

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
            errorReporter.reportError(log, this, t);
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

    public void commandStarted(Command command) {
        for (CommandProcessorListener l : listeners) {
            l.commandStarted(this, command);
        }
    }

    public void addListener(CommandProcessorListener listener) {
        listeners.add(listener);
    }

    public void removeListener(CommandProcessorListener listener) {
        listeners.remove(listener);
    }
}
