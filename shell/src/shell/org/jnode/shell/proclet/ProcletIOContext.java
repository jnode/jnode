/*
 * $Id$
 */
package org.jnode.shell.proclet;

import java.io.InputStream;
import java.io.PrintStream;
import org.jnode.vm.IOContext;
import org.jnode.vm.VmSystem;

/**
 * @author Levente S\u00e1ntha
 * @author crawley@jnode.org
 */
public class ProcletIOContext implements IOContext {
    private static InputStream globalInStream;
    private static PrintStream globalOutStream;
    private static PrintStream globalErrStream;

    public void setGlobalInStream(InputStream in) {
        globalInStream = in;
    }

    public void setGlobalOutStream(PrintStream out) {
        globalOutStream = out;
    }


    public PrintStream getGlobalOutStream() {
        return globalOutStream;
    }

    public void setGlobalErrStream(PrintStream err) {
        globalErrStream = err;
    }

    public PrintStream getGlobalErrStream(){
        return globalErrStream;
    }

    public InputStream getGlobalInStream() {
        return globalInStream;
    }

    public void setSystemIn(InputStream in) {
        if (in instanceof ProcletProxyInputStream) {
            try {
                in = ((ProcletProxyInputStream) in).getProxiedStream();
            }
            catch (ProxyStreamException ex) {
                throw new ProcletException("Cannot resolve 'in'", ex);
            }
        }
        ProcletContext procletContext = ProcletContext.currentProcletContext();
        if (procletContext != null) {
            procletContext.setStream(0, in);
        } else {
            globalInStream = in;
        }
    }

    public void setSystemOut(PrintStream out) {
        if (out instanceof ProcletProxyPrintStream) {
            try {
                out = ((ProcletProxyPrintStream) out).getProxiedStream();
            }
            catch (ProxyStreamException ex) {
                throw new ProcletException("Cannot resolve 'out'", ex);
            }
        }
        ProcletContext procletContext = ProcletContext.currentProcletContext();
        if (procletContext != null) {
            procletContext.setStream(1, out);
        } else {
            globalOutStream = out;
        }
    }

    public void setSystemErr(PrintStream err) {
        if (err instanceof ProcletProxyPrintStream) {
            try {
                err = ((ProcletProxyPrintStream) err).getProxiedStream();
            }
            catch (ProxyStreamException ex) {
                throw new ProcletException("Cannot resolve 'err'", ex);
            }
        }
        ProcletContext procletContext = ProcletContext.currentProcletContext();
        if (procletContext != null) {
            procletContext.setStream(2, err);
        } else {
            globalErrStream = err;
        }
    }


    public void enterContext() {
        VmSystem.setStaticField(System.class, "in", new ProcletProxyInputStream());
        VmSystem.setStaticField(System.class, "out", new ProcletProxyPrintStream(1));
        VmSystem.setStaticField(System.class, "err", new ProcletProxyPrintStream(2));    	        
    }

    public void exitContext() {
        InputStream in = globalInStream;
        PrintStream out = globalOutStream;
        PrintStream err = globalErrStream;

        if (in instanceof ProcletProxyStream) {
            throw new ProcletException("Cannot reset in to a proclet proxy stream");
        }
        if (out instanceof ProcletProxyStream) {
            throw new ProcletException("Cannot reset out to a proclet proxy stream");
        }
        if (err instanceof ProcletProxyStream) {
            throw new ProcletException("Cannot reset err to a proclet proxy stream");
        }
        VmSystem.setStaticField(System.class, "in", in);
        VmSystem.setStaticField(System.class, "out", out);
        VmSystem.setStaticField(System.class, "err", err);
    }

	public PrintStream getRealSystemErr() {
		ProcletContext procletContext = ProcletContext.currentProcletContext();
		if (procletContext != null) {
            return (PrintStream) procletContext.getStream(2);
        } else {
            return globalErrStream;
        }
	}

	public InputStream getRealSystemIn() {
		ProcletContext procletContext = ProcletContext.currentProcletContext();
		if (procletContext != null) {
            return (InputStream) procletContext.getStream(0);
        } else {
            return globalInStream;
        }
	}

	public PrintStream getRealSystemOut() {
		ProcletContext procletContext = ProcletContext.currentProcletContext();
		if (procletContext != null) {
            return (PrintStream) procletContext.getStream(1);
        } else {
            return globalOutStream;
        }
	}
}
