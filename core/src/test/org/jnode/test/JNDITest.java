/*
 * $Id$
 */
package org.jnode.test;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author epr
 */
public class JNDITest {

	public static void main(String[] args) 
	throws NamingException {
		InitialContext ic;
		ic = new InitialContext();
		ic.bind("dummy", new Object());
	}
}
