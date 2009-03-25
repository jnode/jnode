/* ZipFile.java --
   Copyright (C) 2001, 2002, 2003, 2004, 2005, 2006
   Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.util.zip;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.EOFException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

final class PartialInputStream extends InputStream {
    /**
     * The UTF-8 charset use for decoding the filenames.
     */
    private static final Charset UTF8CHARSET = Charset.forName("UTF-8");

    /**
     * The actual UTF-8 decoder. Created on demand. 
     */
    private CharsetDecoder utf8Decoder;

    private final RandomAccessFile raf;
    private final byte[] buffer;
    private long bufferOffset;
    private int pos;
    private long end;
    // We may need to supply an extra dummy byte to our reader.
    // See Inflater.  We use a count here to simplify the logic
    // elsewhere in this class.  Note that we ignore the dummy
    // byte in methods where we know it is not needed.
    private int dummyByteCount;

    public PartialInputStream(RandomAccessFile raf, int bufferSize) throws IOException {
        this.raf = raf;
        buffer = new byte[bufferSize];
        bufferOffset = -buffer.length;
        pos = buffer.length;
        end = raf.length();
    }

    void setLength(long length) {
        end = bufferOffset + pos + length;
    }

    private void fillBuffer() throws IOException {
        synchronized (raf) {
            long len = end - bufferOffset;
            if (len == 0 && dummyByteCount > 0) {
                buffer[0] = 0;
                dummyByteCount = 0;
            }
            else {
                raf.seek(bufferOffset);
                raf.readFully(buffer, 0, (int) Math.min(buffer.length, len));
            }
        }
    }
    
    public int available() {
        long amount = end - (bufferOffset + pos);
        if (amount > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) amount;
    }

    public int read() throws IOException {
        if (bufferOffset + pos >= end + dummyByteCount) return -1;
        if (pos == buffer.length) {
            bufferOffset += buffer.length;
            pos = 0;
            fillBuffer();
        }

        return buffer[pos++] & 0xFF;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        if (len > end + dummyByteCount - (bufferOffset + pos)) {
            len = (int) (end + dummyByteCount - (bufferOffset + pos));
            if (len == 0) return -1;
        }

        int totalBytesRead = Math.min(buffer.length - pos, len);
        System.arraycopy(buffer, pos, b, off, totalBytesRead);
        pos += totalBytesRead;
        off += totalBytesRead;
        len -= totalBytesRead;

        while (len > 0) {
            bufferOffset += buffer.length;
            pos = 0;
            fillBuffer();
            int remain = Math.min(buffer.length, len);
            System.arraycopy(buffer, pos, b, off, remain);
            pos += remain;
            off += remain;
            len -= remain;
            totalBytesRead += remain;
        }

        return totalBytesRead;
    }

    public long skip(long amount) throws IOException {
        if (amount < 0) return 0;
        if (amount > end - (bufferOffset + pos)) amount = end - (bufferOffset + pos);
        seek(bufferOffset + pos + amount);
        return amount;
    }

    void seek(long newpos) throws IOException {
        long offset = newpos - bufferOffset;
        if (offset >= 0 && offset <= buffer.length) {
            pos = (int) offset;
        }
        else {
            bufferOffset = newpos;
            pos = 0;
            fillBuffer();
        }
    }

    void readFully(byte[] buf) throws IOException {
        if (read(buf, 0, buf.length) != buf.length) throw new EOFException();
    }

    void readFully(byte[] buf, int off, int len) throws IOException {
        if (read(buf, off, len) != len) throw new EOFException();
    }

    int readLeShort() throws IOException {
        int result;
        if(pos + 1 < buffer.length) {
            result = ((buffer[pos + 0] & 0xff) | (buffer[pos + 1] & 0xff) << 8);
            pos += 2;
        }
        else {
            int b0 = read();
            int b1 = read();
            if (b1 == -1) throw new EOFException();
            result = (b0 & 0xff) | (b1 & 0xff) << 8;
        }
        return result;
    }

    int readLeInt() throws IOException {
        int result;
        if(pos + 3 < buffer.length) {
            result = (((buffer[pos + 0] & 0xff) | (buffer[pos + 1] & 0xff) << 8)
            | ((buffer[pos + 2] & 0xff) | (buffer[pos + 3] & 0xff) << 8) << 16);
            pos += 4;
        }
        else {
            int b0 = read();
            int b1 = read();
            int b2 = read();
            int b3 = read();
            if (b3 == -1) throw new EOFException();
            result =  (((b0 & 0xff) | (b1 & 0xff) << 8) | ((b2 & 0xff)
            | (b3 & 0xff) << 8) << 16);
        }
        return result;
    }

    /**
     * Decode chars from byte buffer using UTF8 encoding.  This
     * operation is performance-critical since a jar file contains a
     * large number of strings for the name of each file in the
     * archive.  This routine therefore avoids using the expensive
     * utf8Decoder when decoding is straightforward.
     *
     * @param buffer the buffer that contains the encoded character
     *        data
     * @param pos the index in buffer of the first byte of the encoded
     *        data
     * @param length the length of the encoded data in number of
     *        bytes.
     *
     * @return a String that contains the decoded characters.
     */
    private String decodeChars(byte[] buffer, int pos, int length) throws IOException {
        String result;
        int i=length - 1;
        while ((i >= 0) && (buffer[i] <= 0x7f)) {
            i--;
        }
        if (i < 0) {
            result = new String(buffer, 0, pos, length);
        }
        else {
            ByteBuffer bufferBuffer = ByteBuffer.wrap(buffer, pos, length);
            if (utf8Decoder == null)
            utf8Decoder = UTF8CHARSET.newDecoder();
            utf8Decoder.reset();
            char [] characters = utf8Decoder.decode(bufferBuffer).array();
            result = String.valueOf(characters);
        }
        return result;
    }

    String readString(int length) throws IOException {
        if (length > end - (bufferOffset + pos)) throw new EOFException();

        String result = null;
        try {
            if (buffer.length - pos >= length) {
                result = decodeChars(buffer, pos, length);
                pos += length;
            }
            else {
                byte[] b = new byte[length];
                readFully(b);
                result = decodeChars(b, 0, length);
            }
        }
        catch (UnsupportedEncodingException uee) {
            throw new AssertionError(uee);
        }
        return result;
    }

    public void addDummyByte() {
        dummyByteCount = 1;
    }
}
