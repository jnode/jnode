/*

JTestServer is a client/server framework for testing any JVM implementation.
 
Copyright (C) 2009  Fabien DUMINY (fduminy@jnode.org)

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

import gnu.testlet.runner.RunResult;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.client.process.ServerProcess;
import org.jtestserver.client.utils.WatchDog;
import org.jtestserver.common.protocol.Client;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;

public class TestDriverInstance {
    private static final Logger LOGGER = Logger.getLogger(TestDriverInstance.class.getName());
    
    private final TestClient client;
    private final ServerProcess process;
    private final WatchDog watchDog;
    
    public TestDriverInstance(Config config, Client<?, ?> client, ServerProcess process) {
        this.client = new DefaultTestClient(client);
        this.process = process;
        watchDog = new WatchDog(process, config) {

            @Override
            protected void processDead() {
                LOGGER.warning("process is dead. restarting it.");
                try {
                    TestDriverInstance.this.process.start();
                } catch (IOException e) {
                    LOGGER.log(Level.SEVERE, "error while restarting", e);
                }
            }
        };
    }
    
    public void startInstance() throws IOException, ProtocolException {
        process.start();
        watchDog.startWatching();
    }

    public void stopInstance() throws IOException, ProtocolException {
        // stop the watch dog before actually stop the process
        watchDog.stopWatching();

        LOGGER.info("killing running servers");        
        boolean killed = false;
        try {
            // kill server that might still be running
            client.close();
        } catch (ProtocolException pe) {
            // assume that exception means the server has been killed
            killed = true;
        }
        
        while (!killed) {
            try {
                client.getStatus();
            } catch (ProtocolException pe) {
                // assume that exception means the server has been killed
                killed = true;
            } catch (TimeoutException e) {
                // assume that exception means the server has been killed
                killed = true;
            }
        }
        
        LOGGER.info("all servers are killed");

        try {
            process.stop();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "an error happened while stopping", e);
        }
    }

    public RunResult runTest(String test) throws ProtocolException, IOException {
        
        RunResult delta = null;
        LOGGER.info("running test " + test);
        
        try {
            delta = client.runMauveTest(test);
        } catch (TimeoutException e) {
            LOGGER.log(Level.SEVERE, "a timeout happened", e);
        }
        
        return delta;
    }    
}
