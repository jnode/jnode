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

import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.scheduler.VmScheduler;
import org.jnode.vm.scheduler.VmThread;
import org.vmmagic.unboxed.Address;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public final class VmX86Processor64 extends VmX86Processor {

    /**
     * @param id
     * @param arch
     * @param statics
     * @param cpuId
     */
    public VmX86Processor64(int id, VmX86Architecture64 arch, VmSharedStatics statics,
                            VmIsolatedStatics isolatedStatics, VmScheduler scheduler, X86CpuID cpuId) {
        super(id, arch, statics, isolatedStatics, scheduler, cpuId);
        // TODO Auto-generated constructor stub
    }

    protected final VmThread createThread(VmIsolatedStatics isolatedStatics) {
        return new VmX86Thread64(isolatedStatics);
    }

    protected final VmX86Thread createThread(VmIsolatedStatics isolatedStatics, byte[] stack) {
        return new VmX86Thread64(isolatedStatics, stack);
    }

    public final VmThread createThread(VmIsolatedStatics isolatedStatics, Thread javaThread) {
        return new VmX86Thread64(isolatedStatics, javaThread);
    }

    protected Address setupBootCode(ResourceManager rm, GDT gdt)
        throws ResourceNotFreeException {
        // TODO Auto-generated method stub
        return null;
    }

    protected void setupGDT(GDT gdt) {
        // TODO Auto-generated method stub

    }

    protected void setupUserStack(byte[] userStack) {
        // TODO Auto-generated method stub

    }
}
