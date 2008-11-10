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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.common.Status;
import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;
import org.jtestserver.common.protocol.UDPProtocol;

public class TestDriver {
    private static final Logger LOGGER = Logger.getLogger(TestDriver.class.getName());
    
    public static void main(String[] args) throws ProtocolException, IOException {
        TestDriver testDriver = createUDPTestDriver(InetAddress.getByName("localhost"));
        
        if ((args.length > 0) && "kill".equals(args[0])) {
            testDriver.killRunningServers();
        } else {
            testDriver.start();
        }
    }
    
    private static TestDriver createUDPTestDriver(InetAddress serverAddress) throws ProtocolException {
        UDPProtocol protocol = UDPProtocol.createClient(serverAddress);
        protocol.setTimeout(10000);
        
        return new TestDriver(protocol, new NewProcessLauncher());
    }
    
    private final TestClient client;
    private final TestServerProcess serverProcess;
    private final List<String> tests = new ArrayList<String>();
    
    private TestDriver(Protocol protocol, TestServerLauncher launcher) {
        this.client = new DefaultTestClient(protocol);
        this.serverProcess = new TestServerProcess(launcher);
    }
    
    public void killRunningServers() throws ProtocolException {
        try {
            // kill server that might still be running
            client.shutdown();
        } catch (Throwable t) {
            LOGGER.log(Level.SEVERE, "unexpected error", t);
        }
        
        boolean killed = false;
        while (!killed) {
            try {
                client.getStatus();
            } catch (TimeoutException e) {
                LOGGER.log(Level.SEVERE, "a timeout happened", e);
                killed = true;
            }
        }        
    }
    
    public void start() throws IOException, ProtocolException {
        //killRunningServers();
        
        //serverProcess.start();
        
        readTests();
        
        for (String test : tests) {
            try {
                Status status = client.runMauveTest(test); 
                LOGGER.info(((status == null) ? "null" : status.toString()) + ": " + test);
            } catch (TimeoutException e) {
                LOGGER.log(Level.SEVERE, "a timeout happened", e);
            }
        }
        
        //killRunningServers();
    }
    
    private void readTests() throws IOException {
        if (tests.isEmpty()) {
            InputStream in = TestDriver.class.getResourceAsStream("/org/jnode/test/jtestserver/tests/mauve-tests.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String test = null;
            while ((test = reader.readLine()) != null) {
                if (!test.startsWith("#")) {
                    tests.add(test);
                }
            }
            LOGGER.info("" + tests.size() + " tests");
        }        
    }
    
}
