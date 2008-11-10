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
package org.jtestserver.server;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.common.message.InputMessage;
import org.jtestserver.common.message.OutputMessage;
import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;
import org.jtestserver.common.protocol.UDPProtocol;
import org.jtestserver.server.commands.GetStatusCommand;
import org.jtestserver.server.commands.MauveTestRunner;
import org.jtestserver.server.commands.RunMauveTestCommand;
import org.jtestserver.server.commands.ShutdownCommand;

public class TestServer {
    private static final Logger LOGGER = Logger.getLogger(TestServer.class.getName());
        
    public static void main(String[] args) throws ProtocolException {
        UDPProtocol protocol = UDPProtocol.createServer();
        //protocol.setTimeout(10000);
        
        TestServer server = new TestServer(protocol);
        server.start();
    }
    
    private boolean shutdownRequested = false;
    private final Protocol protocol;
    private final Map<String, TestServerCommand> nameToCommand;
    
    public TestServer(Protocol protocol) {
        this.protocol = protocol;
        nameToCommand = new HashMap<String, TestServerCommand>();
        
        addCommand(new RunMauveTestCommand());
        addCommand(new ShutdownCommand(this));
        addCommand(new GetStatusCommand());
    }
    
    private void addCommand(TestServerCommand command) {
        nameToCommand.put(command.getName(), command);
    }
    
    public void start() {
        while (!shutdownRequested) {
            try {
                InputMessage input = InputMessage.create(protocol);
                String commandName = input.getString();
                TestServerCommand command = nameToCommand.get(commandName);
                
                OutputMessage output = null;
                if (command == null) {
                    //TODO
                } else {
                    try {
                        output = command.execute(input);
                    } catch (Throwable t) {
                        LOGGER.log(Level.SEVERE, "error in command", t);
                    }
                }
                
                // if the command returns a result
                if (output != null) {
                    output.sendWith(protocol);
                }
                
            } catch (ProtocolException pe) {
                LOGGER.log(Level.SEVERE, "protocol error", pe);
            } catch (TimeoutException te) {
                // ignore
            } catch (Throwable t) {
                LOGGER.log(Level.SEVERE, "unexpected error", t);
            }
        }

        shutdown();
    }
    
    private void shutdown() {
        try {
            protocol.close();
        } catch (ProtocolException e) {
            LOGGER.log(Level.SEVERE, "error in shutdown", e);
        }
        MauveTestRunner.getInstance().shutdown();
        LOGGER.info("Server has shutdown");
    }
    
    public void requestShutdown() {
        shutdownRequested = true;
        LOGGER.info("shutdown requested");
    }
}
