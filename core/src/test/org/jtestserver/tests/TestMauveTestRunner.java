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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import gnu.testlet.runner.RunResult;

import java.io.IOException;
import java.net.InetAddress;

import org.jtestserver.client.DefaultTestClient;
import org.jtestserver.client.TestClient;
import org.jtestserver.common.protocol.Client;
import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;
import org.jtestserver.common.protocol.udp.UDPProtocol;
import org.jtestserver.server.Config;
import org.jtestserver.server.TestFailureException;
import org.jtestserver.server.TestServer;
import org.jtestserver.server.commands.MauveTestRunner;
import org.jtestserver.server.commands.RunMauveTestCommand;
import org.junit.Test;

public class TestMauveTestRunner {
    //private static final String MAUVE_TEST = "gnu.testlet.java.io.BufferedInputStream.SimpleRead";
    private static final String MAUVE_TEST = "gnu.testlet.java.io.DataOutputStream.WriteRead";
    
    @Test    
    public void testRunTest() throws IOException {
        MauveTestRunner runner = MauveTestRunner.getInstance();
        try {
            RunResult result = runner.runTest(MAUVE_TEST);
            assertNotNull(result);
        } catch (TestFailureException e) {
            fail(e.getMessage());
        }
    }
    
    @Test
    public void testRunMauveTestCommand() throws ProtocolException, TimeoutException {
        final String xmlReport = new RunMauveTestCommand() {
            public String runTest(String test) throws ProtocolException, TimeoutException {
                return execute(new Object[]{test});
            }
        } .runTest(MAUVE_TEST);
        
        assertNotNull(xmlReport);
        
        RunResult result = new DefaultTestClient(null) {
            public RunResult parseReport() throws ProtocolException {
                return parseMauveReport(xmlReport);
            }
        } .parseReport();
        assertNotNull(result);
    }
    
    @Test
    public void testRunMauveThroughClient() throws ProtocolException, TimeoutException, IOException {
        Protocol<?> protocol = new UDPProtocol();
        int port = Config.read().getPort();
        Client<?, ?> client = null;
        TestServer server = null;
        
        try {            
            server = new TestServer();
            final TestServer s = server;
            new Thread() {
                public void run() {
                    s.start();
                }
            } .start();

            client = protocol.createClient(InetAddress.getLocalHost(), port);
            TestClient testClient = new DefaultTestClient(client);
            
            RunResult result = testClient.runMauveTest(MAUVE_TEST);
            assertNotNull(result);
        } finally {
            if (client != null) {
                client.close();
            }
            
            if (server != null) {
                server.requestShutdown();
            }
        }
    }
}
