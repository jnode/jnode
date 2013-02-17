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
