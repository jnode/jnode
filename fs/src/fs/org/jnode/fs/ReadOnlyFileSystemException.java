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
 
package org.jnode.fs;

import java.io.IOException;

/**
 * New exception allowing to handle cases where a FileSystem is mounted readOnly
 * 
 * @author Fabien DUMINY
 * 
 * 
 */
@SuppressWarnings("serial")
public class ReadOnlyFileSystemException extends IOException {
    /**
     * Constructs a {@code ReadOnlyFileSystemException} with
     * a default error message.
     */
    public ReadOnlyFileSystemException() {
        super("The file system is flagged as read-only. No modifications allowed.");
    }
    
    /**
     * Constructs a {@code ReadOnlyFileSystemException} with
     * the specified cause and message.
     * 
     * @param message the detail message of the exception.
     * @param cause the cause of the exception.
     */
    public ReadOnlyFileSystemException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    /**
     * Constructs a {@code ReadOnlyFileSystemException} with
     * the specified cause.
     * 
     * @param cause the cause of the exception.
     */
    public ReadOnlyFileSystemException(Throwable cause) {
        super();
        initCause(cause);
    }

    /**
     * Constructs a {@code ReadOnlyFileSystemException} with
     * the specified message.
     * 
     * @param message the detail message of the exception. 
     */
    public ReadOnlyFileSystemException(String message) {
        super(message);
    }
}
