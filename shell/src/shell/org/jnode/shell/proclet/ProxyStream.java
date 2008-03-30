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

import java.io.Closeable;

/**
 * Proxy streams have an underlying stream and offer methods for getting that
 * stream.
 * 
 * @author crawley@jnode.org
 */
public interface ProxyStream<T extends Closeable> {

    /**
     * Get the underlying (non-proxy) stream for this proxy. If there are
     * multiple layers of proxying, these are unwound.
     * 
     * @return a real (non-proxied) stream.
     */
    public T getRealStream() throws ProxyStreamException;

    /**
     * Get the stream that this proxy stream wraps. The result may also be a
     * proxy stream.
     * 
     * @return the wrapped stream for this proxy.
     */
    public T getProxiedStream() throws ProxyStreamException;

    /**
     * Determine if this proxy refers to the same underlying stream as another
     * stream object.
     * 
     * @param other
     * @return <code>true</code> if this object and <code>other</code>
     *         resolve to the same underlying stream, otherwise false. Note: the
     *         'otherwise' covers cases where <code>other</code> is
     *         <code>null</code>.
     */
    public boolean sameStream(T other) throws ProxyStreamException;
}
