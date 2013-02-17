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

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.jnode.util.IOUtils;
import org.jnode.util.ProxyStream;

/**
 * Base class for the CommandIO implementations.
 * 
 * @author crawley@jnode.org
 */
abstract class BaseCommandIO implements CommandIO {
    
    private String assignedEncoding;
    private final Closeable systemObject;
    
    BaseCommandIO(Closeable systemObject) {
        this.systemObject = systemObject;
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends Closeable> T deproxy(T stream) {
        if (stream instanceof ProxyStream<?>) {
            return ((ProxyStream<T>) stream).getProxiedStream();
        } else {
            return stream;
        }
    }
    
    public final String getAssignedEncoding() {
        return assignedEncoding;
    }

    public abstract int getDirection();

    public final String getEncoding() {
        return assignedEncoding != null ? assignedEncoding : getImpliedEncoding();
    }

    protected String getImpliedEncoding() {
        return Charset.defaultCharset().name();
    }

    public final Closeable getSystemObject() {
        return systemObject;
    }
    
    @Override
    public Closeable findBaseStream() {
        return IOUtils.findBaseStream(systemObject);
    }

    public final boolean isTTY() {
        if (systemObject == null) {
            return false;
        } else {
            return IOUtils.isTTY(systemObject);
        }
    }
    
    public final boolean isPipe() {
        if (systemObject == null) {
            return false;
        } else {
            return IOUtils.isPipe(systemObject);
        }
    }
    
    public final synchronized void close() throws IOException {
        doClose();
    }
    
    abstract void doClose() throws IOException;
    
    public final synchronized void flush() throws IOException {
        doFlush();
    }
    
    abstract void doFlush() throws IOException;

    @Override
    public final PrintStream getPrintStream() throws CommandIOException {
        return this.getPrintStream(true);
    }

    @Override
    public final PrintWriter getPrintWriter() throws CommandIOException {
        return this.getPrintWriter(true);
    }
    
    @Override
    public String toString() {
        return super.toString() + "{systemObject=" + systemObject + "}";
    }
    
}
