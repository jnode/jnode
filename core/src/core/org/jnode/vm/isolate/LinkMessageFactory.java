/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.vm.isolate;

import java.net.ServerSocket;
import java.net.Socket;

import javax.isolate.Link;
import javax.isolate.LinkMessage;


public final class LinkMessageFactory {

    /**
     * Create a LinkMessage containing the given link messages.
     *
     * @param messages
     * @return
     */
    public static LinkMessage newCompositeMessage(LinkMessage... messages) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a LinkMessage containing the given data.
     *
     * @param bytes
     * @return
     */
    public static LinkMessage newDataMessage(byte[] bytes, int offset, int length) {
        return new DataLinkMessage(bytes, offset, length);
    }

    /**
     * Create a LinkMessage containing the given isolate.
     *
     * @param isolate
     * @return
     */
    public static LinkMessage newIsolateMessage(VmIsolate isolate) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a LinkMessage containing the given link.
     *
     * @return
     */
    public static LinkMessage newLinkMessage(Link link) {
        return new LinkLinkMessage(((LinkImpl) link).getImpl());
    }

    /**
     * Create a LinkMessage containing the given server socket.
     *
     * @return
     */
    public static LinkMessage newServerSocketMessage(ServerSocket socket) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a LinkMessage containing the given socket.
     *
     * @return
     */
    public static LinkMessage newSocketMessage(Socket socket) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a LinkMessage containing the given string.
     *
     * @param string
     * @return
     */
    public static LinkMessage newStringMessage(String string) {
        return new StringLinkMessage(string);
    }
}
