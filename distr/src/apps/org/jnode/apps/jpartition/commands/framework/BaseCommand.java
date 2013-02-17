/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.Context;

/** 
 * Abstract implementation of the {@link Command} interface.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public abstract class BaseCommand implements Command {
    private static final Logger log = Logger.getLogger(BaseCommand.class);

    private CommandStatus status = CommandStatus.NOT_RUNNING;
    private final String name;

    protected BaseCommand(String name) {
        this.name = name;
    }

    /**
     * Implementation of the command life cycle. 
     * The actual work of the command is delegated to the abstract {@link #doExecute()}
     * method.
     */
    public final void execute(CommandProcessor processor) throws CommandException {
        try {
            status = CommandStatus.RUNNING;
            processor.commandStarted(this);

            doExecute(processor.getContext());
            status = CommandStatus.SUCCESS;
        } catch (CommandException e) {
            log.error("command failed", e);
            status = CommandStatus.FAILED;
            throw e;
        } catch (Throwable t) {
            log.error("command failed", t);
            status = CommandStatus.FAILED;
            throw new CommandException("command failed", t);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final CommandStatus getStatus() {
        return status;
    }

    /**
     * Actually execute the command with the given context.
     * @param context The context to use.
     * @throws CommandException
     */
    protected abstract void doExecute(Context context) throws CommandException;

    /**
     * Get a printable representation of the command.
     * @return printable representation of the command.
     */
    @Override
    public String toString() {
        return status + " - " + name;
    }
}
