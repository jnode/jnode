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

import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.scheduler.VmScheduler;
import org.jnode.vm.scheduler.VmThread;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public final class VmX86Processor32 extends VmX86Processor {

    /**
     * TSS used in this processor
     */
    private TSS32 tss;

    /**
     * Space of boot code
     */
    private MemoryResource bootCode;

    /**
     * @param id
     * @param arch
     * @param statics
     * @param cpuId
     */
    public VmX86Processor32(int id, VmX86Architecture32 arch,
                            VmSharedStatics statics, VmIsolatedStatics isolatedStatics, VmScheduler scheduler,
                            X86CpuID cpuId) {
        super(id, arch, statics, isolatedStatics, scheduler, cpuId);
    }

    /**
     * Create a new thread
     *
     * @return The new thread
     */
    protected VmThread createThread(VmIsolatedStatics isolatedStatics) {
        return new VmX86Thread32(isolatedStatics);
    }

    /**
     * Create a new thread
     *
     * @param javaThread
     * @return The new thread
     */
    public VmThread createThread(VmIsolatedStatics isolatedStatics, Thread javaThread) {
        return new VmX86Thread32(isolatedStatics, javaThread);
    }

    /**
     * Create a new thread
     *
     * @param stack
     * @return The new thread
     */
    protected VmX86Thread createThread(VmIsolatedStatics isolatedStatics, byte[] stack) {
        return new VmX86Thread32(isolatedStatics, stack);
    }

    /**
     * Setup the given GDT for use by this processor.
     *
     * @param gdt
     */
    protected void setupGDT(GDT gdt) {
        gdt.setBase(GDT.PROCESSOR_ENTRY, ObjectReference.fromObject(this)
            .toAddress());
        // Clone TSS
        this.tss = new TSS32();
        gdt.setBase(GDT.TSS_ENTRY, tss.getAddress());

        // Create kernel stack
        tss.setKernelStack(new byte[VmThread.DEFAULT_STACK_SLOTS * VmX86Architecture32.SLOT_SIZE]);
    }

    /**
     * Setup the initial user stack
     */
    protected final void setupUserStack(byte[] userStack) {
        tss.setUserStack(userStack);
    }

    /**
     * Setup a memory region with bootcode for this processor.
     *
     * @param rm
     */
    protected final Address setupBootCode(ResourceManager rm, GDT gdt)
        throws ResourceNotFreeException {
        // Setup the AP bootcode
        final int size = UnsafeX86.getAPBootCodeSize();

        // Claim the memory
        this.bootCode = rm.claimMemoryResource(ResourceOwner.SYSTEM, null,
            size, ResourceManager.MEMMODE_ALLOC_DMA);

        // Initialize the memory
        UnsafeX86.setupBootCode(bootCode.getAddress(), gdt.getGdt(), tss
            .getTSS());

        return bootCode.getAddress();
    }
}
