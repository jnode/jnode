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
 
package org.jnode.driver.net;

import java.net.SocketException;

/**
 * @author epr
 */
public class NetworkException extends SocketException {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public NetworkException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public NetworkException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    /**
     * @param cause
     */
    public NetworkException(Throwable cause) {
        super();
        initCause(cause);
    }

    /**
     * @param s
     */
    public NetworkException(String s) {
        super(s);
    }
}
