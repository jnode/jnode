/*
 * $Id: ThreadCommandInvoker.java 3374 2007-08-02 18:15:27Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007 JNode.org
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;

import junit.framework.TestCase;

import org.jnode.util.ReaderInputStream;

public class ReaderInputStreamTest extends TestCase {

    public void testEmpty() throws Exception {
        Reader r = new StringReader("");
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        BufferedReader bisr = new BufferedReader(new InputStreamReader(ris));
        String line = bisr.readLine();
        assertNull(line);
    }
    
    public void testOneLine() throws Exception {
        final String LINE = "The quick brown fox jumped over the lazy dog";
        Reader r = new StringReader(LINE + "\n");
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        BufferedReader bisr = new BufferedReader(new InputStreamReader(ris));
        String line = bisr.readLine();
        assertEquals(LINE, line);
    }
    
    public void testByteAtATime() throws Exception {
        final String LINE = "The quick brown fox jumped over the lazy dog\n";
        final byte[] BYTES = LINE.getBytes();
        Reader r = new StringReader(LINE);
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        for (int i = 0; i < BYTES.length; i++) {
            assertEquals(BYTES[i], ris.read());
        }
        assertEquals(-1, ris.read());
    }
    
    public void testBigBuffer() throws Exception {
        final String LINE = "The quick brown fox jumped over the lazy dog\n";
        Reader r = new StringReader(LINE);
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        byte[] buffer = new byte[100];
        assertEquals(LINE.length(), ris.read(buffer));
    }
    
    public void testTwoBytesAtATime() throws Exception {
        final String LINE = "The quick brown fox jumped over the lazy dog";
        final byte[] BYTES = LINE.getBytes();
        final byte[] buffer = new byte[2];
        Reader r = new StringReader(LINE);
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        for (int i = 0; i < BYTES.length; i += 2) {
            assertEquals(2, ris.read(buffer));
            assertEquals(BYTES[i], buffer[0]);
            assertEquals(BYTES[i + 1], buffer[1]);
        }
        assertEquals(-1, ris.read(buffer));
    }
    
    public void testEmpty2() throws Exception {
        Reader r = new StringReader("");
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        assertEquals(-1, ris.read());
    }
    
    public void testLatin1Simple() throws Exception {
        final String LINE = "The quick brown fox jumped over the lazy dog";
        Reader r = new StringReader(LINE);
        ReaderInputStream ris = new ReaderInputStream(r, "latin1");
        byte[] buffer = new byte[100];
        assertEquals(LINE.length(), ris.read(buffer));
        for (int i = 0; i < LINE.length(); i++) {
            assertEquals((byte) LINE.charAt(i), buffer[i]);
        }
    }
    
    public void testLatin1All() throws Exception {
        byte[] bytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            bytes[i] = (byte) i;
        }
        final String LINE = new String(bytes, "latin1");
        Reader r = new StringReader(LINE);
        ReaderInputStream ris = new ReaderInputStream(r, "latin1");
        byte[] buffer = new byte[256];
        assertEquals(LINE.length(), ris.read(buffer));
        for (int i = 0; i < LINE.length(); i++) {
            assertEquals((byte) i, buffer[i]);
        }
    }

    public void testBadLatin1All() throws Exception {
        char[] chars = new char[257];
        for (int i = 0; i < 257; i++) {
            chars[i] = (char) i;
        }
        final String LINE = new String(chars);
        Reader r = new StringReader(LINE);
        ReaderInputStream ris = new ReaderInputStream(r, "latin1");
        byte[] buffer = new byte[257];
        try {
            ris.read(buffer);
            fail("No exception raised");
        } catch (UnmappableCharacterException ex) {
            // expected
        }
    }
    
    public void testUnicode() throws Exception {
        char[] chars = new char[1024];
        for (int i = 0; i < 1024; i++) {
            chars[i] = (char) i;
        }
        final String LINE = new String(chars);
        Reader r = new StringReader(LINE);
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        InputStreamReader isr = new InputStreamReader(ris);
        char[] buffer = new char[1024];
        isr.read(buffer);
        for (int i = 0; i < 1024; i++) {
            assertEquals(chars[i], buffer[i]);
        }
    }
    
    public void testUnicode2() throws Exception {
        char[] chars = new char[]{'\ud800', '\udc00'};
        final String LINE = new String(chars);
        Reader r = new StringReader(LINE);
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        InputStreamReader isr = new InputStreamReader(ris);
        assertEquals(chars[0], isr.read());
        assertEquals(chars[1], isr.read());
    }
    
    public void testBadUnicode() throws Exception {
        char[] chars = new char[]{'\ud800'};
        final String LINE = new String(chars);
        Reader r = new StringReader(LINE);
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        InputStreamReader isr = new InputStreamReader(ris);
        try {
            isr.read();
            fail("No exception raised");
        } catch (MalformedInputException ex) {
            // expected
        }
    }
    
    public void testBadUnicode2() throws Exception {
        char[] chars = new char[]{'a', '\ud800'};
        final String LINE = new String(chars);
        Reader r = new StringReader(LINE);
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        InputStreamReader isr = new InputStreamReader(ris);
        assertEquals(chars[0], isr.read());
        try {
            isr.read();
            fail("No exception raised");
        } catch (MalformedInputException ex) {
            // expected
        }
    }
    
    public void testBadUnicode3() throws Exception {
        char[] chars = new char[]{'\udc00'};
        final String LINE = new String(chars);
        Reader r = new StringReader(LINE);
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        InputStreamReader isr = new InputStreamReader(ris);
        try {
            isr.read();
            fail("No exception raised");
        } catch (MalformedInputException ex) {
            // expected
        }
    }
    
    public void testUnicode3() throws Exception {
        char[] chars = new char[]{'\ud800', '\udc00'};
        final String LINE = new String(chars);
        Reader r = new OneCharAtATimeReader(new StringReader(LINE));
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        InputStreamReader isr = new InputStreamReader(ris);
        assertEquals(chars[0], isr.read());
        assertEquals(chars[1], isr.read());
    }
    
    public void testBadUnicode4() throws Exception {
        char[] chars = new char[]{'\ud800'};
        final String LINE = new String(chars);
        Reader r = new OneCharAtATimeReader(new StringReader(LINE));
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        InputStreamReader isr = new InputStreamReader(ris);
        try {
            isr.read();
            fail("No exception raised");
        } catch (MalformedInputException ex) {
            // expected
        }
    }
    
    public void testBadUnicode5() throws Exception {
        char[] chars = new char[]{'a', '\ud800'};
        final String LINE = new String(chars);
        Reader r = new OneCharAtATimeReader(new StringReader(LINE));
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        InputStreamReader isr = new InputStreamReader(ris);
        assertEquals(chars[0], isr.read());
        try {
            isr.read();
            fail("No exception raised");
        } catch (MalformedInputException ex) {
            // expected
        }
    }
    
    public void testBadUnicode6() throws Exception {
        char[] chars = new char[]{'\udc00'};
        final String LINE = new String(chars);
        Reader r = new OneCharAtATimeReader(new StringReader(LINE));
        ReaderInputStream ris = new ReaderInputStream(r, "UTF-8");
        InputStreamReader isr = new InputStreamReader(ris);
        try {
            isr.read();
            fail("No exception raised");
        } catch (MalformedInputException ex) {
            // expected
        }
    }
    
    /**
     * This wrapper class delivers characters from a Reader one at a time, no matter
     * what the client asks for.
     */
    private class OneCharAtATimeReader extends Reader {
        
        private Reader reader;

        public OneCharAtATimeReader(Reader reader) {
            this.reader = reader;
        }

        @Override
        public void close() throws IOException {
            this.reader.close();
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0) {
                return 0;
            }
            int ch = reader.read();
            if (ch == -1) {
                return -1;
            } else {
                cbuf[off] = (char) ch;
                return 1;
            }
        }
    }
}
