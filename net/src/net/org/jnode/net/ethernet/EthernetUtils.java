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
 
package org.jnode.net.ethernet;

import org.jnode.driver.net.NetworkException;

/**
 * @author epr
 */
public class EthernetUtils implements EthernetConstants {

    /**
     * Gets the procotol info from the ethernet frame in skbuf
     * @param hdr
     * @throws NetworkException The protocol cannot be found
     */
    public static int getProtocol(EthernetHeader hdr) throws NetworkException {
        final int length = hdr.getLengthType();
        if (length < ETH_FRAME_LEN) {
            // It is a length field
            return EthernetConstants.ETH_P_802_2;
        } else {
            // It is a protocol ID
            return length;
        }
    }
}
