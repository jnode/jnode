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
package org.jtestserver.client.router;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import org.jtestserver.client.Config;
import org.jtestserver.client.TestDriverInstance;
import org.jtestserver.client.process.ServerProcess;
import org.jtestserver.common.protocol.Client;
import org.jtestserver.common.protocol.ProtocolException;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class SingleClientTestRouter implements TestRouter {
    private final Deque<String> tests = new ArrayDeque<String>();
    
    private final TestDriverInstance instance;
    
    public SingleClientTestRouter(Config config, Client<?, ?> client, ServerProcess process) {
        instance = new TestDriverInstance(config, client, process);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addTest(String test) throws ProtocolException, IOException {
        tests.offer(test);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws IOException, ProtocolException {
        instance.startInstance();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws IOException, ProtocolException {
        instance.stopInstance();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestRouterResult getResult() throws ProtocolException, IOException {
        TestRouterResult result = null;
        if (hasPendingTests()) {
            String test = tests.poll();
            result = new TestRouterResult(test, instance.runTest(test));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPendingTests() {
        return !tests.isEmpty();
    }
}
