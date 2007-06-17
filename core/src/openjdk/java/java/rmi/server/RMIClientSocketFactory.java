/*
 * Copyright 1998-2001 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.rmi.server;

import java.io.*;
import java.net.*;

/**
 * An <code>RMIClientSocketFactory</code> instance is used by the RMI runtime
 * in order to obtain client sockets for RMI calls.  A remote object can be
 * associated with an <code>RMIClientSocketFactory</code> when it is
 * created/exported via the constructors or <code>exportObject</code> methods
 * of <code>java.rmi.server.UnicastRemoteObject</code> and
 * <code>java.rmi.activation.Activatable</code> .
 *
 * <p>An <code>RMIClientSocketFactory</code> instance associated with a remote
 * object will be downloaded to clients when the remote object's reference is
 * transmitted in an RMI call.  This <code>RMIClientSocketFactory</code> will
 * be used to create connections to the remote object for remote method calls.
 *
 * <p>An <code>RMIClientSocketFactory</code> instance can also be associated
 * with a remote object registry so that clients can use custom socket
 * communication with a remote object registry.
 *
 * <p>An implementation of this interface should be serializable and
 * should implement {@link Object#equals} to return <code>true</code> when
 * passed an instance that represents the same (functionally equivalent)
 * client socket factory, and <code>false</code> otherwise (and it should also
 * implement {@link Object#hashCode} consistently with its
 * <code>Object.equals</code> implementation).
 *
 * @version 1.18, 05/05/07
 * @author  Ann Wollrath
 * @author  Peter Jones
 * @since   1.2
 * @see     java.rmi.server.UnicastRemoteObject
 * @see     java.rmi.activation.Activatable
 * @see     java.rmi.registry.LocateRegistry
 */
public interface RMIClientSocketFactory {

    /**
     * Create a client socket connected to the specified host and port.
     * @param  host   the host name
     * @param  port   the port number
     * @return a socket connected to the specified host and port.
     * @exception IOException if an I/O error occurs during socket creation
     * @since 1.2
     */
    public Socket createSocket(String host, int port)
	throws IOException;
}
