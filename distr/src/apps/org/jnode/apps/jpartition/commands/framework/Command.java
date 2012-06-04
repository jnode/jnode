/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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

/**
 * Interface for a command.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public interface Command {
    /**
     * Query the given #@link {@link CommandProcessor} to execute this command. 
     * @param processor the command processor to use
     * @throws CommandException
     */
    public void execute(CommandProcessor processor) throws CommandException;

    /**
     * Get the actual status of the command.
     * @return actual status of the command.
     */
    public CommandStatus getStatus();
}
