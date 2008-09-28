/*
 * $Id: Shell.java 4556 2008-09-13 08:02:20Z crawley $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
package org.jnode.shell.isolate;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

/**
 * This special purpose socket type is a light-weight wrapper for an 
 * input or output stream for use in local isolate communication.  One
 * of these sockets is created in the 'connected' state, and does not
 * support the normal socket connection mechanisms.
 * 
 * @author crawley@jnode.org
 */
public class IsolateSocket extends Socket {

    public IsolateSocket(InputStream in) throws SocketException {
        super(new IsolateSocketImpl(in)); 
    }
    
    public IsolateSocket(OutputStream out) throws SocketException {
        super(new IsolateSocketImpl(out)); 
    }
}
