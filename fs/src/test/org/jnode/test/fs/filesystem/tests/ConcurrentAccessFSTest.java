/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.test.fs.filesystem.tests;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Vector;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.test.fs.filesystem.AbstractFSTest;
import org.jnode.test.fs.filesystem.config.FSTestConfig;
import org.jnode.test.support.TestUtils;

/**
 * @author Fabien DUMINY
 */
public class ConcurrentAccessFSTest extends AbstractFSTest {
    protected static final int MAX_SLEEP = 100;

    protected static final int MIN_SLEEP = 10;

    protected static final int NB_READERS = 10;

    protected static final int NB_WRITERS = 10;

    public ConcurrentAccessFSTest() {
        super();
    }

    public ConcurrentAccessFSTest(String name) {
        super(name);
    }

    public void testRead(FSTestConfig config) throws Throwable {
        setUp(config);

        FSFile file = prepareFile(config);

        Monitor monitor = new Monitor("testRead");

        createReaders(monitor, file);

        monitor.waitAll();
    }

    public void testWrite(FSTestConfig config) throws Throwable {
        if (!config.isReadOnly()) {
            setUp(config);

            FSFile file = prepareFile(config);
            Monitor monitor = new Monitor("testWrite");
            createWriters(monitor, file);
            monitor.waitAll();
            assertTrue("integrity test failed", isGoodResultFile(file));
        }
    }

    public void testReadWrite(FSTestConfig config) throws Throwable {
        setUp(config);

        FSFile file = prepareFile(config);
        Monitor monitor = new Monitor("testReadWrite");
        createReaders(monitor, file);
        if (!config.isReadOnly()) {
            createWriters(monitor, file);
        }
        monitor.waitAll();
        assertTrue("integrity test failed", isGoodResultFile(file));
    }

    protected void createReaders(Monitor monitor, FSFile file) {
        for (int i = 0; i < NB_READERS; i++) {
            monitor.addWorker(new Reader(monitor, file, i * 2, NB_READERS * 2,
                    MIN_SLEEP, MAX_SLEEP));
        }
    }

    protected void createWriters(Monitor monitor, FSFile file) {
        for (int i = 0; i < NB_WRITERS; i++)
            monitor.addWorker(new Writer(monitor, file, i * 2, NB_WRITERS * 2,
                    MIN_SLEEP, MAX_SLEEP));
    }

    protected boolean isGoodResultFile(FSFile file) throws IOException {
        byte[] expData = TestUtils.getTestData(FILE_SIZE_IN_WORDS);

        ByteBuffer data = ByteBuffer.allocate(expData.length);
        file.read(0, data);

        return TestUtils.equals(expData, data.array());
    }

    protected FSFile prepareFile(FSTestConfig config) throws Exception {
        remountFS(config, false);

        final String fileName = "RWTest";
        FSEntry rootEntry = getFs().getRootEntry();
        FSEntry entry = rootEntry.getDirectory().addFile(fileName);
        FSFile file = entry.getFile();
        file.setLength(FILE_SIZE_IN_WORDS * 2);
        file.flush();
        assertEquals("Bad file size", FILE_SIZE_IN_WORDS * 2, file.getLength());

        remountFS(config, getFs().isReadOnly());

        rootEntry = getFs().getRootEntry();
        entry = rootEntry.getDirectory().getEntry(fileName);
        file = entry.getFile();
        assertEquals("Bad file size", FILE_SIZE_IN_WORDS * 2, file.getLength());

        return file;
    }
    
    class FailureRecord {
        final Throwable exception;
        final String workerClass;
        
        FailureRecord(Throwable exception, String workerClass) {
            this.exception = exception;
            this.workerClass = workerClass;
        }
    }

    class Monitor {
        private Vector<Worker> workers = new Vector<Worker>();
        private Vector<Worker> finishedWorkers = new Vector<Worker>();
        private Vector<FailureRecord> failures = new Vector<FailureRecord>();
        private final String testName;
        
        Monitor(String testName) {
            this.testName = testName;
        }

        public void addWorker(Worker worker) {
            workers.add(worker);
        }
        
        public void notifyEnd(Worker worker) {
            finishedWorkers.add(worker);
        }

        public void notifyError(Worker worker, Throwable throwable) {
            failures.add(new FailureRecord(throwable, worker.getClass().getName()));
        }

        public void waitAll() throws Throwable {
            for (Worker worker : workers) {
                new Thread(worker).start();
            }

            while (finishedWorkers.size() != workers.size()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // ignore
                }
            }

            if (failures.size() == 1) {
                FailureRecord failure = failures.get(0);
                throw new Error("Worker " + failure.workerClass + " failed", 
                        unwrap(failure.exception));
            }
            if (failures.size() > 0) {
                int i = 1;
                for (FailureRecord failure : failures) {
                    Throwable throwable = unwrap(failure.exception);
                    System.err.println("Failure #" + (i++) + " of test '" + testName +
                            " in worker " + failure.workerClass + ": " + throwable.getMessage());
                    throwable.printStackTrace(System.err);
                }
                throw new Error("Multiple workers had errors/exceptions (see earlier messages)");
            }
        }
        
        public Throwable unwrap(Throwable throwable) {
            if (throwable.getClass().equals(RuntimeException.class) &&
                    throwable.getCause() != null) {
                throwable = throwable.getCause();
            }
            return throwable;
        }
    }

    class Reader extends Worker {
        public Reader(Monitor monitor, FSFile file, int offsetStart, int offsetStep,
                int minSleep, int maxSleep) {
            super(monitor, file, offsetStart, offsetStep, minSleep, maxSleep);
        }

        public void doRun(long offset) throws IOException {
            ByteBuffer dest = ByteBuffer.allocate(2);
            file.read(offset, dest);
        }
    }

    abstract class Worker implements Runnable {
        protected FSFile file;

        protected int maxSleep;

        protected int minSleep;

        protected Monitor monitor;

        protected int offsetStart;

        protected int offsetStep;

        /**
         * 
         * @param monitor
         * @param file the file on which to work
         * @param offsetStart file's offset from which to start
         * @param offsetStep value to add to file's offset at each iteration
         * @param minSleep minimum delay to sleep between 2 iterations
         * @param maxSleep maximum delay to sleep between 2 iterations
         */
        public Worker(Monitor monitor, FSFile file, int offsetStart, int offsetStep,
                int minSleep, int maxSleep) {
            this.file = file;
            this.offsetStart = offsetStart;
            this.offsetStep = offsetStep;
            this.minSleep = minSleep;
            this.maxSleep = maxSleep;
            this.monitor = monitor;
        }

        protected abstract void doRun(long offset) throws IOException;

        public final void run() {
            long length = file.getLength();
            try {
                for (int i = offsetStart; i < (length - 1); i += offsetStep) {
                    try {
                        doRun(i);
                    } catch (IOException ex) {
                        throw new RuntimeException("Error in worker thread", ex);
                    }
                    int sleep = (int) (minSleep + Math.random() * (maxSleep - minSleep));
                    try {
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        //ignore
                    }
                }
            } catch (Throwable t) {
                monitor.notifyError(this, t);
            } finally {
                // worker has finished properly
                monitor.notifyEnd(this);
            }
        }
    }

    class Writer extends Worker {
        /**
         * {@inheritDoc}
         */
        public Writer(Monitor monitor, FSFile file, int offsetStart, int offsetStep,
                int minSleep, int maxSleep) {
            super(monitor, file, offsetStart, offsetStep, minSleep, maxSleep);
        }

        public void doRun(long offset) throws IOException {
            long value = offset / 2;
            byte msbValue = (byte) (value & 0xFF00);
            byte lsbValue = (byte) (value & 0x00FF);
            ByteBuffer src = ByteBuffer.wrap(new byte[]{msbValue, lsbValue});
            file.write(offset, src);
        }
    }
}
