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
import java.io.InputStream;

/**
 * The SystemInputStream is a stream wrapped for the system's actual keyboard
 * input stream.  It deals with the problem that a System.in needs to be set
 * before we have configured the keyboard.  It also supports thread localization
 * of the system input, though the CommandInvoker implementations do not use
 * this anymore.
 */
public final class SystemInputStream extends InputStream {
    // FIXME ... remove the thread localization support.  It is now just a misleading
    // historical relic.
    
    private static final InputStream EMPTY = new EmptyInputStream();

    private final class ThreadLocalInputStream extends InheritableThreadLocal {
        public Object get() {
            Object o = super.get();
            if (o == EMPTY) {
                set(systemIn);
                o = systemIn;
            }
            return o;
        }

        protected Object initialValue() {
            return systemIn;
        }
    }

    private static SystemInputStream instance;

    private InputStream systemIn;

    private ThreadLocalInputStream systemInOwnerLocal;

    private final ThreadLocalInputStream localeIn = new ThreadLocalInputStream();

    public final InputStream getIn() {
        return getLocalIn();
    }

    public final void setIn(InputStream in) {
        if (in != this) {
            localeIn.set(in);
        }
    }

    public final void claimSystemIn() {
        // TODO must be protected by the SecurityManager
        setIn(systemIn);
    }

    public final void releaseSystemIn() {
        this.systemIn = EMPTY;
    }

    public static SystemInputStream getInstance() {
        // TODO protect me with SecurityManager !
        if (instance == null) {
            instance = new SystemInputStream();
        }
        return instance;
    }

    /**
     * Set the wrapped stream to the supplied parameter.  This method
     * only has any effect if the wrapped stream is unset.
     *
     * @param systemIn
     */
    public void initialize(InputStream systemIn) {
        // TODO protect me with SecurityManager !
        // register only the first keyboard
        if (this.systemIn == EMPTY && systemIn != this)  {
            this.systemIn = systemIn;
        }
    }

    private SystemInputStream() {
        this.systemIn = EMPTY; // by default, no keyboard
    }

    /**
     * Calls the <code>in.mark(int)</code> method.
     *
     * @param readlimit The parameter passed to <code>in.mark(int)</code>
     */
    public final void mark(int readlimit) {
        getLocalIn().mark(readlimit);
    }

    /**
     * Calls the <code>in.markSupported()</code> method.
     *
     * @return <code>true</code> if mark/reset is supported, <code>false</code>
     *         otherwise
     */
    public final boolean markSupported() {
        return getLocalIn().markSupported();
    }

    /**
     * Calls the <code>in.reset()</code> method.
     *
     * @throws IOException If an error occurs
     */
    public final void reset() throws IOException {
        getLocalIn().reset();
    }

    /**
     * Calls the <code>in.available()</code> method.
     *
     * @return The value returned from <code>in.available()</code>
     * @throws IOException If an error occurs
     */
    public final int available() throws IOException {
        return getLocalIn().available();
    }

    /**
     * Calls the <code>in.skip(long)</code> method
     *
     * @param numBytes The requested number of bytes to skip.
     * @return The value returned from <code>in.skip(long)</code>
     * @throws IOException If an error occurs
     */
    public final long skip(long numBytes) throws IOException {
        return getLocalIn().skip(numBytes);
    }

    /**
     * Calls the <code>in.read()</code> method
     *
     * @return The value returned from <code>in.read()</code>
     * @throws IOException If an error occurs
     */
    public final int read() throws IOException {
        return getLocalIn().read();
    }

    /**
     * Calls the <code>read(byte[], int, int)</code> overloaded method.
     * Note that
     * this method does not redirect its call directly to a corresponding
     * method in <code>in</code>.  This allows subclasses to override only the
     * three argument version of <code>read</code>.
     *
     * @param buf The buffer to read bytes into
     * @return The value retured from <code>in.read(byte[], int, int)</code>
     * @throws IOException If an error occurs
     */
    public final int read(byte[] buf) throws IOException {
        return read(buf, 0, buf.length);
    }

    /**
     * Calls the <code>in.read(byte[], int, int)</code> method.
     *
     * @param buf    The buffer to read bytes into
     * @param offset The index into the buffer to start storing bytes
     * @param len    The maximum number of bytes to read.
     * @return The value retured from <code>in.read(byte[], int, int)</code>
     * @throws IOException If an error occurs
     */
    public final int read(byte[] buf, int offset, int len) throws IOException {
        return getLocalIn().read(buf, offset, len);
    }

    /**
     * This method closes the input stream by closing the input stream that
     * this object is filtering.  Future attempts to access this stream may
     * throw an exception.
     *
     * @throws IOException If an error occurs
     */
    public final void close() throws IOException {
        getLocalIn().close();
    }

    private InputStream getLocalIn() {
        return (InputStream) localeIn.get();
    }
}
