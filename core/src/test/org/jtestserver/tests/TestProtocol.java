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

import static org.jtestserver.tests.TestUtils.IP;
import static org.jtestserver.tests.TestUtils.PORT;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.jtestserver.common.protocol.Client;
import org.jtestserver.common.protocol.Protocol;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.Server;
import org.jtestserver.common.protocol.TimeoutException;
import org.jtestserver.common.protocol.udp.UDPProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TestProtocol {
    @Parameters
    public static List<Object[]> getProtocols() throws ProtocolException {
        return Arrays.asList(new Object[][] {{new UDPProtocol()}});
    }
    
    private final Protocol<?> protocol;
    private Server<?, ?> server;
    private Client<?, ?> client;
    
    public TestProtocol(Protocol<?> protocol) throws ProtocolException {
        this.protocol = protocol;
    }

    @Before
    public void setupDown() throws ProtocolException {
        client = protocol.createClient(IP, PORT);
        server = protocol.createServer(PORT);
        
        client.setTimeout(1000);
        server.setTimeout(1000);
    }
    
    @After
    public void tearDown() {
        client.close();
        server.close();
    }
    
    @Test
    public void testSendReceive() throws Throwable {
        sendReceive("A Message", "A response");
    }
    
    @Test
    public void testSendReceiveBlank() throws Throwable {
        sendReceive("  ", " ");
    }
    
    @Test
    public void testSendReceiveEmpty() throws Throwable {
        sendReceive("", "");
    }
    
    @Test(expected = NullPointerException.class)
    public void testSendNull() throws Throwable {
        client.send(null, false);        
    }

    
    @Test
    public void testSendLongMessage() throws Throwable {
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 65000; i++) {
            longMessage.append(' ');
        }
        sendReceive(longMessage.toString(), longMessage.toString());
    }
    
    @Test
    public void testMultiThreadedAccess() {
        final int nbThreads = 10;
        final int nbLoops = 10;
        TestThread[] threads = new TestThread[nbThreads];
        for (int i = 0; i < threads.length; i++) {
            final int baseValue = (i + 1) * 1000000;
            threads[i] = new TestThread(baseValue, nbLoops);
        }
        
        for (TestThread t : threads) {
            t.start();
        }
        
        boolean running = true;
        while (running) {
            running = false;
            for (TestThread t : threads) {
                if (t.isAlive()) {
                    running = true;
                    break;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        
        int errors = 0;
        for (TestThread t : threads) {
            if (t.getError()) {
                errors++;
            }
        }
        if (errors > 0) {
            Assert.fail("" + errors + " errors");
        }
    }
    
    private class TestThread extends Thread {
        private final int baseValue;
        private final int nbLoops;
        
        private boolean error = false;
        
        public TestThread(int baseValue, int nbLoops) {
            super("Thread-" + baseValue);
            this.baseValue = baseValue;
            this.nbLoops = nbLoops;
        }
        
        public void run() {
            for (int loop = 0; loop < nbLoops; loop++) {
                try {
                    int value = baseValue + loop;
                    sendReceive("" + value, "response" + value);
                } catch (ProtocolException e) {
                    error = true;
                    throw new RuntimeException(e);
                } catch (TimeoutException e) {
                    error = true;
                    throw new RuntimeException(e);
                } catch (Throwable t) {
                    error = true;
                    throw new RuntimeException(t);
                }
            }
        }
        
        public boolean getError() {
            return error;
        }
    }

    private void sendReceive(String message, String response) throws Throwable {
        TestUtils.sendReceive(client, message, server, response);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
