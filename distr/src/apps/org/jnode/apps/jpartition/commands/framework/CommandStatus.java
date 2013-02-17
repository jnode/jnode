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

/**
 * Status for a command.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public enum CommandStatus {
    /**
     * The command is not running.
     */
    NOT_RUNNING("not running"),

    /**
     * The command is running.
     */
    RUNNING("running"),

    /**
     * The command has run and finished with success.
     */
    SUCCESS("success"),

    /**
     * The command has run and failed.
     */
    FAILED("failed");

    /**
     * printable name of the status.
     */
    private final String name;

    private CommandStatus(String name) {
        this.name = name;
    }

    /**
     * Get a printable representation of the command.
     * @return printable representation of the command.
     */
    public final String toString() {
        return name;
    }
}
