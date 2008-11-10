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
package org.jtestserver.client;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.common.Status;
import org.jtestserver.common.message.Descriptors;
import org.jtestserver.common.message.Message;
import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;

public class DefaultTestClient implements TestClient {
    private static final Logger LOGGER = Logger.getLogger(DefaultTestClient.class.getName());
        
    private final Protocol protocol;

    public DefaultTestClient(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public Status runMauveTest(String test) throws ProtocolException, TimeoutException {
        return (Status) Message.send(protocol, Descriptors.RUN_MAUVE_TEST, test);
    }
    
    @Override
    public Status getStatus() throws ProtocolException, TimeoutException {
        return (Status) Message.send(protocol, Descriptors.GET_STATUS);
    }
    
    @Override
    public void shutdown() throws ProtocolException, TimeoutException {
        Message.send(protocol, Descriptors.SHUTDOWN);
    }
    
    @Override
    public void close() throws ProtocolException {
        try {
            shutdown();
        } catch (TimeoutException e) {
            LOGGER.log(Level.SEVERE, "error in close", e);            
        }
        protocol.close();
    }
}
