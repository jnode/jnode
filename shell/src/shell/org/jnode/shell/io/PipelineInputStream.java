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
 
package org.jnode.shell.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class provides the input side of a JNode shell pipeline.
 * Unlike the standard PipedInputStream, this one is designed to
 * support a pipeline with multiple readers and writers.  
 * 
 * @author crawley@jnode.org
 */
public final class PipelineInputStream extends InputStream {
    
    private Pipeline pipeline;

    /**
     * This is not a public constructor.  Use {@link Pipeline#createSink()} 
     * to create a PipelineInputStream instance.
     * 
     * @param pipeline the parent pipeline.
     */
    PipelineInputStream(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public int read() throws IOException {
        if (pipeline == null) {
            throw new IOException("pipeline closed");
        }
        byte[] buffer = new byte[1];
        int got = pipeline.read(buffer, 0, 1);
        if (got == 1) {
            return buffer[0];
        } else {
            return -1;
        }
    }

    @Override
    public int available() throws IOException {
        if (pipeline == null) {
            throw new IOException("pipeline closed");
        }
        return pipeline.available();
    }

    @Override
    public void close() throws IOException {
        if (pipeline != null) {
            pipeline.closeInput(this);
            pipeline = null;
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        // ignore
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (pipeline == null) {
            throw new IOException("pipeline closed");
        }
        return pipeline.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        if (pipeline == null) {
            throw new IOException("pipeline closed");
        }
        return pipeline.read(b, 0, b.length);
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("reset not supported");
    }

    @Override
    public long skip(long n) throws IOException {
        if (pipeline == null) {
            throw new IOException("pipeline closed");
        }
        return pipeline.skip(n);
    }    
}
