package org.jnode.shell.proclet;

import java.io.Closeable;

import org.jnode.shell.proclet.ProxyStreamException;

/**
 * Proxy streams have an underlying stream and offer methods for getting that stream.
 * 
 * @author crawley@jnode.org
 */
public interface ProxyStream<T extends Closeable> {
	
	/**
	 * Get the underlying (non-proxy) stream for this proxy.  If there are multiple layers
	 * of proxying, these are unwound.
	 * 
	 * @return a real (non-proxied) stream.
	 */
	public T getRealStream() throws ProxyStreamException;
	
	/**
	 * Get the stream that this proxy stream wraps.  The result may also be a proxy stream.
	 * 
	 * @return the wrapped stream for this proxy.
	 */
	public T getProxiedStream() throws ProxyStreamException;
	
	/**
	 * Determine if this proxy refers to the same underlying stream as another stream object.
	 * @param other
	 * @return <code>true</code> if this object and <code>other</code> resolve to the same 
	 * underlying stream, otherwise false.  Note: the 'otherwise' covers cases where 
	 * <code>other</code> is <code>null</code>.
	 */
	public boolean sameStream(T other) throws ProxyStreamException;
}
