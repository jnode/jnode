
package org.jnode.test.bugs;

public class bug824457
{

	public static void main (String args[])
	{
		String s = "test";

		System.out.println(s.intern() == "test");
		System.out.println(s.intern() == args[0]);
	}

}

