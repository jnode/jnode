/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.vm.VmStackReader;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

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
	protected Offset getMagicOffset(Address sf) {
		return Offset.fromIntSignExtend(MAGIC_OFFSET);
	}

	/**
	 * @param sf
	 * @return int
	 */
	protected Offset getMethodOffset(Address sf) {
		return Offset.fromIntSignExtend(METHOD_OFFSET);
	}

	/**
	 * @param sf
	 * @return int
	 */
	protected final Offset getPCOffset(Address sf) {
		return Offset.fromIntSignExtend(0xFFFFFFFF);//PC_OFFSET;
	}

	/**
	 * @param sf
	 * @return int
	 */
	protected Offset getPreviousOffset(Address sf) {
		return Offset.fromIntSignExtend(PREVIOUS_OFFSET);
	}

	/**
	 * @param sf
	 * @return int
	 */
	protected Offset getReturnAddressOffset(Address sf) {
		return Offset.fromIntSignExtend(RETURNADDRESS_OFFSET);
	}

}
