/*
 * $Id: ThreadCommandInvoker.java 3374 2007-08-02 18:15:27Z lsantha $
 *
 * JNode.org
 * Copyright (C) 2007-2008 JNode.org
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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.jnode.util.ProxyStream;
import org.jnode.util.ProxyStreamException;

/**
 * This class provides a proxy mechanism for System.out,err. If the current
 * thread is a member thread of a ProcletContext, operations are dispatched to
 * the ProcletContext-specific 'standard output' or 'standard error'. Otherwise,
 * they go to the global 'standard' PrintStreams.
 * 
 * @author crawley@jnode.org
 */
public class ProcletProxyPrintStream extends AbstractProxyPrintStream implements
        ProcletProxyStream<PrintStream> {

    private Map<Integer, PrintStream> streamMap; 
    
    private final int fd;

    /**
     * Construct a proxy print stream with 'out' as the initial global stream.
     * 
     * @param proxy
     * @param ps
     * @param pid
     */
    public ProcletProxyPrintStream(PrintStream ps, int fd) {
        super();
        if (ps == null) {
            throw new IllegalArgumentException("null stream");
        }
        this.fd = fd;
        streamMap = new HashMap<Integer, PrintStream>();
        streamMap.put(ProcletIOContext.GLOBAL_STREAM_ID, ps);
    }
    
    /**
     * Construct a proxy print stream based on the state of the supplied proxy.
     * The new proxy has all entries of the existing one except that the entry
     * for pid is set / reset to in.
     * 
     * @param proxy
     * @param ps
     * @param pid
     */
    public ProcletProxyPrintStream(ProcletProxyPrintStream proxy, PrintStream ps,
            int pid) {
        if (ps == null) {
            throw new IllegalArgumentException("null stream");
        }
        streamMap = new HashMap<Integer, PrintStream>(proxy.streamMap);
        streamMap.put(pid, ps);
        fd = proxy.fd;
    }
    
    /**
     * This method does the work of deciding which printstream to delegate to.
     * 
     * @return the PrintStream we are currently delegating to.
     */
    private PrintStream proxiedPrintStream() throws ProxyStreamException {
        ProcletContext procletContext = ProcletContext.currentProcletContext();
        int pid = (procletContext == null) ? ProcletIOContext.GLOBAL_STREAM_ID : procletContext.getPid();
        PrintStream ps = getProxiedStream(pid);
        if (ps == null) {
            throw new ProxyStreamException(
                    "Proclet stream not set (fd = " + fd + ")");
        } else if (ps instanceof ProcletProxyStream) {
            throw new ProxyStreamException(
                    "Proclet stream points to another proclet stream (fd = " + fd + ")");
        }
        return ps;
    }

    @SuppressWarnings("unchecked")
    public PrintStream getRealStream() throws ProxyStreamException {
        PrintStream ps = proxiedPrintStream();
        if (ps instanceof ProxyStream) {
            return ((ProxyStream<PrintStream>) ps).getRealStream();
        } else {
            return ps;
        }
    }

    protected PrintStream effectiveOutput() {
        try {
            return getRealStream();
        } catch (ProxyStreamException ex) {
            return getNullPrintStream();
        }
    }

    public PrintStream getProxiedStream() throws ProxyStreamException {
        return proxiedPrintStream();
    }

    PrintStream getProxiedStream(int pid) {
        PrintStream ps = streamMap.get(pid);
        if (ps == null && pid != ProcletIOContext.GLOBAL_STREAM_ID) {
            ps = streamMap.get(ProcletIOContext.GLOBAL_STREAM_ID);
        }
        return ps;
    }

}
