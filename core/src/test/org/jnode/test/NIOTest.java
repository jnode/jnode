/*
 * $Id$
 */
package org.jnode.test;

import java.nio.ByteBuffer;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NIOTest {

	public static void main(String[] args) {
		test("allocate", ByteBuffer.allocate(500));
		test("allocateDirect", ByteBuffer.allocateDirect(500));
	}
	
	private static void test(String msg, ByteBuffer buf) {
		System.out.println(msg);
		for (int i = 0; i < 5; i++) {
			buf.put((byte)('a' + i));
		}
		
		buf.flip();
		
		while (buf.remaining() > 0) {
			System.out.print((char)buf.get());
		}
		System.out.println();		
	}
}
