/*
JTestServer is a client/server framework for testing any JVM implementation.
 
Copyright (C) 2008  Fabien DUMINY (fduminy@jnode.org)

JTestServer is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

JTestServer is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package org.jtestserver.common.protocol;

import java.net.InetAddress;
import java.net.SocketAddress;


public abstract class Protocol<S> {
    public abstract Client<S, ? extends Protocol<S>> createClient(InetAddress serverIp, int serverPort) 
        throws ProtocolException;
    
    public abstract Server<S, ? extends Protocol<S>> createServer(int localPort) throws ProtocolException;
    
    protected abstract void sendMessage(S socket, String message, SocketAddress remoteAddress) 
        throws ProtocolException, TimeoutException;
    protected abstract ReceivedMessage receiveMessage(S socket) throws ProtocolException, TimeoutException;
}
