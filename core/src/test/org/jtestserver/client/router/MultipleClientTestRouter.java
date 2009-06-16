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

import gnu.testlet.runner.RunResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jtestserver.client.Config;
import org.jtestserver.client.TestDriverInstance;
import org.jtestserver.client.process.WatchDog;
import org.jtestserver.client.process.ServerProcess;
import org.jtestserver.common.protocol.Client;
import org.jtestserver.common.protocol.ProtocolException;

/**
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class MultipleClientTestRouter implements TestRouter {
    private static final Logger LOG = Logger.getLogger(MultipleClientTestRouter.class.getName());
    
    private final List<TestDriverInstance> instances;
    private final AtomicInteger currentInstance = new AtomicInteger(0);
    private final AtomicInteger numberOfTests = new AtomicInteger(0);
    private final CompletionService<TestRouterResult> completionService;
    private final ExecutorService executorService;
    private final WatchDog watchDog;
        
    public MultipleClientTestRouter(Config config, Client<?, ?> client, ServerProcess process) {
        watchDog = new WatchDog(config);
        int nbInstances = 10;
        instances = new ArrayList<TestDriverInstance>(nbInstances);
        for (int i = 0; i < nbInstances; i++) {
            instances.add(new TestDriverInstance(config, client, process, watchDog));
        }
        
        executorService = Executors.newFixedThreadPool(instances.size());
        completionService = new ExecutorCompletionService<TestRouterResult>(executorService);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addTest(String test) throws ProtocolException, IOException {
        completionService.submit(new TestCallable(test));
        numberOfTests.incrementAndGet();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws IOException, ProtocolException {
        int nbStarted = 0;
        
        for (TestDriverInstance instance : instances) {
            try {
                instance.startInstance();
                nbStarted++;
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "can't start instance", e);
            }
        }
        
        if (nbStarted == 0) {
            throw new IOException("no instance can be started");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws IOException, ProtocolException {
        int nbStopped = 0;
        
        for (TestDriverInstance instance : instances) {
            try {
                instance.stopInstance();
                nbStopped++;
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "can't stop instance", e);
            }
        }
        
        executorService.shutdown();
        
        if (nbStopped != instances.size()) {
            throw new IOException("some instances haven't stopped");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestRouterResult getResult() throws Exception {
        TestRouterResult result = null;
        if (hasPendingTests()) {
            TestRouterResult r = completionService.take().get();
            result = new TestRouterResult(r.getTest(), r.getRunResult());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasPendingTests() {
        int pendingTests = numberOfTests.get();
        LOG.info(pendingTests + " pending tests");
        return (pendingTests > 0);
    }
    
    
    private TestDriverInstance getTestDriverInstance() {
        int index;
        synchronized (instances) {
            index = currentInstance.getAndIncrement();
            if (currentInstance.compareAndSet(instances.size(), 0)) {
                index = 0;
            }
        }
            
        return instances.get(index);
    }
    
    private class TestCallable implements Callable<TestRouterResult> {
        private final Logger LOG = Logger.getLogger(TestCallable.class.getName());
        
        private final String test;
        
        TestCallable(String test) {
            this.test = test;
        }
        
        @Override
        public TestRouterResult call() throws Exception {
            LOG.info("call: test=" + test);
            RunResult runResult = getTestDriverInstance().runTest(test);
            numberOfTests.decrementAndGet();
            return new TestRouterResult(test, runResult);
        }
    }
}
