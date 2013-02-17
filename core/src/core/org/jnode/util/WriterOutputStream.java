/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/**
 * This class wraps a Writer with the OutputStream API.  It is used internally
 * by the JNode shell, and is not recommended for general use.
 * 
 * @author crawley@jnode.org
 */
public class WriterOutputStream extends OutputStream {
    
    private ByteBuffer bytes = ByteBuffer.allocate(2048);
    private CharBuffer chars = CharBuffer.allocate(2048);
    
    private Writer writer;
    private CharsetDecoder decoder;
    private final boolean reallyClose;
    
    /**
     * Construct an OutputStream that encodes the data in the default character coding system.
     * @param writer the Writer to be wrapped
     * @param reallyClose if {@code true}, calling {@link #close()} will close
     *    the Writer; otherwise {@link #close()} means {@link #flush()}.
     */
    public WriterOutputStream(Writer writer, boolean reallyClose) {
        this(writer, Charset.defaultCharset().name(), reallyClose);
    }

    /**
     * Construct an OutputStream that encodes the data in the supplied character coding system.
     * @param writer the Writer to be wrapped
     * @param encoding the name of a character coding system.
     * @param reallyClose if {@code true}, calling {@link #close()} will close
     *    the Writer; otherwise {@link #close()} means {@link #flush()}.
     */
    public WriterOutputStream(Writer writer, String encoding, boolean reallyClose) {
        this.writer = writer;
        this.decoder = Charset.forName(encoding).newDecoder();
        this.bytes.clear();
        this.chars.clear();
        this.reallyClose = reallyClose;
    }

    @Override
    public synchronized void write(int b) throws IOException {
        bytes.put((byte) b);
        if (bytes.remaining() == 0) {
            flush(false);
        }
    }

    @Override
    public void flush() throws IOException {
        flush(false);
        writer.flush();
    }
    
    @Override
    public void close() throws IOException {
        flush(reallyClose);
        writer.close();
    }

    private synchronized int flush(boolean all) throws IOException {
        if (bytes.position() > 0) {
            bytes.flip();
            chars.clear();
            CoderResult cr = decoder.decode(bytes, chars, all);
            int count = chars.position();
            if (count > 0) {
                int pos = chars.arrayOffset();
                writer.write(chars.array(), pos, count);
            }
            if (cr.isError() || (all && cr == CoderResult.UNDERFLOW)) {
                cr.throwException();
            }
            if (bytes.remaining() > 0) {
                byte[] tmp = new byte[bytes.remaining()];
                bytes.get(tmp);
                bytes.clear();
                bytes.put(tmp);
            } else {
                bytes.clear();
            }
            return count;
        } else {
            return 0;
        }
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        while (len > 0) {
            int toWrite = Math.min(len, bytes.remaining());
            bytes.put(b, off, toWrite);
            off += toWrite;
            len -= toWrite;
            if (bytes.remaining() == 0) {
                flush(false);
            }
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    Writer getWriter() {
        return writer;
    }
}
