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

package org.jnode.vm;

import org.jnode.util.NumberUtils;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.scheduler.VmProcessor;
import org.vmmagic.unboxed.Address;

/**
 * Address is not a normal Java object. Instead it is used as a reference
 * to a virtual memory address. Variables of this type are not considered to
 * be objects by the garbage collector.
 *
 * @author epr
 */
@MagicPermission
public abstract class VmAddress extends VmSystemObject {

    /**
     * Convert the given address to a String.
     * The length of the string depends of the reference size of
     * the current architecture.
     *
     * @param addr
     * @return
     */
    public static String toString(VmAddress addr) {
        final int refsize = VmProcessor.current().getArchitecture().getReferenceSize();
        if (refsize == 4) {
            return NumberUtils.hex(Address.fromAddress(addr).toInt());
        } else {
            return NumberUtils.hex(Address.fromAddress(addr).toLong());
        }
    }
}
