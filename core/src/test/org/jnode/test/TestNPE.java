/*
 * Created on Mar 12, 2003
 * 
 * To change this generated comment go to Window>Preferences>Java>Code Generation>Code Template
 */
package org.jnode.test;

/**
 * @author epr
 */
public class TestNPE {

	public static void main(String[] args) {
		try {
			String s = null;
			s.length();
			System.out.println("No throw of NPE: NOK!");
		} catch (NullPointerException ex) {
			System.out.println("catch of NPE: OK! (" + ex + ")");
		}
	}

}
