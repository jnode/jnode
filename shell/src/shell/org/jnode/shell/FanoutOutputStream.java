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
package org.jnode.shell;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The FanoutOutputStream writes its output to multiple print streams.  This implementation
 * makes no attempt to you from adding the same OutputStream multiple times in the fanout.
 * If you do this, the close method will attempt to close some streams more than once.
 * 
 * @author crawley@jnode.org
 */
public class FanoutOutputStream extends OutputStream {

    private OutputStream[] streams;
    private final boolean ignoreClose;

    /**
     * Construct a FanoutOutputStream for an an initial set of streams
     * @param ignoreClose if <code>true</code>, a close all just does a flush.  In other words
     * the responsibility of closing the individual streams remains with the caller.
     * @param streams The initial set of streams.
     */
    public FanoutOutputStream(boolean ignoreClose, OutputStream ... streams) {
        this.streams = streams;
        this.ignoreClose = ignoreClose;
    }
    
    /**
     * Add another OutputStream to the fanout.
     * 
     * @param os the stream to be added.
     */
    public synchronized void addStream(OutputStream os) {
        int len = streams.length;
        OutputStream[] tmp = new OutputStream[len + 1];
        System.arraycopy(streams, 0, tmp, 0, len);
        tmp[len] = os;
        streams = tmp;
    }
    
    /**
     * Remove a OutputStream from the fanout.
     * @param os the stream to be removed.
     * @return returns <code>true</code> if the stream to be removed was removed.
     */
    public synchronized boolean removeStream(OutputStream os) {
        int len = streams.length;
        for (int i = 0; i < len; i++) {
            if (streams[i] == os) {
                int len2 = streams.length - 1;
                OutputStream[] tmp = new OutputStream[len2];
                for (int j = 0; j < len2; j++) {
                    if (j < i) {
                        tmp[j] = streams[j];
                    } else {
                        tmp[j] = streams[j + 1];
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void close() throws IOException {
        if (streams == null) {
            // already closed.
        } else if (ignoreClose) {
            flush();
        } else {
            for (OutputStream os : streams) {
                os.close();
            }
            streams = null;
        }
    }

    @Override
    public void flush() throws IOException {
        OutputStream[] streams = this.streams;
        for (OutputStream os : streams) {
            os.flush();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        OutputStream[] streams = this.streams;
        for (OutputStream os : streams) {
            os.write(b, off, len);
        }
    }

    @Override
    public void write(byte[] b) throws IOException  {
        OutputStream[] streams = this.streams;
        for (OutputStream os : streams) {
            os.write(b);
        }
    }

    @Override
    public void write(int b) throws IOException {
        OutputStream[] streams = this.streams;
        for (OutputStream os : streams) {
            os.write(b);
        }
    }
}
