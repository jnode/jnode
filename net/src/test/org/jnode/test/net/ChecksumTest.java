/*
 * $Id$
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
