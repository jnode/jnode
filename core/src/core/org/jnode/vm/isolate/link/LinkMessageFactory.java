/*
 * $Id$
 */
package org.jnode.vm.isolate.link;

import java.net.ServerSocket;
import java.net.Socket;

import javax.isolate.Link;
import javax.isolate.LinkMessage;

import org.jnode.vm.isolate.VmIsolate;

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
    public static LinkMessage newDataMessage(byte[] bytes,
                                             int offset,
                                             int length) {
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
