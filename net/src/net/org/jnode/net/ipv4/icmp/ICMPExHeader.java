/*
 * $Id$
 *
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
 * This class adds the identifier and sequence number fields to the header.
 * 
 * @author epr
 */
public abstract class ICMPExHeader extends ICMPHeader {

    private final int identifier;
    private final int seqNumber;

    /**
     * @param type
     */
    public ICMPExHeader(int type, int code, int identifier, int seqNumber) {
        super(type, code);
        this.identifier = identifier;
        this.seqNumber = seqNumber;
    }

    /**
     * @param skbuf
     */
    public ICMPExHeader(SocketBuffer skbuf) {
        super(skbuf);
        this.identifier = skbuf.get16(4);
        this.seqNumber = skbuf.get16(6);
    }

    /**
     * @see org.jnode.net.ipv4.icmp.ICMPHeader#doPrefixTo(org.jnode.net.SocketBuffer)
     */
    protected void doPrefixTo(SocketBuffer skbuf) {
        skbuf.set16(4, identifier);
        skbuf.set16(6, seqNumber);
    }

    /**
     * Gets the identifier
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Gets the sequence number
     */
    public int getSeqNumber() {
        return seqNumber;
    }
}
