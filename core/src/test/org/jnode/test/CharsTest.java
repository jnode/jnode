package org.jnode.test;

/**
 * Print all (8-bit) chars to the console, for debugging purposes.
 * This demonstrates the support for 8-bit chars introduced to
 * the output driver ({@link org.jnode.driver.console.x86.ScrollableShellConsole}).
 * 
 * @author Bengt B�verman
 * @since 2003-08
 */
public class CharsTest
{
	public static void main(String[] args) 
	{
		System.out.println("Test written by Bengt B�verman");
		
		System.out.println("Some chars used in Sweden: ��� ��� ���� ��");
		
		System.out.println("Other chars: � �� �� �� ���� ���� �� �$� ����� ");

		System.out.println();
		System.out.print("All 8-bit chars in groups of 64:");
		for (int i=0; i<256; i++) {
			if (i%64 == 0) System.out.println();
			
			System.out.print((char) i);	
		}
		System.out.println();
	}
}
