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

/**
 * A CommandInfo object is a descriptor used by the CommandShell and CommandInvokers
 * to hold information about a command that is being prepared for execution.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author crawley@jnode.org
 */
public final class CommandInfo {

    private final Class<?> clazz;

    private final boolean internal;

    private Command instance;

    public CommandInfo(Class<?> clazz, boolean internal) {
        this.clazz = clazz;
        this.internal = internal;
    }

    public final Class<?> getCommandClass() {
        return clazz;
    }

    public final boolean isInternal() {
        return internal;
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
     * @return The Command instance to be used for binding argument and executing 
     * the command, or <code>null</code>.
     */
    public Command getCommandInstance() {
        return instance;
    }
}
