/*
 * $Id$
 */
package org.jnode.vm.bytecode;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface BytecodeFlags {

	public static final byte F_START_OF_BASICBLOCK = 0x01;
	public static final byte F_START_OF_TRYBLOCK = 0x02;
	public static final byte F_START_OF_TRYBLOCKEND = 0x04;
	public static final byte F_START_OF_EXCEPTIONHANDLER = 0x08;
	public static final byte F_START_OF_INSTRUCTION = 0x10;
	public static final byte F_YIELDPOINT = 0x20;
}
