/*
 * Created on Feb 22, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.jnode.test;

import junit.framework.TestCase;

import org.jnode.vm.classmgr.Signature;

/**
 * @author epr
 */
public class SignatureTest extends TestCase {

	/**
	 * Constructor for SignatureTest.
	 * @param arg0
	 */
	public SignatureTest(String arg0) {
		super(arg0);
	}

	public void testSignatureObject() {
		String res = Signature.toSignature(Object.class);
		assertEquals("Ljava/lang/Object;", res);
	}

	public void testSignatureChar() {
		String res = Signature.toSignature(Character.TYPE);
		assertEquals("C", res);
	}

	public void testSignatureCharArray() {
		String res = Signature.toSignature(char[].class);
		assertEquals("[C", res);
	}
}
