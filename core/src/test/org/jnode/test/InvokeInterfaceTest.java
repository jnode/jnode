/*
 * $Id$
 */
package org.jnode.test;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InvokeInterfaceTest {

	public static void main(String[] args) {
		A a2 = new B();
		
		System.out.print("a2.foo  : (expect B.foo) ="); 
		a2.foo();
		System.out.print("a2.foo2 : (expect B.foo2)="); 
		a2.foo2();
	}
	
	static interface A {
		public void foo();
		public void foo2();
	}

	static class B implements A {
		public void foo2() {
			System.out.println("B.foo2");
		}
		public void foo() {
			System.out.println("B.foo");
		}
	}
}
