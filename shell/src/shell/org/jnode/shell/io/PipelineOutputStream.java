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
        if (pipeline == null) {
            throw new IOException("pipeline closed");
        }
        pipeline.flush();
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
