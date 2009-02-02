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
 
package org.jnode.test.net;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ipv4.IPv4Utils;
import org.jnode.util.NumberUtils;

/**
 * @author epr
 */
public class ChecksumTest {

    public static void main(String[] args) {

        SocketBuffer skbuf = new SocketBuffer();
        skbuf.insert(20);
        for (int i = 0; i < 20; i++) {
            skbuf.set(i, i);
        }
        skbuf.set16(10, 0);

        final int ccs = IPv4Utils.calcChecksum(skbuf, 0, 20);
        skbuf.set16(10, ccs);
        final int ccs2 = IPv4Utils.calcChecksum(skbuf, 0, 20);

        System.out.println("ccs=0x" + NumberUtils.hex(ccs) + ", ccs2=0x" + NumberUtils.hex(ccs2));

    }
}
