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
package org.jtestserver.client.process;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.client.Config;
import org.jtestserver.common.protocol.ProtocolException;

public class ServerProcessManager {
    private static final Logger LOGGER = Logger.getLogger(ServerProcessManager.class.getName());
    
//    private final TestClient client;
    private final List<ServerProcess> processes;
    private final WatchDog watchDog;
    
    public ServerProcessManager(Config config) {
//        this.client = new DefaultTestClient(client);
        watchDog = new WatchDog(config);
        processes = new ArrayList<ServerProcess>();
        
        ServerProcess process;
        for (int i = 0; i < 10; i++) { // TODO read number of processes from config
            process = config.getVMConfig().createServerProcess();
            processes.add(process);
        }
    }
    
    public void startAll() throws IOException, ProtocolException {
        watchDog.startWatching();
        
        for (ServerProcess process : processes) {
            process.start();
            watchDog.watch(process);
        }
    }

    public void stopAll() throws IOException, ProtocolException {
        for (ServerProcess process : processes) {
            // stop the watch dog before actually stop the process
            watchDog.unwatch(process);
    
            LOGGER.info("killing running servers");        
//        boolean killed = false;
//        try {
//            // kill server that might still be running
//            client.close();
//        } catch (ProtocolException pe) {
//            // assume that exception means the server has been killed
//            killed = true;
//        }
//        
//        while (!killed) {
//            try {
//                client.getStatus();
//            } catch (ProtocolException pe) {
//                // assume that exception means the server has been killed
//                killed = true;
//            } catch (TimeoutException e) {
//                // assume that exception means the server has been killed
//                killed = true;
//            }
//        }
        
            LOGGER.info("all servers are killed");
    
            try {
                process.stop();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "an error happened while stopping", e);
            }
        }
    }
}
