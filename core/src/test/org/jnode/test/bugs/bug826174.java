package org.jnode.test.bugs;

public class bug826174
{

	public static void main (String args[])
	{
		System.out.println(12L);
		System.out.println(Long.parseLong("12"));
	}

}

