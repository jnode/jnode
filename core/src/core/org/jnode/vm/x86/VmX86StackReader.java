/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
public final class VmX86StackReader extends VmStackReader {

	// Locals are before this object.
	// ...
	public static final int METHOD_OFFSET = 0;
	public static final int MAGIC_OFFSET = 1;
	public static final int PREVIOUS_OFFSET = 2;
	public static final int RETURNADDRESS_OFFSET = 3;
	// Stack follows here
	// ...

    private final int slotSize;
    
    public VmX86StackReader(int slotSize) {
        this.slotSize = slotSize;
    }
    
	/**
	 * @param sf
	 * @return The offset of he magic in the given stackframe
	 */
	protected Offset getMagicOffset(Address sf) {
		return Offset.fromIntSignExtend(MAGIC_OFFSET * slotSize);
	}

	/**
	 * @param sf
	 * @return int
	 */
	protected Offset getMethodOffset(Address sf) {
		return Offset.fromIntSignExtend(METHOD_OFFSET * slotSize);
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
		return Offset.fromIntSignExtend(PREVIOUS_OFFSET * slotSize);
	}

	/**
	 * @param sf
	 * @return int
	 */
	protected Offset getReturnAddressOffset(Address sf) {
		return Offset.fromIntSignExtend(RETURNADDRESS_OFFSET * slotSize);
	}

}
