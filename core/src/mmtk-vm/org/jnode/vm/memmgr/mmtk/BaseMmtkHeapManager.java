/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.vm.memmgr.mmtk;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.naming.NameNotFoundException;

import org.jnode.bootlog.BootLogInstance;
import org.jnode.naming.InitialNaming;
import org.jnode.system.resource.MemoryResource;
import org.jnode.system.resource.ResourceManager;
import org.jnode.system.resource.ResourceNotFreeException;
import org.jnode.system.resource.ResourceOwner;
import org.jnode.vm.Unsafe;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.facade.HeapStatistics;
import org.jnode.vm.facade.ObjectFilter;
import org.jnode.vm.facade.VmArchitecture;
import org.jnode.vm.facade.VmProcessor;
import org.jnode.vm.facade.VmUtils;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.VmHeapManager;
import org.mmtk.plan.BasePlan;
import org.mmtk.policy.Space;
import org.mmtk.utility.heap.HeapGrowthManager;
import org.mmtk.utility.heap.LazyMmapper;
import org.mmtk.utility.scan.MMType;
import org.mmtk.vm.Memory;
import org.mmtk.vm.SynchronizedCounter;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Offset;
import org.vmmagic.unboxed.Word;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class BaseMmtkHeapManager extends VmHeapManager implements
        Uninterruptible {

    /** <code>true</code> if built with GCSpy */
    private static final boolean WITH_GCSPY = false;

    /** Used by mmtypes for arrays */
    private final int[] EMPY_INT_ARRAY = new int[0];

    /** Offset in bytes of the flags from the object reference */
    private final int flagsOffset;

    /** Offset in bytes of the TIB reference from the object reference */
    private final int tibOffset;

    /** Size of the java header in bytes */
    private final int headerSize;

    /** Resource used to claim the memory region occupied by the available heap */
    private MemoryResource heapResource;

    /**
     * Is this heapmanager busy initializing (used to detect recursion in
     * initialize)
     */
    private boolean initializing;

    /**
     * @param loader
     * @param helper
     */
    public BaseMmtkHeapManager(VmClassLoader loader, HeapHelper helper) {
        super(helper);
        final int refSize = loader.getArchitecture().getReferenceSize();
        this.flagsOffset = ObjectLayout.FLAGS_SLOT * refSize;
        this.tibOffset = ObjectLayout.TIB_SLOT * refSize;
        this.headerSize = ObjectLayout.HEADER_SLOTS * refSize;
        setWriteBarrier(null);
    }

    public final Object createProcessorHeapData(VmProcessor cpu) {
        try {
            final Class[] types = { HeapHelper.class };
            final Class cls = Class.forName("org.mmtk.vm.Plan");
            final Constructor cons = cls.getConstructor(types);
            return cons.newInstance(new Object[] { helper });
        } catch (ClassNotFoundException ex) {
            throw new InternalError("Plan class not found");
        } catch (NoSuchMethodException ex) {
            ex.printStackTrace();
            throw new InternalError("Plan(HeapHelper) constructor not found");
        } catch (InstantiationException ex) {
            throw new InternalError("Cannot instantiate Plan");
        } catch (IllegalAccessException ex) {
            throw new InternalError("Cannot access Plan");
        } catch (InvocationTargetException ex) {
            throw (InternalError) new InternalError("Error instantiating Plan")
                    .initCause(ex.getTargetException());
        }
    }

    protected Object allocObject(VmClassType< ? > vmClass, int size) {
        if (false) {
            Unsafe.debug("allocObject: ");
            Unsafe.debug(vmClass.getName());
            Unsafe.debug('\n');
        }
        if (false) {
        	getCurrentProcessor().getArchitecture().getStackReader()
                    .debugStackTrace();
        }

        final int align = ObjectLayout.OBJECT_ALIGN;
        final Word headerSize = Word.fromIntZeroExtend(this.headerSize);
        final Offset tibOffset = Offset.fromIntSignExtend(this.tibOffset);
        final Offset flagsOffset = Offset.fromIntSignExtend(this.flagsOffset);
        int allocator = BasePlan.ALLOC_DEFAULT;

        final int refSize = VmUtils.getVm().getArch().getReferenceSize();
        allocator = checkAllocator(size, align, allocator);

        // Allocate the raw space
        size += this.headerSize;
        final Address ptr = alloc(size, align, this.headerSize, allocator);

        // Initialize the header
        final Address objPtr = ptr.add(headerSize);
        final ObjectReference tibRef = ObjectReference.fromObject(vmClass
                .getTIB());
        objPtr.store(tibRef, tibOffset);
        objPtr.store((int) 0, flagsOffset);

        // Post allocation
        final Object result = objPtr.toObjectReference().toObject();
        if (false) {
            Unsafe.debug("result=");
            Unsafe.debug(objPtr);
            Unsafe.debug('\n');
        }
        postAlloc(ObjectReference.fromObject(result), ObjectReference
                .fromObject(vmClass), size, allocator);

        return result;
    }

    public void dumpStatistics(PrintWriter out) {
        // Default behavior is to do nothing
    }

    public void gc() {
        // Default behavior is to do nothing
    }

    public long getFreeMemory() {
        return BasePlan.freeMemory().toLong();
    }

    /**
     * {@inheritDoc}
     */
    public HeapStatistics getHeapStatistics(ObjectFilter objectFilter) {
        return null;
    }

    public long getTotalMemory() {
        return BasePlan.totalMemory().toLong();
    }

    protected void initialize() {
        Unsafe.debug("MmtkHeapManager#initialize\n");
        if (initializing) {
        	getCurrentProcessor().getArchitecture().getStackReader()
                    .debugStackTrace();
            Unsafe.die("Recursive initialize");
        }
        initializing = true;
        final VmArchitecture arch = VmUtils.getVm().getArch();
        helper.bootArchitecture(true);

        final Address bootImgStart = helper.getBootImageStart();
        final Address bootImgEnd = helper.getBootImageEnd();
        final int bootImgSize = bootImgEnd.sub(bootImgStart.toWord()).toInt();
        LazyMmapper.boot(bootImgStart, bootImgSize);

        final Extent heapSize = helper.getHeapSize();
        HeapGrowthManager.boot(heapSize, heapSize);
        bootPlan();
        SynchronizedCounter.boot();
        initializing = false;
        Unsafe.debug("MmtkHeapManager#initialize done\n");

        Space.printVMMap();
    }

    public boolean isLowOnMemory() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isObject(Address ptr) {
        return Space.isMappedAddress(ptr);
    }

    public void start() {
        Unsafe.debug("MmtkHeapManager#start\n");

        // Claim the available heap region as resource.
        try {
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            final Address start = Memory.AVAILABLE_START();
            final Extent size = Memory.AVAILABLE_END().toWord().sub(start.toWord()).toExtent();
            heapResource = rm.claimMemoryResource(ResourceOwner.SYSTEM, start,
                    size, ResourceManager.MEMMODE_NORMAL);
        } catch (NameNotFoundException ex) {
            BootLogInstance.get().fatal("Cannot find resource manager", ex);
        } catch (ResourceNotFreeException ex) {
            BootLogInstance.get().fatal("Cannot claim available heap region", ex);
        }

    }

    public void notifyClassResolved(VmType< ? > vmType) {
        final MMType type;
        final boolean acyclic = false; // TODO understand me
        if (vmType.isArray()) {
            final VmArrayClass< ? > arrType = (VmArrayClass< ? >) vmType;
            type = new MMType(false, !arrType.isPrimitiveArray(), acyclic,
                    pickAllocatorForType(vmType), EMPY_INT_ARRAY);
        } else if (!vmType.isInterface()) {
            final VmNormalClass< ? > clsType = (VmNormalClass< ? >) vmType;
            type = new MMType(false, false, acyclic,
                    pickAllocatorForType(vmType), clsType.getReferenceOffsets());
        } else {
            type = null;
        }
        vmType.setMmType(type);
    }

    /**
     * Determine the default allocator to be used for a given type.
     * 
     * @param type
     *            The type in question
     * @return The allocator to use for allocating instances of type
     *         <code>type</code>.
     */
    private static int pickAllocatorForType(VmType< ? > type) {
        int allocator = BasePlan.ALLOC_DEFAULT;
        final String clsName = type.getName();
        if (WITH_GCSPY) {
            if (clsName.startsWith("org.mmtk.vm.gcspy")
                    || clsName.startsWith("[Lorg.mmtk.vm.gcspy")) {
                allocator = BasePlan.ALLOC_GCSPY;
            }
        }
        if (clsName.startsWith("org.mmtk")
                || clsName.startsWith("org.jnode.vm.VmProcessor")) {
            allocator = BasePlan.ALLOC_IMMORTAL;
        }
        return allocator;
    }

    public void loadClasses(VmClassLoader loader) throws ClassNotFoundException {
        loader.loadClass("org.mmtk.vm.Plan", true);
        loader.loadClass("org.mmtk.vm.PlanConstants", true);
        loader.loadClass(MMType.class.getName(), true);
    }

    public void verifyBeforeEmit() {
        super.verifyBeforeEmit();
        Space.printVMMap();
    }

    /**
     * Call plan.alloc
     * 
     * @param bytes
     * @param align
     * @param offset
     * @param allocator
     * @return the start address of the allocated memory region
     */
    protected abstract Address alloc(int bytes, int align, int offset,
            int allocator);

    /**
     * Call plan.postAlloc.
     * 
     * @param object
     * @param typeRef
     * @param bytes
     * @param allocator
     */
    protected abstract void postAlloc(ObjectReference object,
            ObjectReference typeRef, int bytes, int allocator);

    /**
     * Call Plan.checkAllocator.
     * 
     * @param bytes
     * @param align
     * @param allocator
     */
    protected abstract int checkAllocator(int bytes, int align, int allocator);

    /**
     * Call the boot method of the current plan.
     */
    protected abstract void bootPlan();
}
