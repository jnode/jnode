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

import org.jnode.vm.Unsafe;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.classmgr.TypeSizeInfo;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.compiler.IMTCompiler;
import org.jnode.vm.x86.compiler.X86IMTCompiler32;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Word;

/**
 * Architecture description for the x86 (32-bit) architecture.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmX86Architecture32 extends VmX86Architecture {

    /** Size of an object reference */
    public static final int SLOT_SIZE = 4;

    /** The IMT compiler */
    private final X86IMTCompiler32 imtCompiler;
    
    /** The type size information */
    private final TypeSizeInfo typeSizeInfo;

    /** The next physical page address to be mmaped */
    private Word pageCursor;
    
    /** Default page entry flags */
    private final static int PF_DEFAULT = PF_PRESENT | PF_WRITE | PF_USER | PF_PSE;
    
    /**
     * Initialize this instance.
     */
    public VmX86Architecture32() {
        this("L1A");
    }

    /**
     * Initialize this instance.
     * 
     * @param compiler
     */
    public VmX86Architecture32(String compiler) {
        super(compiler);
        this.imtCompiler = new X86IMTCompiler32();
        this.typeSizeInfo = new TypeSizeInfo(1, 1, 2, 2, 1);
    }

    /**
     * Create a processor instance for this architecture.
     * 
     * @return The processor
     */
    public VmProcessor createProcessor(int id, VmSharedStatics sharedStatics, VmIsolatedStatics isolatedStatics) {
        return new VmX86Processor32(id, this, sharedStatics, isolatedStatics, null);
    }

    /**
     * Gets the compiler of IMT's.
     * 
     * @return The IMT compiler
     */
    public final IMTCompiler getIMTCompiler() {
        return imtCompiler;
    }

    /**
     * Gets the size in bytes of an object reference.
     * 
     * @return Size of reference, always 4 here
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
        
        start = pageAlign(start.toWord(), false).toAddress();
        size = pageAlign(size.toWord(), true).toExtent();
        
        if (pageCursor.isZero()) {
            Unsafe.debug("pageCursor is zero");
        }
        
        final Word pageSize = Word.fromIntZeroExtend(getPageSize());
        while (!size.isZero()) {
            mapPage(start);
            start = start.add(pageSize);
            size = size.sub(pageSize);
        }
        
        return true;
    }
    
    /**
     * Map a page at the given virtual address.
     * @param vmAddress
     */
    private final void mapPage(Address vmAddress) {        
        // Setup the pdir structures
        final Word pdirIdx = vmAddress.toWord().rshl(22);
        final Address pdirEntryPtr = UnsafeX86.getCR3().add(pdirIdx.lsh(2));
        Word entry = pdirEntryPtr.loadWord();
        if (entry.and(Word.fromIntZeroExtend(PF_PRESENT)).isZero()) {
            // Get a free page
            final Word pagePtr = pageCursor;
            pageCursor = pageCursor.add(getPageSize());

            // There is currently no present page, so do the mapping
            entry = pagePtr.or(Word.fromIntZeroExtend(PF_DEFAULT));
            pdirEntryPtr.store(entry);                
        }
    }

    /**
     * @see org.jnode.vm.VmArchitecture#boot()
     */
    protected void boot(boolean emptyMMap) {
        Unsafe.debug("VmArchitecture32#boot\n");
        dumpMultibootMMap();
        pageCursor = getFirstAvailableHeapPage();
        
        if (emptyMMap) {
            // Remove all page mappings between AVAILABLE_START-END
            final Word psize = Word.fromIntZeroExtend(getPageSize());
            final Word start = Word.fromIntZeroExtend(AVAILABLE_START);
            final Word end = Word.fromIntZeroExtend(AVAILABLE_END);
            final Address pdir = UnsafeX86.getCR3();
            
            for (Word ptr = start; ptr.LT(end); ptr = ptr.add(psize)) {
                final Word pdirIdx = ptr.rshl(22);
                pdir.add(pdirIdx.lsh(2)).store(Word.zero());            
            }               
        }
    }
}
