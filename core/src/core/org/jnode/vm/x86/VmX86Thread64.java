/*
 * $Id$
 *
 * JNode.org
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

import org.jnode.util.NumberUtils;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.memmgr.VmHeapManager;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Word;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public final class VmX86Thread64 extends VmX86Thread {

    // State when not running
    volatile Word r8;
    volatile Word r9;
    volatile Word r10;
    volatile Word r11;
    volatile Word r13;
    volatile Word r14;
    volatile Word r15;

    /**
     *
     */
    public VmX86Thread64(VmIsolatedStatics isolatedStatics) {
        super(isolatedStatics, VmX86Architecture64.SLOT_SIZE);
    }

    /**
     * @param stack
     */
    public VmX86Thread64(VmIsolatedStatics isolatedStatics, byte[] stack) {
        super(isolatedStatics, stack, VmX86Architecture64.SLOT_SIZE);
    }

    /**
     * @param javaThread
     */
    public VmX86Thread64(VmIsolatedStatics isolatedStatics, Thread javaThread) {
        super(isolatedStatics, javaThread);
    }

    protected final int getReferenceSize() {
        return VmX86Architecture64.SLOT_SIZE;
    }

    public String getReadableErrorState() {
        return "RAX " + NumberUtils.hex(exEax.toLong()) + " RBX "
            + NumberUtils.hex(exEbx.toLong()) + " RCX "
            + NumberUtils.hex(exEcx.toLong()) + " RDX "
            + NumberUtils.hex(exEdx.toLong()) + " RSI "
            + NumberUtils.hex(exEsi.toLong()) + " RDI "
            + NumberUtils.hex(exEdi.toLong()) + " RSP "
            + NumberUtils.hex(exEsp.toLong()) + " RIP "
            + NumberUtils.hex(exEip.toLong()) + " CR2 "
            + NumberUtils.hex(exCr2.toLong()) + " RFLAGS "
            + NumberUtils.hex(exEflags.toInt());
    }

    /**
     * @see org.jnode.vm.scheduler.VmThread#visit(org.jnode.vm.ObjectVisitor, org.jnode.vm.memmgr.VmHeapManager)
     */
    public boolean visit(ObjectVisitor visitor, VmHeapManager heapManager) {
        if (!super.visit(visitor, heapManager)) {
            return false;
        }
        // Scan registers
        Address addr = r8.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = r9.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = r10.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = r11.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = r13.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = r14.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = r15.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        return true;
    }
}
