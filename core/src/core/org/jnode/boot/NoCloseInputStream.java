/*
 * $Id$
 */
package org.jnode.boot;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NoCloseInputStream extends InputStream {

    private final InputStream is;

    public NoCloseInputStream(InputStream is) {
        this.is = is;
    }

    /**
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
        return this.is.available();
    }

    /**
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
        // Do not close!
    }

    /**
     * @see java.io.InputStream#mark(int)
     */
    public void mark(int readLimit) {
        this.is.mark(readLimit);
    }

    /**
     * @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return this.is.markSupported();
    }

    /**
     * @return @throws
     *         IOException
     */
    public int read() throws IOException {
        return this.is.read();
    }

    /**
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        return this.is.read(b);
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException {
        return this.is.read(b, off, len);
    }

    /**
     * @see java.io.InputStream#reset()
     */
    public void reset() throws IOException {
        this.is.reset();
    }

    /**
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long n) throws IOException {
        return this.is.skip(n);
    }
}