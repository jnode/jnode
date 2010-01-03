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
 
package org.jnode.shell.proclet;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;

import org.jnode.vm.IOContext;
import org.jnode.vm.VmIOContext;
import org.jnode.vm.VmSystem;

/**
 * The ProcletIOContext is an IOContext implementation that uses Proxy streams to 
 * direct System.in/out/err traffic to different places depending on the current
 * proclet.  The System properties and 'env' are also 'procletized' in this 
 * implementation.
 * <p>
 * A JNode isolate is switched to 'proclet mode" by calling 
 * {@link VmSystem#switchToExternalIOContext(IOContext)}.
 * In theory, calling {@link VmSystem#resetIOContext()} will return the isolate
 * to "normal mode".  However, doing this abruptly cause any remaining 
 * proclets' procletized state to abruptly revert to the isolate-global version 
 * of that state.
 * 
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public class ProcletIOContext implements IOContext {
    
    public static final int GLOBAL_STREAM_ID = Proclet.NO_SUCH_PID;
    
    public ProcletIOContext() {
    }

    public synchronized void setSystemIn(InputStream is) {
        int pid = getCurrentPid();
        if (is instanceof ProcletProxyInputStream) {
            is = ((ProcletProxyInputStream) is).getProxiedStream(pid);
        }
        ProcletProxyInputStream newProxyStream = new ProcletProxyInputStream(
                    (ProcletProxyInputStream) System.in, is, pid);
        VmSystem.setStaticField(System.class, "in", newProxyStream);
    }

    public synchronized void setSystemOut(PrintStream ps) {
        int pid = getCurrentPid();
        if (ps instanceof ProcletProxyPrintStream) {
            ps = ((ProcletProxyPrintStream) ps).getProxiedStream(pid);
        }
        ProcletProxyPrintStream newProxyStream = new ProcletProxyPrintStream(
                (ProcletProxyPrintStream) System.out, ps, pid);
        VmSystem.setStaticField(System.class, "out", newProxyStream);
    }

    public synchronized void setSystemErr(PrintStream ps) {
        int pid = getCurrentPid();
        if (ps instanceof ProcletProxyPrintStream) {
            ps = ((ProcletProxyPrintStream) ps).getProxiedStream(pid);
        }
        ProcletProxyPrintStream newProxyStream = new ProcletProxyPrintStream(
                (ProcletProxyPrintStream) System.err, ps, pid);
        VmSystem.setStaticField(System.class, "err", newProxyStream);
    }
    
    public Map<String, String> getEnv() {
        Proclet proclet = Proclet.currentProcletContext();
        if (proclet != null) {
            return proclet.getEnvironment();
        } else {
            return VmIOContext.getGlobalEnv();
        }
    }

    public Properties getProperties() {
        Proclet proclet = Proclet.currentProcletContext();
        if (proclet != null) {
            return proclet.getProperties();
        } else {
            return VmIOContext.getGlobalProperties();
        }
    }

    public void setEnv(Map<String, String> env) {
        Proclet proclet = Proclet.currentProcletContext();
        if (proclet != null) {
            proclet.setEnvironment(env);
        } else {
            VmIOContext.setGlobalEnv(env);
        }
    }

    public void setProperties(Properties props) {
        Proclet proclet = Proclet.currentProcletContext();
        if (proclet != null) {
            proclet.setProperties(props);
        } else {
            VmIOContext.setGlobalProperties(props);
        }
    }

    private int getCurrentPid() {
        Proclet procletContext = Proclet.currentProcletContext();
        return (procletContext == null) ? GLOBAL_STREAM_ID : procletContext.getPid();
    }

    public synchronized void enterContext() {
        VmSystem.setStaticField(System.class, "in",
                new ProcletProxyInputStream(System.in, 0));
        VmSystem.setStaticField(System.class, "out",
                new ProcletProxyPrintStream(System.out, 1));
        VmSystem.setStaticField(System.class, "err",
                new ProcletProxyPrintStream(System.err, 2));
    }

    public synchronized void exitContext() {
        InputStream in = VmIOContext.getGlobalInStream();
        PrintStream out = VmIOContext.getGlobalOutStream();
        PrintStream err = VmIOContext.getGlobalErrStream();

        if (in instanceof ProcletProxyStream) {
            throw new ProcletException(
                    "Cannot reset in to a proclet proxy stream");
        }
        if (out instanceof ProcletProxyStream) {
            throw new ProcletException(
                    "Cannot reset out to a proclet proxy stream");
        }
        if (err instanceof ProcletProxyStream) {
            throw new ProcletException(
                    "Cannot reset err to a proclet proxy stream");
        }
        VmSystem.setStaticField(System.class, "in", in);
        VmSystem.setStaticField(System.class, "out", out);
        VmSystem.setStaticField(System.class, "err", err);
    }

    public synchronized PrintStream getRealSystemErr() {
        return ((ProcletProxyPrintStream) System.err).getProxiedStream(getCurrentPid());
    }

    public synchronized InputStream getRealSystemIn() {
        return ((ProcletProxyInputStream) System.in).getProxiedStream(getCurrentPid());
    }

    public synchronized PrintStream getRealSystemOut() {
        return ((ProcletProxyPrintStream) System.out).getProxiedStream(getCurrentPid());
    }

    synchronized void setStreamsForNewProclet(int pid, Object[] streams) {
        ProcletProxyInputStream in = new ProcletProxyInputStream(
                (ProcletProxyInputStream) System.in, (InputStream) streams[0], pid);
        ProcletProxyPrintStream out = new ProcletProxyPrintStream(
                (ProcletProxyPrintStream) System.out, (PrintStream) streams[1], pid);
        ProcletProxyPrintStream err = new ProcletProxyPrintStream(
                (ProcletProxyPrintStream) System.err, (PrintStream) streams[2], pid);
        VmSystem.setStaticField(System.class, "in", in);
        VmSystem.setStaticField(System.class, "out", out);
        VmSystem.setStaticField(System.class, "err", err);
    }
}
