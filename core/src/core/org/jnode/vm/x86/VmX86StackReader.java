/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.vm.VmAddress;
import org.jnode.vm.VmStackReader;

/**
 * Stack frame reader for the X86 architecture.
 * 
 * Strack frame layout.
 * 
 * <pre>
 *   .. bottom of stack ..
 *   method argument 1
 *     ..
 *   method argument n
 *   return address (pushed by CALL)
 *   old EBP
 *   magic constants
 *   java program counter
 *   VmMethod reference    (( EBP points here
 *   local variables
 *   calculation stack
 * </pre>
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmX86StackReader extends VmStackReader {

	// Locals are before this object.
	// ...
	public static final int METHOD_OFFSET = 0;
	//public static final int PC_OFFSET = 4;
	public static final int MAGIC_OFFSET = 4;
	public static final int PREVIOUS_OFFSET = 8;
	//public static final int ESI_OFFSET = 16;
	//public static final int EDI_OFFSET = 20;
	//public static final int EBX_OFFSET = 20;
	public static final int RETURNADDRESS_OFFSET = 12;
	// Stack follows here
	// ...

	/**
	 * @param sf
	 * @return The offset of he magic in the given stackframe
	 */
	protected int getMagicOffset(VmAddress sf) {
		return MAGIC_OFFSET;
	}

	/**
	 * @param sf
	 * @return int
	 */
	protected int getMethodOffset(VmAddress sf) {
		return METHOD_OFFSET;
	}

	/**
	 * @param sf
	 * @return int
	 */
	protected final int getPCOffset(VmAddress sf) {
		return 0xFFFFFFFF;//PC_OFFSET;
	}

	/**
	 * @param sf
	 * @return int
	 */
	protected int getPreviousOffset(VmAddress sf) {
		return PREVIOUS_OFFSET;
	}

	/**
	 * @param sf
	 * @return int
	 */
	protected int getReturnAddressOffset(VmAddress sf) {
		return RETURNADDRESS_OFFSET;
	}

}
