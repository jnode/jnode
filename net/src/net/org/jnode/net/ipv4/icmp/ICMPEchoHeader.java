/*
 * $Id$
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
 
package org.jnode.net.ipv4.icmp;

import org.jnode.net.SocketBuffer;

/**
 * @author epr
 */
public class ICMPEchoHeader extends ICMPExHeader {

    /**
     * @param type
     * @param identifier
     * @param seqNumber
     */
    public ICMPEchoHeader(int type, int identifier, int seqNumber) {
        super(type, 0, identifier, seqNumber);
    }

    /**
     * @param skbuf
     */
    public ICMPEchoHeader(SocketBuffer skbuf) {
        super(skbuf);
    }

    /**
     * @see org.jnode.net.LayerHeader#getLength()
     */
    public int getLength() {
        return 8;
    }

    /**
     * Create a reply header based on info in this header
     * 
     * @throws IllegalArgumentException If the type of this header is not equal
     *             to ICMP_ECHO.
     * @return A header that is a suitable reply to this message
     */
    public ICMPEchoHeader createReplyHeader() {
        if (getType() != ICMP_ECHO) {
            throw new IllegalArgumentException("Not an echo request");
        }
        return new ICMPEchoHeader(ICMP_ECHOREPLY, getIdentifier(), getSeqNumber());
    }
}
