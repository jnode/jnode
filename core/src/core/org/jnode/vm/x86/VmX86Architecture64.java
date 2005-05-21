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

import org.jnode.vm.VmProcessor;
import org.jnode.vm.classmgr.TypeSizeInfo;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.compiler.IMTCompiler;
import org.jnode.vm.x86.compiler.X86IMTCompiler64;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmX86Architecture64 extends VmX86Architecture {

	/** Size of an object reference */
	public static final int SLOT_SIZE = 8;

    /** The IMT compiler */
    private final X86IMTCompiler64 imtCompiler;
    
    /** The type size information */
    private final TypeSizeInfo typeSizeInfo;

	/**
	 * Initialize this instance.
	 */
	public VmX86Architecture64() {
		this("L1A");
	}

	/**
	 * Initialize this instance.
	 * 
	 * @param compiler
	 */
	public VmX86Architecture64(String compiler) {
		super(compiler);
		this.imtCompiler = new X86IMTCompiler64();
        this.typeSizeInfo = new TypeSizeInfo(1, 1, 2, 2, 1);
	}

	/**
	 * @see org.jnode.vm.VmArchitecture#createProcessor(int,
	 *      org.jnode.vm.classmgr.VmStatics)
	 */
	public final VmProcessor createProcessor(int id, VmSharedStatics sharedStatics, VmIsolatedStatics isolatedStatics) {
		return new VmX86Processor64(id, this, sharedStatics, isolatedStatics, null);
	}

	/**
	 * @see org.jnode.vm.VmArchitecture#getIMTCompiler()
	 */
	public final IMTCompiler getIMTCompiler() {
		return imtCompiler;
	}

	/**
	 * @see org.jnode.vm.VmArchitecture#getReferenceSize()
	 */
	public final int getReferenceSize() {
		return SLOT_SIZE;
	}

    /**
     * Gets the type size information of this architecture.
     * 
     * @return
     */
    public final TypeSizeInfo getTypeSizeInfo() {
        return typeSizeInfo;
    }

    /**
     * @see org.jnode.vm.VmArchitecture#getLogPageSize()
     */
    public final byte getLogPageSize() {
        return 22; // 4Mb
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
    public final boolean mmap(Space space, Address start, Extent size)
    throws UninterruptiblePragma {
        if (space != Space.HEAP) {
            return false;
        }
        
        return false;
    }

    /**
     * @see org.jnode.vm.VmArchitecture#boot()
     */
    protected void boot(boolean emptyMMap) {
        dumpMultibootMMap();
    }
}
