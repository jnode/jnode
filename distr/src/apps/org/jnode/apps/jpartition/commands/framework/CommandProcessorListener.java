/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 * Listener interface for {@link CommandProcessor} events.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public interface CommandProcessorListener {
    /**
     * Method called when a {@link Command} has been added to a {@link CommandProcessor}. 
     * @param processor the command processor
     * @param command the command
     */
    void commandAdded(CommandProcessor processor, Command command);

    /**
     * Method called when a {@link Command} has been removed from a {@link CommandProcessor}. 
     * @param processor the command processor
     * @param command the command
     */
    void commandRemoved(CommandProcessor processor, Command command);

    /**
     * Method called when a {@link Command} has started. 
     * @param processor the command processor
     * @param command the command
     */
    void commandStarted(CommandProcessor processor, Command command);

    /**
     * Method called when a {@link Command} has finished. 
     * @param processor the command processor
     * @param command the command
     */
    void commandFinished(CommandProcessor processor, Command command);
}
