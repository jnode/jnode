/*
 * Copyright 2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package com.sun.corba.se.impl.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.ServerSocket;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

import com.sun.corba.se.pept.transport.Acceptor;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.ORBSocketFactory;

import com.sun.corba.se.impl.orbutil.ORBConstants;

public class DefaultSocketFactoryImpl
    implements ORBSocketFactory
{
    private ORB orb;

    public void setORB(ORB orb)
    {
	this.orb = orb;
    }

    public ServerSocket createServerSocket(String type, 
					   InetSocketAddress inetSocketAddress)
        throws IOException
    {
	ServerSocketChannel serverSocketChannel = null;
	ServerSocket serverSocket = null;

	if (orb.getORBData().acceptorSocketType().equals(ORBConstants.SOCKETCHANNEL)) {
	    serverSocketChannel = ServerSocketChannel.open();
	    serverSocket = serverSocketChannel.socket();
	} else {
	    serverSocket = new ServerSocket();
	}
	serverSocket.bind(inetSocketAddress);
	return serverSocket;
    }

    public Socket createSocket(String type, 
			       InetSocketAddress inetSocketAddress)
        throws IOException
    {
	SocketChannel socketChannel = null;
	Socket socket = null;

	if (orb.getORBData().connectionSocketType().equals(ORBConstants.SOCKETCHANNEL)) {
	    socketChannel = SocketChannel.open(inetSocketAddress);
	    socket = socketChannel.socket();
	} else {
	    socket = new Socket(inetSocketAddress.getHostName(),
				inetSocketAddress.getPort());
	}

	// Disable Nagle's algorithm (i.e., always send immediately).
	socket.setTcpNoDelay(true);

	return socket;
    }

    public void setAcceptedSocketOptions(Acceptor acceptor,
					 ServerSocket serverSocket,
					 Socket socket)
	throws SocketException
    {
	// Disable Nagle's algorithm (i.e., always send immediately).
	socket.setTcpNoDelay(true);
    }
}

// End of file.
