/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.shell.proclet;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jnode.util.ProxyStream;
import org.jnode.util.ProxyStreamException;

/**
 * This class provides a proxy mechanism for System.in. If the current thread is
 * a member thread of a ProcletContext, operations are dispatched to the
 * ProcletContext-specific 'standard input'. Otherwise, they go to the global
 * standard input.
 * 
 * @author crawley@jnode.org
 */
public class ProcletProxyInputStream extends InputStream implements
        ProcletProxyStream<InputStream> {
    
    private Map<Integer, InputStream> streamMap;

    private int fd;

    /**
     * Construct a proxy input stream for a designated fd. Note that if the fd
     * is non-zero, the proxy will not work in a non-ProcletContext context, and
     * not work in the ProcletContext context if the fd doesn't correspond to an
     * InputStream.
     * 
     * @param is the initial value for globalInput.
     * @param fd
     */
    @SuppressWarnings("unchecked")
    public ProcletProxyInputStream(InputStream is, int fd) {
        this.fd = fd;
        streamMap = new HashMap<Integer, InputStream>();
        if (is == null) {
            throw new IllegalArgumentException("null stream");
        }
        if (is instanceof ProxyStream<?>) {
            is = ((ProxyStream<InputStream>) is).getProxiedStream();
        } 
        streamMap.put(ProcletIOContext.GLOBAL_STREAM_ID, is);
    }

    /**
     * Construct a proxy input stream based on the state of the supplied proxy.
     * The new proxy has all entries of the existing one except that the entry
     * for pid is set / reset to in.
     * 
     * @param proxy
     * @param is
     * @param pid
     */
    @SuppressWarnings("unchecked")
    public ProcletProxyInputStream(ProcletProxyInputStream proxy, InputStream is,
            int pid) {
        if (is == null) {
            throw new IllegalArgumentException("null stream");
        }
        if (is instanceof ProxyStream<?>) {
            is = ((ProxyStream<InputStream>) is).getProxiedStream();
        } 
        streamMap = new HashMap<Integer, InputStream>(proxy.streamMap);
        streamMap.put(pid, is);
        fd = proxy.fd;
    }

    @Override
    public int read() throws IOException {
        return getRealStream().read();
    }

    @Override
    public int available() throws IOException {
        return getRealStream().available();
    }

    @Override
    public void close() throws IOException {
        // TODO - should we intercept attempts to close the global stdin?
        getRealStream().close();
    }

    @Override
    public void mark(int readLimit) {
        getRealStream().mark(readLimit);
    }

    @Override
    public boolean markSupported() {
        return getRealStream().markSupported();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return getRealStream().read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return getRealStream().read(b);
    }

    @Override
    public void reset() throws IOException {
        getRealStream().reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return getRealStream().skip(n);
    }

    public InputStream getRealStream() throws ProxyStreamException {
        InputStream is = getProxiedStream();
        if (is instanceof ProxyStream) {
            return (InputStream) ((ProxyStream<?>) is).getRealStream();
        } else {
            return is;
        }
    }

    public InputStream getProxiedStream() throws ProxyStreamException {
        Proclet procletContext = Proclet.currentProcletContext();
        int pid = (procletContext == null) ? 
                ProcletIOContext.GLOBAL_STREAM_ID : procletContext.getPid();
        InputStream is = streamMap.get(pid);
        if (is == null) {
            throw new ProxyStreamException(
                    "Proclet stream not set (fd = " + fd + ")");
        } else if (is instanceof ProcletProxyStream) {
            throw new ProxyStreamException(
                    "Proclet stream points to another proclet stream (fd = " + fd + ")");
        }
        return is;
    }

    InputStream getProxiedStream(int pid) {
        InputStream is = streamMap.get(pid);
        if (is == null && pid != ProcletIOContext.GLOBAL_STREAM_ID) {
            is = streamMap.get(ProcletIOContext.GLOBAL_STREAM_ID);
        }
        return is;
    }

}
