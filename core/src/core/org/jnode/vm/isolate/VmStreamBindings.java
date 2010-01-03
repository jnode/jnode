/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.vm.isolate;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

import org.jnode.vm.IOContext;

/**
 * Class used to pass stdout/stderr/stdin streams to a new isolate.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmStreamBindings {

    private FileOutputStream errStream;
    private Socket errSocket;
    private FileOutputStream outStream;
    private Socket outSocket;
    private FileInputStream inStream;
    private Socket inSocket;

    /**
     * Bind the stderr of a new isolate to the given file stream.
     *
     * @param stream
     */
    public VmStreamBindings setErr(FileOutputStream stream) {
        this.errStream = stream;
        return this;
    }

    /**
     * Bind the stderr of a new isolate to the given socket.
     *
     * @param socket
     */
    public VmStreamBindings setErr(Socket socket) {
        this.errSocket = socket;
        return this;
    }

    /**
     * Bind the stdout of a new isolate to the given file stream.
     *
     * @param stream
     */
    public VmStreamBindings setOut(FileOutputStream stream) {
        this.outStream = stream;
        return this;
    }

    /**
     * Bind the stdout of a new isolate to the given socket.
     *
     * @param socket
     */
    public VmStreamBindings setOut(Socket socket) {
        this.outSocket = socket;
        return this;
    }

    /**
     * Bind the stdin of a new isolate to the given stream.
     *
     * @param stream
     */
    public VmStreamBindings setIn(FileInputStream stream) {
        this.inStream = stream;
        return this;
    }

    /**
     * Bind the stdin of a new isolate to the given stream.
     *
     * @param socket
     */
    public VmStreamBindings setIn(Socket socket) {
        this.inSocket = socket;
        return this;
    }

    /**
     * Create an isolated stdout.
     *
     * @return
     * @throws IOException
     */
    final PrintStream createIsolatedOut() throws IOException {
        final OutputStream stream;
        if (outStream != null) {
            stream = new FilterOutputStream(outStream);
        } else if (outSocket != null) {
            stream = new FilterOutputStream(outSocket.getOutputStream());
        } else {
            IOContext ioContext = VmIsolate.getRoot().getIOContext();
            stream = new FilterOutputStream(ioContext.getRealSystemOut());
        }
        return new PrintStream(stream);
    }

    /**
     * Create an isolated stderr.
     *
     * @return
     * @throws IOException
     */
    final PrintStream createIsolatedErr() throws IOException {
        final OutputStream stream;
        if (errStream != null) {
            stream = new FilterOutputStream(errStream);
        } else if (errSocket != null) {
            stream = new FilterOutputStream(errSocket.getOutputStream());
        } else {
            IOContext ioContext = VmIsolate.getRoot().getIOContext();
            stream = new FilterOutputStream(ioContext.getRealSystemErr());
        }
        return new PrintStream(stream);
    }

    /**
     * Create an isolated stdin.
     *
     * @return
     * @throws IOException
     */
    final InputStream createIsolatedIn() throws IOException {
        final InputStream stream;
        if (inStream != null) {
            stream = new WrappedInputStream(inStream);
        } else if (inSocket != null) {
            stream = new WrappedInputStream(inSocket.getInputStream());
        } else {
            IOContext ioContext = VmIsolate.getRoot().getIOContext();
            stream = new WrappedInputStream(ioContext.getRealSystemIn());
        }
        return stream;
    }

    private final class WrappedInputStream extends FilterInputStream {
        /**
         * @param in
         */
        public WrappedInputStream(InputStream in) {
            super(in);
        }
    }
}
