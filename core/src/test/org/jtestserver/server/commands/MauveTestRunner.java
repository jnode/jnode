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
package org.jtestserver.server.commands;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnode.test.mauve.SingleTestHarness;
import org.jtestserver.common.Status;
import org.jtestserver.server.TestFailureException;

public class MauveTestRunner implements TestRunner {
    private static final MauveTestRunner INSTANCE = new MauveTestRunner();
    
    public static final MauveTestRunner getInstance() {
        return INSTANCE;
    }
    
    private Status status = Status.READY;
    private RunnerThread thread = new RunnerThread();
    
    private MauveTestRunner() {        
    }
    
    @Override
    public void runTest(String test) throws TestFailureException {
        try {
            Class<?> k = Thread.currentThread().getContextClassLoader().loadClass(
                    test);
            
            thread.runTest(k);
        } catch (ClassNotFoundException e) {
            throw new TestFailureException(e);            
        }
    }

    public Status getStatus() {
        return status;
    }

    public void shutdown() {
        thread.requestShutdown();
    }

    private class RunnerThread extends Thread {
        private final Logger LOGGER = Logger.getLogger(RunnerThread.class.getName());
                
        private boolean shutdownRequested = false;
        
        private AtomicReference<Class<?>> testClassRef = new AtomicReference<Class<?>>();
        
        public void runTest(Class<?> testClass) {
            if (!isAlive()) {
                start();
            }
            
            while (!shutdownRequested && !testClassRef.compareAndSet(null, testClass)) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
        
        public void requestShutdown() {
            shutdownRequested = true;
        }
        
        @Override
        public void run() {
            while (!shutdownRequested) {
                Class<?> testClass = null;
                status = Status.READY;
                while (!shutdownRequested && ((testClass = testClassRef.getAndSet(null)) == null)) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
                
                if (!shutdownRequested) {
                    status = Status.RUNNING;
                    try {
                        Testlet t = (Testlet) testClass.newInstance();
                        TestHarness h = new SingleTestHarness(t);
                        t.test(h);
                        status = Status.READY;
                    } catch (Throwable t) {
                        LOGGER.log(Level.SEVERE, "error in run", t);            
                        status = Status.ERROR;
                    } finally {
                        testClass = null;
                    }
                }
            }
        }
    }
}
