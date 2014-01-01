/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

import java.io.StringWriter;
import java.nio.charset.MalformedInputException;

import org.jnode.util.WriterOutputStream;
import org.junit.Assert;
import org.junit.Test;

public class WriterOutputStreamTest {

    @Test
    public void testEmpty() throws Exception {
        String LINE = "";
        StringWriter sw = new StringWriter();
        WriterOutputStream wos = new WriterOutputStream(sw, "UTF-8", true);
        byte[] buffer = LINE.getBytes();
        wos.write(buffer);
        wos.flush();
        Assert.assertEquals(LINE, sw.getBuffer().toString());
    }

    @Test
    public void testLine() throws Exception {
        String LINE = "The quick brown fox jumped over the lazy doc";
        StringWriter sw = new StringWriter();
        WriterOutputStream wos = new WriterOutputStream(sw, "UTF-8", true);
        byte[] buffer = LINE.getBytes();
        wos.write(buffer);
        wos.flush();
        Assert.assertEquals(LINE, sw.getBuffer().toString());
    }

    @Test
    public void testByteAtATime() throws Exception {
        String LINE = "The quick brown fox jumped over the lazy doc";
        StringWriter sw = new StringWriter();
        WriterOutputStream wos = new WriterOutputStream(sw, "UTF-8", true);
        byte[] buffer = LINE.getBytes();
        for (byte b : buffer) {
            wos.write(b);
        }
        wos.flush();
        Assert.assertEquals(LINE, sw.getBuffer().toString());
    }

    @Test
    public void testByteAtATimeWithFlushes() throws Exception {
        String LINE = "The quick brown fox jumped over the lazy doc";
        StringWriter sw = new StringWriter();
        WriterOutputStream wos = new WriterOutputStream(sw, "UTF-8", true);
        byte[] buffer = LINE.getBytes();
        for (int i = 0; i < buffer.length; i++) {
            wos.write(buffer[i]);
            wos.flush();
            Assert.assertEquals(LINE.charAt(i), sw.getBuffer().charAt(i));
        }
        Assert.assertEquals(LINE, sw.getBuffer().toString());
    }

    @Test
    public void testUnicode() throws Exception {
        char[] chars = new char[8192];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) i;
        }
        byte[] buffer = new String(chars).getBytes();
        StringWriter sw = new StringWriter();
        WriterOutputStream wos = new WriterOutputStream(sw, "UTF-8", true);
        wos.write(buffer);
        wos.flush();
        StringBuffer sb = sw.getBuffer();
        Assert.assertEquals(chars.length, sb.length());
        for (int i = 0; i < chars.length; i++) {
            Assert.assertEquals(chars[i], sb.charAt(i));
        }
    }

    @Test
    public void testBadUnicode() throws Exception {
        byte[] BAD = new byte[] {(byte) 0x80};
        StringWriter sw = new StringWriter();
        WriterOutputStream wos = new WriterOutputStream(sw, "UTF-8", true);
        try {
            wos.write(BAD);
            wos.flush();
            Assert.fail("no exception thrown");
        } catch (MalformedInputException ex) {
            // expected
        }
    }

    @Test
    public void testBadUnicode2() throws Exception {
        byte[] BAD = new byte[] {(byte) 'h', (byte) 'i', (byte) 0x80};
        StringWriter sw = new StringWriter();
        WriterOutputStream wos = new WriterOutputStream(sw, "UTF-8", true);
        try {
            wos.write(BAD);
            wos.flush();
            Assert.fail("no exception thrown");
        } catch (MalformedInputException ex) {
            // expected
            Assert.assertEquals("hi", sw.getBuffer().toString());
        }
    }

    @Test
    public void testBadUnicode3() throws Exception {
        byte[] BAD = new byte[] {(byte) 'h', (byte) 'i', (byte) 0xc2, (byte) 0x00};
        StringWriter sw = new StringWriter();
        WriterOutputStream wos = new WriterOutputStream(sw, "UTF-8", true);
        try {
            wos.write(BAD);
            wos.flush();
            Assert.fail("no exception thrown");
        } catch (MalformedInputException ex) {
            // expected
            Assert.assertEquals("hi", sw.getBuffer().toString());
        }
    }

    @Test
    public void testBadUnicode4() throws Exception {
        byte[] BAD = new byte[] {(byte) 'h', (byte) 'i', (byte) 0xc2};
        StringWriter sw = new StringWriter();
        WriterOutputStream wos = new WriterOutputStream(sw, "UTF-8", true);
        wos.write(BAD);
        wos.flush();
        try {
            wos.close();
            Assert.fail("no exception thrown");
        } catch (MalformedInputException ex) {
            // expected
            Assert.assertEquals("hi", sw.getBuffer().toString());
        }
    }
}
