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
