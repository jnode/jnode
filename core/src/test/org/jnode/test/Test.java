/**
 * $Id$
 */

package org.jnode.test;

public class Test {
	private int m_v;
	private byte[] ba;

	public Test() {
	}

	public Test(int v) {
		this.m_v = v << 5;
	}

	public Test(long v) {
	}

	public int return11() {
		return 11 + m_v + ba[12];
	}

	public void trycatch() {
		try {
			return11();
		} catch (RuntimeException ex) {
			return11();
		}
	}
}
