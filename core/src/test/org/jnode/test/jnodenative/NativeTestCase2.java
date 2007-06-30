package org.jnode.test.jnodenative;

public class NativeTestCase2 {
	public TestCase2 getTestCase2()
	{
		System.err.println("this is the native implementation for JNode");
		return new TestCase2();
	}
}
