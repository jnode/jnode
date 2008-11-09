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
 
package org.jnode.vm.memmgr.mmtk;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.system.BootLog;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmArrayClass;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmNormalClass;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.HeapStatistics;
import org.jnode.vm.memmgr.VmHeapManager;
import org.jnode.vm.memmgr.VmWriteBarrier;
import org.jnode.vm.scheduler.VmProcessor;
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

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#createProcessorHeapData(org.jnode.vm.scheduler.VmProcessor)
     */
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

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#allocObject(org.jnode.vm.classmgr.VmClassType,
     *      int)
     */
    protected Object allocObject(VmClassType< ? > vmClass, int size) {
        if (false) {
            Unsafe.debug("allocObject: ");
            Unsafe.debug(vmClass.getName());
            Unsafe.debug('\n');
        }
        if (false) {
            VmProcessor.current().getArchitecture().getStackReader()
                    .debugStackTrace();
        }

        final int align = ObjectLayout.OBJECT_ALIGN;
        final Word headerSize = Word.fromIntZeroExtend(this.headerSize);
        final Offset tibOffset = Offset.fromIntSignExtend(this.tibOffset);
        final Offset flagsOffset = Offset.fromIntSignExtend(this.flagsOffset);
        int allocator = BasePlan.ALLOC_DEFAULT;

        final int refSize = Vm.getArch().getReferenceSize();
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

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#dumpStatistics(java.io.PrintStream)
     */
    public void dumpStatistics(PrintWriter out) {
        // Default behavior is to do nothing
    }

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#gc()
     */
    public void gc() {
        // Default behavior is to do nothing
    }

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#getFreeMemory()
     */
    public long getFreeMemory() {
        return BasePlan.freeMemory().toLong();
    }

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#getHeapStatistics()
     */
    public HeapStatistics getHeapStatistics() {
        return null;
    }

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#getTotalMemory()
     */
    public long getTotalMemory() {
        return BasePlan.totalMemory().toLong();
    }

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#initialize()
     */
    protected void initialize() {
        Unsafe.debug("MmtkHeapManager#initialize\n");
        if (initializing) {
            VmProcessor.current().getArchitecture().getStackReader()
                    .debugStackTrace();
            Unsafe.die("Recursive initialize");
        }
        initializing = true;
        final VmArchitecture arch = Vm.getArch();
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

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#isLowOnMemory()
     */
    public boolean isLowOnMemory() {
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#isObject(org.vmmagic.unboxed.Address)
     */
    public boolean isObject(Address ptr) {
        return Space.isMappedAddress(ptr);
    }

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#start()
     */
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
            BootLog.fatal("Cannot find resource manager", ex);
        } catch (ResourceNotFreeException ex) {
            BootLog.fatal("Cannot claim available heap region", ex);
        }

    }

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#notifyClassResolved(org.jnode.vm.classmgr.VmType)
     */
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

    /**
     * @see org.jnode.vm.memmgr.VmHeapManager#loadClasses(org.jnode.vm.classmgr.VmClassLoader)
     */
    public void loadClasses(VmClassLoader loader) throws ClassNotFoundException {
        loader.loadClass("org.mmtk.vm.Plan", true);
        loader.loadClass("org.mmtk.vm.PlanConstants", true);
        loader.loadClass(MMType.class.getName(), true);
    }

    /**
     * @see org.jnode.vm.VmSystemObject#verifyBeforeEmit()
     */
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
