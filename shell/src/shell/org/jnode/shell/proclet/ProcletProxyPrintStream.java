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

import org.jnode.util.ProxyStream;
import org.jnode.util.ProxyStreamException;
import org.jnode.vm.VmSystem;

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
    private final int fd;

    public ProcletProxyPrintStream(int fd) {
        super();
        this.fd = fd;
    }

    /**
     * This method does the work of deciding which printstream to delegate to.
     * 
     * @return the PrintStream we are currently delegating to.
     */
    private PrintStream proxiedPrintStream() throws ProxyStreamException {
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        if (threadGroup instanceof ProcletContext) {
            try {
                Object stream = ((ProcletContext) threadGroup).getStream(fd);
                if (stream instanceof ProcletProxyStream) {
                    throw new ProxyStreamException(
                            "Proclet stream points to another proclet stream");
                }
                if (stream instanceof ProxyStream) {
                    stream = ((ProxyStream<?>) stream).getRealStream();
                }
                return (PrintStream) stream;
            } catch (Exception ex) {
                throw new ProxyStreamException("Proclet print broken for fd = "
                        + fd, ex);
            }
        } else {
            switch (fd) {
                case 1:
                    return VmSystem.getGlobalOutStream();
                case 2:
                    return VmSystem.getGlobalErrStream();
                default:
                    throw new ProxyStreamException(
                            "Proclet print stream broken: wrong fd = " + fd);
            }
        }
    }

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

    public boolean sameStream(PrintStream obj) throws ProxyStreamException {
        PrintStream rs = getRealStream();
        if (obj == rs) {
            return true;
        } else if (rs instanceof ProxyStream) {
            return ((ProxyStream<PrintStream>) rs).sameStream(obj);
        } else if (obj instanceof ProxyStream) {
            return ((ProxyStream<PrintStream>) obj).sameStream(rs);
        } else {
            return false;
        }
    }

}
