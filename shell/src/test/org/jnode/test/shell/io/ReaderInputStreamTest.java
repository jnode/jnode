package org.jnode.test.shell.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.UnmappableCharacterException;

import org.jnode.shell.io.ReaderInputStream;

import junit.framework.TestCase;

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
}
