/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.xml.internal.ws.developer;

/**
 * Represents the exception that has occurred on the server side.
 *
 * <p>
 * When an exception occurs on the server, JAX-WS RI sends the stack
 * trace of that exception to the client. On the client side,
 * instances of this class are used to represent such stack trace.
 *
 * @author Kohsuke Kawaguchi
 * @since 2.1
 */
public class ServerSideException extends Exception {
    private final String className;

    public ServerSideException(String className, String message) {
        super(message);
        this.className = className;
    }

    public String toString() {
        String s = className;
        String message = getLocalizedMessage();
        return (message != null) ? (s + ": " + message) : s;
    }
}
