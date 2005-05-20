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
 
package org.jnode.vm;

import java.nio.ByteOrder;

import org.jnode.system.ResourceManager;
import org.jnode.vm.classmgr.TypeSizeInfo;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.compiler.IMTCompiler;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Word;

/**
 * Class describing a specific system architecture.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmArchitecture extends VmSystemObject {

    public enum Space {
        /** Total space that can contain objects */
        HEAP,
        /** Space available to the memory manager */
        AVAILABLE,
        /** Space available to devices */
        DEVICE,
        /** Space the contains the bootimage */
        BOOTIMAGE,
        /** Space the contains the initial jar */
        INITJAR        
    }
    
	/**
	 * Gets the name of this architecture.
	 * This name is the programmers name used to identify packages,
	 * class name extensions etc.
	 * 
	 * @return Name
	 */
	public abstract String getName();

	/**
	 * Gets the full name of this architecture, including operating mode.
	 * 
	 * @return Name
	 */
	public abstract String getFullName();

	/**
	 * Gets the byte ordering of this architecture.
	 * @return ByteOrder
	 */
	public abstract ByteOrder getByteOrder();
	
	/**
	 * Gets the size in bytes of an object reference.
	 * 
	 * @return Reference size
	 */
	public abstract int getReferenceSize();
    
    /**
     * Gets the log base two of the size of an OS page
     * @return
     */
    public abstract byte getLogPageSize()
    throws UninterruptiblePragma;
    
    /**
     * Gets the log base two of the size of an OS page
     * @return
     */
    public final int getPageSize() 
    throws UninterruptiblePragma {
        return 1 << getLogPageSize();
    }
    
    /**
     * Gets the type size information of this architecture.
     * @return
     */
    public abstract TypeSizeInfo getTypeSizeInfo();

	/**
	 * Gets the stackreader for this architecture.
	 * 
	 * @return Stack reader
	 */
	public abstract VmStackReader getStackReader();

	/**
	 * Gets all compilers for this architecture.
	 * 
	 * @return The compilers, sorted by optimization level, from least optimizations to most
	 *         optimizations.
	 */
	public abstract NativeCodeCompiler[] getCompilers();

	/**
	 * Gets all test compilers for this architecture.
	 * This can be used to test new compilers in a running system.
	 * 
	 * @return The compilers, sorted by optimization level, from least optimizations to most
	 *         optimizations, or null for no test compilers.
	 */
	public abstract NativeCodeCompiler[] getTestCompilers();

	/**
	 * Gets the compiler of IMT's.
	 * @return
	 */
	public abstract IMTCompiler getIMTCompiler();
	
    /** 
     * Called early on in the boot process (before the initializating of 
     * the memory manager) to initialize any architecture specific variables.
     * Do not allocate memory here.
     * @param emptyMmap If true, all page mappings in the AVAILABLE region
     * are removed.
     */
    protected abstract void boot(boolean emptyMmap);
    
	/**
	 * Find and start all processors in the system.
	 * All all discovered processors to the given list.
	 * The bootstrap processor is already on the given list.
	 */
	protected abstract void initializeProcessors(ResourceManager rm);
	
	/**
	 * Call this method to register a processor found in {@link #initializeProcessors(ResourceManager)}.
	 * @param cpu
	 */
	protected final void addProcessor(VmProcessor cpu) {
	    Vm.getVm().addProcessor(cpu);
	}

	/**
	 * Create a processor instance for this architecture.
	 * 
	 * @return The processor
	 */
	protected abstract VmProcessor createProcessor(int id, VmSharedStatics sharedStatics, VmIsolatedStatics isolatedStatics);

    /**
     * Gets the start address of the given space.
     * @return
     */
    public Address getStart(Space space) {
        switch (space) {
        case BOOTIMAGE: return Unsafe.getKernelStart();
        case INITJAR: return Unsafe.getInitJarStart();
        default: throw new IllegalArgumentException("Unknown space " + space);
        }
    }

    /**
     * Gets the start address of the given space.
     * @return
     */
    public Address getEnd(Space space) {
        switch (space) {
        case BOOTIMAGE: return Unsafe.getBootHeapEnd();
        case INITJAR: return Unsafe.getInitJarEnd();
        default: throw new IllegalArgumentException("Unknown space " + space);
        }
    }
    
    /**
     * Gets the physical address of the first page available
     * for mmap.
     * @return
     */
    protected final Word getFirstAvailableHeapPage() {
        return pageAlign(Unsafe.getMemoryStart().toWord(), true);
    }
    
    /**
     * Page align a given value.
     * @param v
     * @param up If true, the value will be rounded up, otherwise rounded down.
     * @return
     */
    protected final Word pageAlign(Word v, boolean up) {
        final int logPageSize = getLogPageSize();
        if (up) {
            v = v.add((1 << logPageSize) - 1);
        }
        return v.rshl(logPageSize).lsh(logPageSize);
    }
    
    /**
     * Map a region of the heap space. 
     * Note that you cannot allocate memory in this memory, because
     * it is used very early in the boot process.
     * 
     * @param space
     * @param start
     * @param size
     * @return true for success, false otherwise.
     */
    public abstract boolean mmap(Space space, Address start, Extent size)
    throws UninterruptiblePragma;
}
