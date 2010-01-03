/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 * Class of exception thrown while a {@link Command} is running when and error happens.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class CommandException extends Exception {
    private static final long serialVersionUID = -8340890789850970389L;

    /**
     * Constructor. 
     * @param message
     * @param cause root cause of the exception.
     */
    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * @param message
     */
    public CommandException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param cause root cause of the exception.
     */
    public CommandException(Throwable cause) {
        super(cause);
    }

}
