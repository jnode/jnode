/*
 * $Id$
 *
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
package org.jnode.test.shell.io;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.jnode.shell.io.Pipeline;

public class PipelineTest extends TestCase {

    public void testConstructor() {
        new Pipeline();
    }
    
    public void testLifecycle() throws IOException {
        Pipeline p = new Pipeline();
        InputStream is = p.createSink();
        OutputStream os = p.createSource();
        assertFalse(p.isActive());
        assertFalse(p.isClosed());
        assertFalse(p.isShutdown());
        p.activate();
        assertTrue(p.isActive());
        assertFalse(p.isClosed());
        assertFalse(p.isShutdown());
        is.close();
        os.close();
        assertFalse(p.isActive());
        assertTrue(p.isClosed());
        assertFalse(p.isShutdown());
    }
    
    public void testLifecycle2() throws IOException {
        Pipeline p = new Pipeline();
        InputStream is = p.createSink();
        OutputStream os = p.createSource();
        assertFalse(p.isActive());
        assertFalse(p.isShutdown());
        p.activate();
        assertTrue(p.isActive());
        assertFalse(p.isShutdown());
        p.shutdown();
        assertFalse(p.isActive());
        assertTrue(p.isShutdown());
        assertEquals(-1, is.read());
        try {
            os.write('X');
            fail("no exception on write()");
        } catch (IOException ex) {
            // expected ...
        }
    }
    
    public void testOneOne() throws IOException {
        // This should work ... despite the source and sink being operated from
        // the same thread ... because we are reading/writing less than a buffer full.
        Pipeline p = new Pipeline();
        InputStream is = p.createSink();
        OutputStream os = p.createSource();
        p.activate();
        assertEquals(0, is.available());
        os.write('A');
        assertEquals(1, is.available());
        assertEquals('A', is.read());
        os.write('B');
        assertEquals('B', is.read());
        assertEquals(0, is.available());
        os.write("the quick brown fox".getBytes());
        int len = "the quick brown fox".length();
        assertEquals(len, is.available());
        byte[] buffer = new byte[100];
        assertEquals(len, is.read(buffer, 0, len));
        assertEquals("the quick brown fox", new String(buffer, 0, len));
    }
    
    public void testTwo1_One1() throws Throwable {
        Pipeline p = new Pipeline();
        InputStream is = p.createSink();
        OutputStream os = p.createSource();
        OutputStream os2 = p.createSource();
        p.activate();
        
        Source source = new Source("1".getBytes(), 10000, -1, os);
        Source source2 = new Source("2".getBytes(), 10000, -1, os2);
        Sink sink = new Sink(20000, 1, -1, is);
        
        List<Throwable> exceptions = runInThreads(new Runnable[] {source, source2, sink});
        if (exceptions.size() > 0) {
            throw exceptions.get(0);
        }
        assertEquals(10000, sink.getCount((byte) '1'));
        assertEquals(10000, sink.getCount((byte) '2'));
    }
    
    public void testTwo10_One10() throws Throwable {
        Pipeline p = new Pipeline();
        InputStream is = p.createSink();
        OutputStream os = p.createSource();
        OutputStream os2 = p.createSource();
        p.activate();
        
        Source source = new Source("1111111111".getBytes(), 1000, -1, os);
        Source source2 = new Source("2222222222".getBytes(), 1000, -1, os2);
        Sink sink = new Sink(20000, 10, -1, is);
        
        List<Throwable> exceptions = runInThreads(new Runnable[] {source, source2, sink});
        if (exceptions.size() > 0) {
            throw exceptions.get(0);
        }
        assertEquals(10000, sink.getCount((byte) '1'));
        assertEquals(10000, sink.getCount((byte) '2'));
    }
    
    public void testTwo100_One100() throws Throwable {
        Pipeline p = new Pipeline();
        InputStream is = p.createSink();
        OutputStream os = p.createSource();
        OutputStream os2 = p.createSource();
        p.activate();
        
        byte[] buff1 = new byte[100];
        Arrays.fill(buff1, (byte) '1');
        Source source = new Source(buff1, 100, -1, os);
        byte[] buff2 = new byte[100];
        Arrays.fill(buff2, (byte) '2');
        Source source2 = new Source(buff2, 100, -1, os2);
        Sink sink = new Sink(20000, 100, -1, is);
        
        List<Throwable> exceptions = runInThreads(new Runnable[] {source, source2, sink});
        if (exceptions.size() > 0) {
            throw exceptions.get(0);
        }
        assertEquals(10000, sink.getCount((byte) '1'));
        assertEquals(10000, sink.getCount((byte) '2'));
    }
    
    public void testTwo100_One100_SmallBuffer() throws Throwable {
        Pipeline p = new Pipeline(100);
        InputStream is = p.createSink();
        OutputStream os = p.createSource();
        OutputStream os2 = p.createSource();
        p.activate();
        
        byte[] buff1 = new byte[100];
        Arrays.fill(buff1, (byte) '1');
        Source source = new Source(buff1, 100, -1, os);
        byte[] buff2 = new byte[100];
        Arrays.fill(buff2, (byte) '2');
        Source source2 = new Source(buff2, 100, -1, os2);
        Sink sink = new Sink(20000, 100, -1, is);
        
        List<Throwable> exceptions = runInThreads(new Runnable[] {source, source2, sink});
        if (exceptions.size() > 0) {
            throw exceptions.get(0);
        }
        assertEquals(10000, sink.getCount((byte) '1'));
        assertEquals(10000, sink.getCount((byte) '2'));
    }
    
    /**
     * Create Threads for each runnable (with an exception handler), start them,
     * join them and return any exceptions
     * @param runnables the test runnables
     * @return any exceptions caught.
     */
    private List<Throwable> runInThreads(Runnable[] runnables) {
        final List<Throwable> exceptions = new ArrayList<Throwable>();
        Thread[] threads = new Thread[runnables.length];
        for (int i = 0; i < threads.length; i++) {
            System.err.println("create");
            threads[i] = new Thread(runnables[i]);
            threads[i].setUncaughtExceptionHandler(
                    new UncaughtExceptionHandler() {
                        public void uncaughtException(Thread thr, Throwable exc) {
                            System.err.println("handled exception " + exc);
                            exc.printStackTrace();
                            exceptions.add(exc);
                        }
                    });
        }
        for (Thread thread : threads) {
            System.err.println("start");
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                System.err.println("join");
                thread.join();
            } catch (InterruptedException ex) {
                exceptions.add(ex);
            }
        }
        System.err.println("done");
        return exceptions;
    }
    
    private static class Source implements Runnable {
        private byte[] b;
        private int count;
        private int sleep;
        private OutputStream os;

        /**
         * Create a test source.
         * @param b the buffer to be written
         * @param count the number of times 
         * @param sleep sleep this number of milliseconds between writes; -1 means no sleep.
         * @param os write bytes here.
         */
        Source(byte[] b, int count, int sleep, OutputStream os) {
            this.b = b;
            this.count = count;
            this.os = os;
            this.sleep = sleep;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < count; i++) {
                    os.write(b);
                    if (sleep != -1) {
                        Thread.sleep(sleep);
                    }
                }
                os.close();
                os = null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                System.err.println("wrote " + count);
                try {
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    private static class Sink implements Runnable {
        private HashMap<Byte, Integer> counters = new HashMap<Byte, Integer>();
        private InputStream is;
        private int max;
        private int sleep;
        private int len;

        /**
         * Create a test Sink.
         * @param max if we read more than this number of bytes, throw an exception
         * @param len the read buffer size.
         * @param sleep sleep this number of milliseconds between reads; -1 means no sleep.
         * @param is read bytes from here.
         */
        Sink(int max, int len, int sleep, InputStream is) {
            this.is = is;
            this.max = max;
            this.sleep = sleep;
            this.len = len;
        }

        @Override
        public void run() {
            try {
                int counter = 0;
                byte[] buffer = new byte[len];
                int nosRead = is.read(buffer);
                while (nosRead != -1) {
                    for (int i = 0; i < nosRead; i++) {
                        Byte bb = new Byte((byte) buffer[i]);
                        Integer cc = counters.get(bb);
                        int c = (cc == null) ? 1 : (cc.intValue() + 1);
                        counters.put(bb, new Integer(c));
                        if (++counter > max) {
                            throw new RuntimeException("too many (" + counter + ")");
                        }
                    }
                    if (sleep != -1) {
                        Thread.sleep(sleep);
                    }
                    nosRead = is.read(buffer);
                }
                System.err.println("read " + counter);
                is.close();
                is = null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        /**
         * Get the number of bytes read for a given byte value.
         * @param b the key byte
         * @return the number instances of the 'key' byte read.
         */
        public int getCount(byte b) {
            Integer cc = counters.get(b);
            return (cc == null) ? 0 : cc.intValue();
        }
    }
}
