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
 
package org.jnode.apps.jpartition.model;

/**
 * Represents an exception happening while trying to do something on a {@link Device}.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class DeviceException extends RuntimeException {

    private static final long serialVersionUID = -6289552400638465023L;

    /**
     * @see RuntimeException#RuntimeException()
     */
    public DeviceException() {
        super();
    }

    /**
     * 
     * @param message
     * @param cause
     * @see RuntimeException#RuntimeException(String, Throwable)
     */
    public DeviceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 
     * @param message
     * @see RuntimeException#RuntimeException(String)
     */
    public DeviceException(String message) {
        super(message);
    }

    /**
     * 
     * @param cause
     * @see RuntimeException#RuntimeException(Throwable)
     */
    public DeviceException(Throwable cause) {
        super(cause);
    }

}
