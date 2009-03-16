package org.jnode.shell.io;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides a buffered byte-stream pipeline implementation that 
 * supports multiple sources and sinks.  The pipeline has a finite buffer,
 * so a thread that reads and writes to the same pipeline risks deadlock.
 * <p>
 * Unlike the standard Piped* classes,
 * Pipeline and its related classes do not try to detect dead pipes based
 * on exit of threads.  Instead, a pipeline shuts down when the sources
 * are closed, or when {@link #shutdown()} method called.  
 * <p>
 * The intended lifecycle of a Pipeline is as follows:
 * <ol>
 * <li>A Pipeline object is instantiated.</li>
 * <li>One or more sources and sinks are created using {@link #createSource()}
 *     and {@link #createSink()}.
 * <li>The Pipeline is activated by calling {@link #activate()}.
 * <li>Data is written to the pipeline sources and read from the sinks.
 * <li>The pipeline sources are closed, causing the pipeline to shut down cleanly.
 *     Read calls on the sinks will return any remaining buffered data and then
 *     signal EOF in the normal way.
 * <li>The sinks are closed, and the Pipeline is shutdown.
 * </ol>
 * 
 * @author crawley@jnode.org
 */
public class Pipeline {
    // FIXME This first-cut implementation unnecessarily double-copies data when
    // a reader is waiting for it.  It doesn't fill/empty the buffer in a
    // circular fashion.  If there are multiple active readers or writers, 
    // too many threads get woken up.  Finally, this class doesn't implement 
    // atomic writes / reads or detect cases where behavior is non-deterministic.
    
    private List<PipelineInputStream> sinks = 
        new ArrayList<PipelineInputStream>();
    private List<PipelineOutputStream> sources = 
        new ArrayList<PipelineOutputStream>();
    
    private byte[] buffer;
    private int pos = 0;
    private int lim = 0;
    private int state = INITIAL;
    
    private static final int INITIAL = 1;
    private static final int ACTIVE = 2;
    private static final int CLOSED = 4;
    private static final int SHUTDOWN = 8;
    
    private static final String[] STATE_NAMES = new String[] {
        null, "INITIAL", "ACTIVE", null, "CLOSED", 
        null, null, null, "SHUTDOWN"
    };
    
    /**
     * The default Pipeline buffer size.
     */
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    
    /**
     * Create a pipeline, in 'inactive' state with the default buffer size;
     */
    public Pipeline() {
        buffer = new byte[DEFAULT_BUFFER_SIZE];
    }
    
    /**
     * Create a pipeline, in 'inactive' state.
     * @param bufferSize the pipeline's buffer size.
     */
    public Pipeline(int bufferSize) {
        buffer = new byte[bufferSize];
    }
    
    /**
     * Create a sink for a inactive pipeline.
     * @return the sink.
     * @throws IOException This is thrown if the pipeline is 'active' or 'shut down'.
     */
    public synchronized PipelineInputStream createSink() throws IOException {
        checkState(INITIAL, "create");
        PipelineInputStream is = new PipelineInputStream(this);
        sinks.add(is);
        return is;
    }
    
    private void checkState(int allowedStates, String action) throws IOException {
        if ((state & allowedStates) == 0) {
            String stateName = STATE_NAMES[state];
            throw new IOException(action + " not allowed in state " + stateName);
        }
    }

    /**
     * Create a source for a inactive pipeline.
     * @return the source.
     * @throws IOException This is thrown if the pipeline is 'active' or 'shut down'.
     */
    public synchronized PipelineOutputStream createSource() throws IOException {
        checkState(INITIAL, "create");
        PipelineOutputStream os = new PipelineOutputStream(this);
        sources.add(os);
        return os;
    }

    /**
     * Put the pipeline into the 'active' state.
     * @throws IOException This is thrown if the pipeline is 'shut down', or
     *         if it is 'inactive' but there are no sources or sinks.
     */
    public synchronized void activate() throws IOException {
        checkState(INITIAL, "activate");
        if (sinks.isEmpty() || sources.isEmpty()) {
            throw new IOException("pipeline has no inputs and/or outputs");
        }
        state = ACTIVE;
    }
    
    /**
     * Test if the pipeline is in the 'active' state.
     * @return <code>true</code> if the pipeline is active.
     */
    public synchronized boolean isActive() {
        return state == ACTIVE;
    }
    
    /**
     * Test if the pipeline is in the 'closed' state.
     * @return <code>true</code> if the pipeline is closed.
     */
    public synchronized boolean isClosed() {
        return state == CLOSED;
    }
    
    /**
     * Test if the pipeline is in the 'shut down' state.
     * @return <code>true</code> if the pipeline is shut down.
     */
    public synchronized boolean isShutdown() {
        return state == SHUTDOWN;
    }
    
    /**
     * Forcibly shut down the pipeline.  This will cause any threads
     * currently blocked on sources or sinks to get an IOException.
     */
    public synchronized void shutdown() {
        state = SHUTDOWN;
        this.notifyAll();
    }

    synchronized int available() throws IOException {
        checkState(ACTIVE, "available");
        return lim - pos;
    }
    
    synchronized void closeInput(PipelineInputStream input) {
        sinks.remove(input);
        if (sinks.isEmpty()) {
            if (state < CLOSED) {
                state = CLOSED;
                this.notifyAll();
            }
        }
    }

    synchronized void closeOutput(PipelineOutputStream output) {
        sources.remove(output);
        if (sources.isEmpty()) {
            if (state < CLOSED) {
                state = CLOSED;
                this.notifyAll();
            }
        }
    }

    synchronized int read(byte[] b, int off, int len) throws IOException {
        checkState(ACTIVE | CLOSED | SHUTDOWN, "read");
        int startOff = off;
        while (off < len && state <= CLOSED) {
            while (pos == lim && state == ACTIVE) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    throw new InterruptedIOException();
                }
            }
            if (pos == lim) {
                break;
            }
            while (off < len && pos < lim) {
                b[off++] = buffer[pos++];
            }
            if (pos == lim) {
                pos = 0;
                lim = 0;
            } 
            this.notifyAll();
        }
        return (off == startOff) ? -1 : (off - startOff);
    }

    synchronized long skip(long n) throws IOException {
        checkState(ACTIVE | CLOSED | SHUTDOWN, "skip");
        long off = 0;
        while (off < n && state <= CLOSED) {
            while (pos == lim && state == ACTIVE) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    throw new InterruptedIOException();
                }
            }
            if (pos == lim) {
                break;
            }
            long count = Math.min(lim - pos, n - off);
            pos += count;
            if (pos == lim) {
                pos = 0;
                lim = 0;
            }
            this.notifyAll();
        }
        return off == 0 ? -1 : off;
    }
    
    synchronized void flush() throws IOException {
        // FIXME This should be unnecessary ... but we'll do it for now to be safe.
        this.notifyAll();
    }

    synchronized void write(byte[] b, int off, int len) throws IOException {
        checkState(ACTIVE, "write");
        while (off < len) {
            while (lim == buffer.length) {
                try {
                    this.wait();
                    checkState(ACTIVE, "write");
                } catch (InterruptedException ex) {
                    throw new InterruptedIOException();
                }
            }
            while (off < len && lim < buffer.length) {
                buffer[lim++] = b[off++];
            }
            this.notifyAll();
        }
    }
}
