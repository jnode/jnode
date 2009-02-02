/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.vm.x86;

import org.jnode.vm.VmStackReader;
import org.jnode.vm.annotation.MagicPermission;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * Stack frame reader for the X86 architecture.
 * <p/>
 * Strack frame layout.
 * <p/>
 * <pre>
 *   .. bottom of stack ..
 *   method argument 1
 *     ..
 *   method argument n
 *   return address (pushed by CALL)
 *   old EBP
 *   CompiledCode ID (( EBP points here
 *   local variables
 *   calculation stack
 * </pre>
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public final class VmX86StackReader extends VmStackReader {

    // Locals are before this object.
    // ...
    public static final int METHOD_ID_OFFSET = 0;
    public static final int PREVIOUS_OFFSET = 1;
    public static final int RETURNADDRESS_OFFSET = 2;
    // Stack follows here
    // ...

    private final int slotSize;

    public VmX86StackReader(int slotSize) {
        this.slotSize = slotSize;
    }

    /**
     * @param sf
     * @return int
     */
    protected Offset getMethodIdOffset(Address sf) {
        return Offset.fromIntSignExtend(METHOD_ID_OFFSET * slotSize);
    }

    /**
     * @param sf
     * @return int
     */
    protected final Offset getPCOffset(Address sf) {
        return Offset.fromIntSignExtend(0xFFFFFFFF); //PC_OFFSET;
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
