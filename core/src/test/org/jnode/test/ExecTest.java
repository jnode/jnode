/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author epr
 */
public class ExecTest {
	
	public static void main(String[] args) {
		// Change System.out. If the process works correctly,
		// the console should not have problem with it.
		
		System.out = System.err;
		System.out.println("Message on new System.out");
		System.exit(0);
		
	}

}
