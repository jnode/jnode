/*
 * $Id$
 */
package org.jnode.test;

/**
 * Test for virtual method invocation.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InvokeTest {

	public static void main(String[] args) {
		A a1 = new A();
		A a2 = new B();
		
		System.out.print("a1.foo  : (expect A.foo) ="); 
		a1.foo();
		System.out.print("a1.foo2 : (expect A.foo2)="); 
		a1.foo2();
		
		System.out.print("a2.foo  : (expect B.foo) ="); 
		a2.foo();
		System.out.print("a2.foo2 : (expect B.foo2)="); 
		a2.foo2();
	}
	
	static class A {
		public void foo() {
			System.out.println("A.foo");
		}
		public void foo2() {
			System.out.println("A.foo2");
		}
	}

	static class B extends A {
		public void foo2() {
			System.out.println("B.foo2");
		}
		public void foo() {
			System.out.println("B.foo");
		}
	}
}
