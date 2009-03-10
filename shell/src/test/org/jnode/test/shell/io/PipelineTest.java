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
        assertFalse(p.isShutdown());
        p.activate();
        assertTrue(p.isActive());
        assertFalse(p.isShutdown());
        is.close();
        os.close();
        assertFalse(p.isActive());
        assertTrue(p.isShutdown());
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
        try {
            is.read();
            fail("no exception on read()");
        } catch (IOException ex) {
            // expected ...
        }
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
}
