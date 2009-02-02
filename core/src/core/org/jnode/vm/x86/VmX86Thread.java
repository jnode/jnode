/*
 * $Id$
 *
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

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmMagic;
import org.jnode.vm.annotation.Internal;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.memmgr.VmHeapManager;
import org.jnode.vm.scheduler.VmThread;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Word;

/**
 * Thread implementation for Intel X86 processor.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public abstract class VmX86Thread extends VmThread {

    // State when not running
    volatile Word eax;
    volatile Word ebx;
    volatile Word ecx;
    volatile Word edx;
    volatile Word esi;
    volatile Word edi;
    volatile Word eflags;
    volatile Address eip;
    volatile Address esp;
    volatile Address ebp;

    private static final int FXSTATE_SIZE = 512 + 16;
    public static final int FXF_USED = 0x01; // FX has been used since last thread switch

    // Saved state of FPU & XMM
    final byte[] fxState;
    volatile Address fxStatePtr;    // This value is set in assembler code
    volatile int fxFlags;

    // State upon last system exception
    volatile Word exEax;
    volatile Word exEbx;
    volatile Word exEcx;
    volatile Word exEdx;
    volatile Word exEsi;
    volatile Word exEdi;
    volatile Word exEflags;
    volatile Address exEip;
    volatile Address exEsp;
    volatile Address exEbp;
    volatile Word exCr2;

    // MSR's
    private volatile MSR[] readWriteMSRs;
    private volatile MSR[] writeOnlyMSRs;

    /**
     * Initialize this instance
     */
    VmX86Thread(VmIsolatedStatics isolatedStatics, int slotSize) {
        super(isolatedStatics, slotSize);
        fxState = new byte[FXSTATE_SIZE];
    }

    /**
     * Create a new instance. This constructor can only be called during the
     * bootstrap phase.
     */
    VmX86Thread(VmIsolatedStatics isolatedStatics, byte[] stack, int slotSize) {
        super(isolatedStatics, stack, getStackEnd(stack, stack.length, slotSize), stack.length);
        fxState = new byte[FXSTATE_SIZE];
    }

    /**
     * @param javaThread
     */
    public VmX86Thread(VmIsolatedStatics isolatedStatics, Thread javaThread) {
        super(isolatedStatics, javaThread);
        fxState = new byte[FXSTATE_SIZE];
    }


    /**
     * Gets the most current stackframe of this thread.
     *
     * @return Stackframe
     */
    @Internal
    public final Address getStackFrame() {
        return ebp;
    }

    /**
     * Gets the most current instruction pointer of this thread.
     * This method is only valid when this thread is not running.
     *
     * @return IP
     */
    protected Address getInstructionPointer() {
        return eip;
    }

    /**
     * Gets the stackframe of the last system exception of this thread.
     */
    protected Address getExceptionStackFrame() {
        return exEbp;
    }

    /**
     * Gets the instruction pointer of the last system exception of this thread.
     */
    protected Address getExceptionInstructionPointer() {
        return exEip;
    }

    /**
     * Calculate the end of the stack.
     *
     * @param stack
     * @param stackSize
     * @return End address of the stack
     */
    protected Address getStackEnd(Object stack, int stackSize) {
        return ObjectReference.fromObject(stack).toAddress().add(STACK_OVERFLOW_LIMIT_SLOTS * getReferenceSize());
    }

    /**
     * Calculate the end of the stack.
     *
     * @param stack
     * @param stackSize
     * @return End address of the stack
     */
    private static Address getStackEnd(byte[] stack, int stackSize, int slotSize) {
        return VmMagic.getArrayData(stack).add(STACK_OVERFLOW_LIMIT_SLOTS * slotSize);
    }

    /**
     * Gets the size of an object reference (pointer).
     */
    protected abstract int getReferenceSize();

    /**
     * Visit all objects on the stack and register state of this thread.
     *
     * @param visitor
     * @param heapManager
     */
    public boolean visit(ObjectVisitor visitor, VmHeapManager heapManager) {
        // For now do it stupid, but safe, just scan the whole stack.
        final int stackSize = getStackSize();
        final Object stack = getStack();
        if (stack != null) {
            final Address stackBottom = ObjectReference.fromObject(stack).toAddress();
            final Address stackTop = stackBottom.add(stackSize);
            final Address stackEnd;
            if (this == currentThread()) {
                stackEnd = stackBottom;
            } else {
                stackEnd = esp;
            }

            Address ptr = stackTop;
            final int slotSize = getReferenceSize();
            while (ptr.GE(stackEnd)) {
                final Address child = ptr.loadAddress();
                if (child != null) {
                    if (heapManager.isObject(child)) {
                        if (!visitor.visit(child)) {
                            return false;
                        }
                    }
                }
                ptr = ptr.sub(slotSize);
            }
        }
        // Scan registers
        Address addr = eax.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = ebx.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = ecx.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = edx.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = esi.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = edi.toAddress();
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets the MSR arrays.
     *
     * @param readWriteMSRs
     * @param writeOnlyMSRs
     * @throws UninterruptiblePragma
     */
    public final void setMSRs(MSR[] readWriteMSRs, MSR[] writeOnlyMSRs)
        throws UninterruptiblePragma {
        this.readWriteMSRs = readWriteMSRs;
        this.writeOnlyMSRs = writeOnlyMSRs;
    }

    /**
     * @return Returns the readWriteMSRs.
     */
    public final MSR[] getReadWriteMSRs() {
        return readWriteMSRs;
    }

    /**
     * @return Returns the writeOnlyMSRs.
     */
    public final MSR[] getWriteOnlyMSRs() {
        return writeOnlyMSRs;
    }

    /**
     * Gets the end address of the stack of this thread.
     *
     * @return
     */
    final Address getStackEnd() {
        return stackEnd;
    }

    //todo protect or hide this method in the future
    /**
     * Gets the stack pointer of this thread.
     * Valid only when the thread is not running.
     *
     * @return
     */
    public final Address getStackPointer() {
        return esp;
    }
}
