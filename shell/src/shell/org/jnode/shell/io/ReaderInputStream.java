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
package org.jnode.shell.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public class ReaderInputStream extends InputStream {
    private final Reader reader;
    
    private CharBuffer chars = CharBuffer.allocate(1024);
    private ByteBuffer bytes = ByteBuffer.allocate(2048);
    
    private CharsetEncoder encoder;

    public ReaderInputStream(Reader reader, String encoding) {
        this.reader = reader;
        this.encoder = Charset.forName(encoding).newEncoder();
        this.bytes.position(bytes.limit());
        this.chars.position(chars.limit());
    }

    @Override
    public synchronized int read() throws IOException {
        if (bytes.remaining() == 0) {
            if (fillBuffer(true) == -1) {
                return -1;
            }
        }
        return bytes.get();
    }
    
    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        // This implementation is simple-minded.  I'm sure we could recode it to avoid
        // the 'bytes.get' copying step if we thought about it.
        int count = 0;
        do {
            if (bytes.remaining() == 0) {
                int nosRead = fillBuffer(count == 0);
                if (nosRead <= 0) {
                    return count > 0 ? count : -1;
                }
            }
            int toCopy = Math.min(bytes.remaining(), len);
            bytes.get(b, off, toCopy);
            count += toCopy;
            len -= toCopy;
            off += toCopy;
        } while (count < len);
        return count;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }
    
    /**
     * This method puts bytes into the (empty) 'bytes' buffer.  It returns
     * <code>false</code> if no bytes were copied either because the reader
     * would have blocked or because it returned <code>-1</code>.  
     * 
     * @param wait if <code>true</code> allow the reader to block.
     * @return the number of bytes added; <code>-1</code> if none were added
     *       and the reader is at the EOF.
     * @throws IOException
     */
    private int fillBuffer(boolean wait) throws IOException {
        bytes.clear();
        // The loop is necessary because the way that the encoder has to deal
        // with UTF-16 surrogate pairs.
        CoderResult cr = null;
        int count;
        do {
            if (chars.remaining() == 0 || cr == CoderResult.UNDERFLOW) {
                if (chars.remaining() == 0) {
                    if (!reader.ready() && !wait) {
                        bytes.flip();
                        return 0;
                    }
                    chars.clear();
                } else {
                    char[] tmp = new char[chars.remaining()];
                    chars.get(tmp);
                    chars.clear();
                    chars.put(tmp);
                }
                if (reader.read(chars) == -1) {
                    chars.flip();
                    cr = encoder.encode(chars, bytes, true);
                    if (cr.isError()) {
                        cr.throwException();
                    }
                    count = bytes.position();
                    bytes.flip();
                    return count > 0 ? count : -1;
                }
                chars.flip();
            }
            cr = encoder.encode(chars, bytes, false);
            if (cr.isError()) {
                cr.throwException();
            }
            count = bytes.position();
        } while (wait && count == 0);
        bytes.flip();
        return count;
    }
}
