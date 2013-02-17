/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.net.arp;

import static org.junit.Assert.*;

import java.net.SocketException;

import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetConstants;
import org.junit.Test;

public class ARPHeaderTest {

	private static final int ARP_HEADER_LENGTH = 28;

	@Test
	public void testHeaderFromSocketBuffer() throws SocketException {
		SocketBuffer buffer = getSocketBuffer();
		ARPHeader header = new ARPHeader(buffer);
		assertEquals(ARP_HEADER_LENGTH,header.getLength());
		assertEquals(ARPOperation.ARP_REQUEST,header.getOperation());
		assertEquals(1,header.getHType());
		assertEquals(EthernetConstants.ETH_P_IP,header.getPType());
	}

	private SocketBuffer getSocketBuffer() {
		SocketBuffer buffer = new SocketBuffer(ARP_HEADER_LENGTH);
		buffer.append(ARP_HEADER_LENGTH);
		buffer.set16(0, 1);
		buffer.set16(2, 0x800);
		buffer.set(4, 6);
		buffer.set(5, 4);
		buffer.set16(6, 1);
		buffer.set(8, 0);
		buffer.set(14, 0);
		buffer.set(18, 0);
		buffer.set(24, 0);
		return buffer;
	}

}
