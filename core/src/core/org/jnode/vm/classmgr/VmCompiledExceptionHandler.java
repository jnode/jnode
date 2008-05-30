/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm.classmgr;

import org.jnode.vm.VmAddress;
import org.jnode.vm.annotation.MagicPermission;
import org.vmmagic.unboxed.Address;

/**
 * @author epr
 */
@MagicPermission
public final class VmCompiledExceptionHandler extends AbstractExceptionHandler {

    private final VmAddress handler;
    private final VmAddress startPtr;
    private final VmAddress endPtr;

    /**
     * Create a new instance
     *
     * @param catchType
     * @param start
     * @param end
     * @param handler
     */
    public VmCompiledExceptionHandler(VmConstClass catchType, VmAddress start, VmAddress end, VmAddress handler) {
        super(catchType);
        this.startPtr = start;
        this.endPtr = end;
        this.handler = handler;
    }

    /**
     * Returns the endPtr.
     *
     * @return Object
     */
    public VmAddress getEnd() {
        return endPtr;
    }

    /**
     * Returns the handler.
     *
     * @return Object
     */
    public VmAddress getHandler() {
        return handler;
    }

    /**
     * Returns the startPtr.
     *
     * @return Object
     */
    public VmAddress getStart() {
        return startPtr;
    }

    /**
     * Is the given address between start and end.
     *
     * @param address
     * @return True if address is between start and end, false otherwise
     */
    public boolean isInScope(Address address) {
        final Address start = Address.fromAddress(startPtr);
        final Address end = Address.fromAddress(endPtr);

        return address.GE(start) && address.LT(end);
    }
}
