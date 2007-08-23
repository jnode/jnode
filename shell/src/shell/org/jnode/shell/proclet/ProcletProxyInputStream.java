package org.jnode.shell.proclet;

import java.io.IOException;
import java.io.InputStream;
import org.jnode.vm.VmSystem;


/**
 * This class provides a proxy mechanism for System.in.  If the current thread is a member
 * thread of a ProcletContext, operations are dispatched to the ProcletContext-specific 'standard input'.
 * Otherwise, they go to the global standard input.
 * 
 * @author crawley@jnode.org
 */
public class ProcletProxyInputStream extends InputStream 
implements ProcletProxyStream<InputStream> {
	
	private int fd;
	
	/**
	 * Construct a proxy input stream for 'standard input'; i.e. fd = 0;
	 */
	public ProcletProxyInputStream() {
		this(0);
	}
	
	/**
	 * Construct a proxy input stream for a designated fd.  Note that if the
	 * fd is non-zero, the proxy will not work in a non-ProcletContext context, and 
	 * not work in the ProcletContext context if the fd doesn't correspond to an 
	 * InputStream. 
	 * 
	 * @param fd
	 */
	public ProcletProxyInputStream(int fd) {
		this.fd = fd;
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
		try {
		    getRealStream().mark(readLimit);
		}
		catch (ProxyStreamException ex) {
			// ignore
		}
	}

	@Override
	public boolean markSupported() {
		try {
		    return getRealStream().markSupported();
		}
		catch (ProxyStreamException ex) {
			return false;
		}
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
			return (InputStream) ((ProxyStream) is).getRealStream();
		}
		else {
			return is;
		}
	}

    public InputStream getProxiedStream() throws ProxyStreamException {
		ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
		if (threadGroup instanceof ProcletContext) {
			try {
				Object stream = ((ProcletContext) threadGroup).getStream(fd);
				if (stream instanceof ProcletProxyStream) {
					throw new ProxyStreamException("Proclet stream points to another proclet stream");
				}
				if (stream instanceof ProxyStream) {
					stream = ((ProxyStream) stream).getRealStream();
				}
				return (InputStream) stream;
			}
			catch (Exception ex) {
				throw new ProxyStreamException("Proclet input broken for fd = " + fd, ex);
			}
		}
		else {
			if (fd != 0) {
				throw new ProxyStreamException("Proclet input broken: wrong fd = " + fd);
			}
			return VmSystem.getGlobalInStream();
		}
	}
    
    public boolean sameStream(InputStream obj) throws ProxyStreamException {
		InputStream rs = getRealStream();
		if (obj == rs) {
			return true;
		}
		else if (rs instanceof ProxyStream) {
			return ((ProxyStream<InputStream>) rs).sameStream(obj);
		}
		else if (obj instanceof ProxyStream) {
			return ((ProxyStream<InputStream>) obj).sameStream(rs);
		}
		else {
			return false;
		}
	}

}
