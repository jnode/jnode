/**
 * $Id$
 */
package org.jnode.assembler.x86;

/**
 * <description>
 * 
 * @author epr
 */
public class X86Utils {
	
	/**
	 * Does the given value fit in an 8-bit signed byte?
	 * @param value
	 * @return boolean
	 */
	public static boolean isByte(int value) {
		return ((value >= Byte.MIN_VALUE) && (value <= Byte.MAX_VALUE));
	}

	/**
	 * Does the given value fit in an 16-bit signed byte?
	 * @param value
	 * @return boolean
	 */
	public static boolean isShort(int value) {
		return ((value >= Short.MIN_VALUE) && (value <= Short.MAX_VALUE));
	}

}
