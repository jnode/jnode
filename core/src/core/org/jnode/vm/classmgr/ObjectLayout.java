/**
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * <description>
 * 
 * @author epr
 */
public class ObjectLayout {
	
	/**
	 * The offset of the flags of an object from the start of the object. This
	 * value must be multipled by SLOT_SIZE to get the offset in bytes.
	 */
	public static final int FLAGS_SLOT = -2;

	/**
	 * The offset of the TIB of an object from the start of the object. This
	 * value must be multipled by SLOT_SIZE to get the offset in bytes.
	 */
	public static final int TIB_SLOT = -1;
	
	/**
	 * The size of the header of an object. This value must be multipled by
	 * SLOT_SIZE to get the size in bytes.
	 */
	public static final int HEADER_SLOTS = 2;
	
	/**
	 * The number of bytes object are aligned on in memory.
	 */
	public static final int OBJECT_ALIGN = 8;
	
	/**
	 * The fixed length (in elements) of an Interface Method Table.
	 * @see IMTBuilder
	 */
	public static final int IMT_LENGTH = 64;
	
	/**
	 * Returns the given value aligned to OBJECT_ALIGN. 
	 * @param value
	 * @return int
	 */
	public static int objectAlign(int value) {
		return (value + OBJECT_ALIGN - 1) & ~(OBJECT_ALIGN - 1);
	}
}
