/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.shell.io;

import java.io.IOException;
import java.io.Writer;

/**
 * The FanoutOutputStream writes its output to multiple print streams.  This implementation
 * makes no attempt to you from adding the same OutputStream multiple times in the fanout.
 * 
 * @author crawley@jnode.org
 */
public class FanoutWriter extends Writer {

    private Writer[] writers;
    private final boolean ignoreClose;

    /**
     * Construct a FanoutOutputStream for an an initial set of streams
     * @param ignoreClose if <code>true</code>, a close all just does a flush.  In other words
     * the responsibility of closing the individual streams remains with the caller.
     * @param writers The initial set of streams.
     */
    public FanoutWriter(boolean ignoreClose, Writer ... writers) {
        this.writers = writers;
        this.ignoreClose = ignoreClose;
    }
    
    /**
     * Add another Writer to the fanout.
     * 
     * @param writer the writer to be added.
     */
    public synchronized void addStream(Writer writer) {
        int len = writers.length;
        Writer[] tmp = new Writer[len + 1];
        System.arraycopy(writers, 0, tmp, 0, len);
        tmp[len] = writer;
        writers = tmp;
    }
    
    /**
     * Remove a Writer from the fanout.
     * @param writer the Writer to be removed.
     * @return returns <code>true</code> if the stream to be removed was removed.
     */
    public synchronized boolean removeStream(Writer writer) {
        int len = writers.length;
        for (int i = 0; i < len; i++) {
            if (writers[i] == writer) {
                int len2 = writers.length - 1;
                Writer[] tmp = new Writer[len2];
                for (int j = 0; j < len2; j++) {
                    if (j < i) {
                        tmp[j] = writers[j];
                    } else {
                        tmp[j] = writers[j + 1];
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void close() throws IOException {
        if (writers == null) {
            // already closed.
        } else if (ignoreClose) {
            flush();
        } else {
            for (Writer writer : writers) {
                writer.close();
            }
            writers = null;
        }
    }

    @Override
    public void flush() throws IOException {
        for (Writer writer : this.writers) {
            writer.flush();
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (Writer writer : this.writers) {
            writer.write(cbuf, off, len);
        }
    }

    @Override
    public void write(int b) throws IOException {
        for (Writer writer : this.writers) {
            writer.write(b);
        }
    }
}
