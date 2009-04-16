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
 
package org.jnode.shell.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class provides the output side of a JNode shell pipeline.
 * Unlike the standard PipedInputStream, this one is designed to
 * support a pipeline with multiple readers and writers.  
 * 
 * @author crawley@jnode.org
 */
public final class PipelineOutputStream extends OutputStream {
    
    private Pipeline pipeline;

    /**
     * This is not a public constructor.  Use {@link Pipeline#createSource()} 
     * to create a PipelineOutputStream instance.
     * 
     * @param pipeline the parent pipeline.
     */
    PipelineOutputStream(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public void close() throws IOException {
        if (pipeline != null) {
            pipeline.closeOutput(this);
            pipeline = null;
        }
    }

    @Override
    public void flush() throws IOException {
        if (pipeline != null) {
            pipeline.flush();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (pipeline == null) {
            throw new IOException("pipeline closed");
        }
        pipeline.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (pipeline == null) {
            throw new IOException("pipeline closed");
        }
        pipeline.write(b, 0, b.length);
    }

    @Override
    public void write(int b) throws IOException {
        if (pipeline == null) {
            throw new IOException("pipeline closed");
        }
        byte[] buffer = new byte[]{(byte) b};
        pipeline.write(buffer, 0, 1);
    }

}
