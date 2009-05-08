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
package org.jtestserver.tests;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jtestserver.common.protocol.Client;
import org.jtestserver.common.protocol.MessageProcessor;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.Server;
import org.jtestserver.common.protocol.TimeoutException;

class TestUtils {
    static final int PORT = 11000; // use a different port than default one
    static final InetAddress IP;
    static final InetAddress UNKNOWN_IP;
    
    static {
        try {
            IP = InetAddress.getLocalHost();
            UNKNOWN_IP = InetAddress.getByAddress(new byte[]{123, 123, 123, 123});
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    static void sendReceive(Client<?, ?> client, String message, Server<?, ?> server, String response) 
        throws Throwable {
        sendReceive(client, message, server, 0, response); // by default, no server delay (0)
    }

    static synchronized void sendReceive(Client<?, ?> client, String message, Server<?, ?> server,
            int serverDelay, String response)
        throws Throwable {
        
        boolean needResponse  = (response != MessageProcessor.NO_RESPONSE);
        
        ServerThread serverThread = new ServerThread(server, serverDelay, response);
        serverThread.start();
        
        ClientThread clientThread = new ClientThread(client, message, needResponse);
        clientThread.start();

        while (clientThread.isAlive() || serverThread.isAlive()) {
            Thread.sleep(1000);
        }
        
//        if (serverThread.hasError()) {
//            server.close();
//        }
//        if (clientThread.hasError()) {
//            client.close();
//        }
        
        assertEquals(message, serverThread.getMessage());
        assertEquals(response, clientThread.getResponse());
    }
    
    private static class ServerThread extends Thread implements MessageProcessor {
        private final Server<?, ?> server;
        private final int serverDelay;
        private final String response;
        
        private String message = null;
        private Throwable t = null;
        
        public ServerThread(Server<?, ?> server, int serverDelay, String response) {
            super("ServerThread");
            this.server = server;
            this.serverDelay = serverDelay;
            this.response = response;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {      
//            if (serverDelay > 0) {
//                try {
//                    Thread.sleep(serverDelay);
//                } catch (InterruptedException e) {
//                    // ignore
//                }
//            }
            
            try {
                server.receive(this);
            } catch (Throwable t) {
                this.t = t;
            }
        }
        
        public boolean hasError() {
            return (t != null);
        }
        
        /* (non-Javadoc)
         * @see org.jtestserver.common.protocol.MessageProcessor#process(java.lang.String)
         */
        @Override
        public String process(String message) {
            this.message = message;
            return response; // might be MessageProcessor.NO_REPLY
        }
        
        public String getMessage() throws Throwable {
            if (t != null) {
                throw t;
            }
            return message;
        }
    }
    
    private static class ClientThread extends Thread {
        private final Client<?, ?> client;
        private final boolean needResponse;
        private final String message;
        
        private boolean responseReceived = false;
        private String response = null;
        private Throwable t = null;
        
        public ClientThread(Client<?, ?> client, String message, boolean needResponse) {
            super("ClientThread");
            this.client = client;
            this.needResponse = needResponse;
            this.message = message;
        }
        
        public boolean hasError() {
            return (t != null);
        }

        /* (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {      
            try {
                responseReceived = false;
                
                response = client.send(message, needResponse);
                responseReceived = true;
            } catch (Throwable  t) {
                this.t = t;
            }
        }
        
        public String getResponse() throws Throwable {
            if (t != null) {
                throw t;
            }
            if (needResponse && !responseReceived) {
                throw new RuntimeException("response not received");
            }
            return response;
        }
    }
}
