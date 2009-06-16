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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.common.protocol.Client;
import org.jtestserver.common.protocol.ProtocolException;
import org.jtestserver.common.protocol.TimeoutException;

public class TestManager {
    private static final Logger LOGGER = Logger.getLogger(TestManager.class.getName());
    
    private final TestClient client;
    private final List<Result> results;
    
    public TestManager(Client<?, ?> client) {
        this.client = new DefaultTestClient(client);
        results = new ArrayList<Result>();
    }
    
    public void runTest(String test) throws ProtocolException, IOException {
        
        LOGGER.info("running test " + test);
        
        try {
            RunResult delta = client.runMauveTest(test);
            results.add(new Result(test, delta));
        } catch (TimeoutException e) {
            LOGGER.log(Level.SEVERE, "a timeout happened", e);
        }
    }

    /**
     * @return
     */
    public boolean hasPendingTests() {
        return !results.isEmpty();
    }
    
    /**
     * @return
     */
    public Result getResult() {
        return results.remove(0);
    }
    
    public static class Result {
        private final String test;
        private final RunResult runResult;
        
        public Result(String test, RunResult runResult) {
            super();
            this.test = test;
            this.runResult = runResult;
        }

        /**
         * @return
         */
        public RunResult getRunResult() {
            return runResult;
        }

        /**
         * @return
         */
        public String getTest() {
            return test;
        }
        
    }
}
