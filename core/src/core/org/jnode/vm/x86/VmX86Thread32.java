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

import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmX86Thread32 extends VmX86Thread {

    /**
     * Initialize this instance.
     */
    public VmX86Thread32() {
        super(VmX86Architecture32.SLOT_SIZE);
    }

    /**
     * Initialize this instance.
     * 
     * @param stack
     */
    public VmX86Thread32(byte[] stack) {
        super(stack, VmX86Architecture32.SLOT_SIZE);
    }

    /**
     * Initialize this instance.
     * 
     * @param javaThread
     */
    public VmX86Thread32(Thread javaThread) {
        super(javaThread);
    }

    /**
     * Gets a human readable representation of the system exception state.
     * 
     * @return String
     */
    public String getReadableErrorState() {
        return "EAX " + NumberUtils.hex(exEax.toInt()) + " EBX "
                + NumberUtils.hex(exEbx.toInt()) + " ECX "
                + NumberUtils.hex(exEcx.toInt()) + " EDX "
                + NumberUtils.hex(exEdx.toInt()) + " ESI "
                + NumberUtils.hex(exEsi.toInt()) + " EDI "
                + NumberUtils.hex(exEdi.toInt()) + " ESP "
                + NumberUtils.hex(exEsp.toInt()) + " EIP "
                + NumberUtils.hex(exEip.toInt()) + " CR2 "
                + NumberUtils.hex(exCr2.toInt()) + " EFLAGS "
                + NumberUtils.hex(exEflags.toInt());
    }

    protected final int getReferenceSize() {
        return VmX86Architecture32.SLOT_SIZE;
    }
}
