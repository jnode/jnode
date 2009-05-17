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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.common.message.InputMessage;
import org.jtestserver.common.message.OutputMessage;
import org.jtestserver.common.protocol.MessageProcessor;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.Server;
import org.jtestserver.common.protocol.TimeoutException;
import org.jtestserver.common.protocol.udp.UDPProtocol;
import org.jtestserver.server.commands.GetStatusCommand;
import org.jtestserver.server.commands.MauveTestRunner;
import org.jtestserver.server.commands.RunMauveTestCommand;
import org.jtestserver.server.commands.ShutdownCommand;

public class TestServer {
    private static final Logger LOGGER = Logger.getLogger(TestServer.class.getName());
        
    public static void main(String[] args) {
        try {
            TestServer server = new TestServer();
            server.start();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "unable to read config", e);
            System.exit(1);
        } catch (ProtocolException e) {
            LOGGER.log(Level.SEVERE, "unable to create server", e);
            System.exit(2);
        }
    }
    
    private boolean shutdownRequested = false;
    private final Server<?, ?> server;
    private final Map<String, TestServerCommand> nameToCommand;
    private final Config config;
    
    public TestServer() throws IOException, ProtocolException {
        nameToCommand = new HashMap<String, TestServerCommand>();
        
        addCommand(new RunMauveTestCommand());
        addCommand(new ShutdownCommand(this));
        addCommand(new GetStatusCommand());
        
        config = Config.read();
        server = new UDPProtocol().createServer(config.getPort());
        //protocol.setTimeout(10000);
        
        MauveTestRunner.getInstance().setConfig(config);
    }
    
    private void addCommand(TestServerCommand command) {
        nameToCommand.put(command.getName(), command);
    }
    
    public void start() {
        LOGGER.info("server started");
        
        while (!shutdownRequested) {
            try {
                server.receive(new MessageProcessor() {
                    /* (non-Javadoc)
                     * @see org.jtestserver.common.protocol.MessageProcessor#process(java.lang.String)
                     */
                    @Override
                    public String process(String message) {
                        InputMessage input = InputMessage.create(message);
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
                        String result = MessageProcessor.NO_RESPONSE;
                        if (output != null) {
                            result = output.toMessage();
                        }
                        
                        return result;
                    }
                });
                
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
        server.close();
        LOGGER.info("Server has shutdown");
    }
    
    public void requestShutdown() {
        shutdownRequested = true;
        LOGGER.info("shutdown requested");
    }
}
