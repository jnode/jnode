/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.util.NumberUtils;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmAddress;
import org.jnode.vm.VmThread;
import org.jnode.vm.memmgr.VmHeapManager;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * Thread implementation for Intel X86 processor.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmX86Thread extends VmThread {

	// State when not running
	volatile int eax;
	volatile int ebx;
	volatile int ecx;
	volatile int edx;
	volatile int esi;
	volatile int edi;
	volatile int eflags;
	volatile VmAddress eip;
	volatile VmAddress esp;
	volatile VmAddress ebp;
	
	// State upon last system exception
	volatile int exEax;
	volatile int exEbx;
	volatile int exEcx;
	volatile int exEdx;
	volatile int exEsi;
	volatile int exEdi;
	volatile int exEflags;
	volatile int exEip;
	volatile int exEsp;
	volatile int exEbp;
	volatile int exCr2;
	
	/**
	 * Initialize this instance 
	 */
	VmX86Thread() {
		super();
	}

    /**
     * Create a new instance. This constructor can only be called during the
     * bootstrap phase.
     */
    VmX86Thread(byte[] stack) {
        super(stack, getStackEnd(stack, stack.length), stack.length);
    }
    
	/**
	 * @param javaThread
	 */
	public VmX86Thread(Thread javaThread) {
		super(javaThread);
	}

	
	/**
	 * Gets the most current stackframe of this thread.
	 * @return Stackframe 
	 */
	protected VmAddress getStackFrame() {
		return ebp;
	}
	
	/**
	 * Gets the most current instruction pointer of this thread.
	 * This method is only valid when this thread is not running.
	 * @return IP 
	 */
	protected VmAddress getInstructionPointer() {
		return eip;
	}

    /**
     * Gets the stackframe of the last system exception of this thread. 
     */
    protected VmAddress getExceptionStackFrame() {
        return VmAddress.valueOf(exEbp);
    }

    /**
     * Gets the instruction pointer of the last system exception of this thread. 
     */
    protected VmAddress getExceptionInstructionPointer() {
        return VmAddress.valueOf(exEip);
    }

	/**
	 * Calculate the end of the stack. 
	 * @param stack
	 * @param stackSize
	 * @return End address of the stack
	 */
	protected VmAddress getStackEnd(Object stack, int stackSize) {
		return VmAddress.add(VmAddress.valueOf(stack), STACK_OVERFLOW_LIMIT);
	}
	
	/**
	 * Calculate the end of the stack. 
	 * @param stack
	 * @param stackSize
	 * @return End address of the stack
	 */
	private static VmAddress getStackEnd(byte[] stack, int stackSize) {
		return VmAddress.add(VmAddress.addressOfArrayData(stack), STACK_OVERFLOW_LIMIT);
	}
	
	/**
	 * Gets a human readable representation of the system exception state.
	 * @return String
	 */
	public String getReadableErrorState() {
		return "EAX " +NumberUtils.hex(exEax) +
			" EBX " +NumberUtils.hex(exEbx) +
			" ECX " +NumberUtils.hex(exEcx) +
			" EDX " +NumberUtils.hex(exEdx) +
			" ESI " +NumberUtils.hex(exEsi) +
			" EDI " +NumberUtils.hex(exEdi) +
			" ESP " +NumberUtils.hex(exEsp) +
			" EIP " +NumberUtils.hex(exEip) +
			" CR2 " +NumberUtils.hex(exCr2) +
			" EFLAGS " +NumberUtils.hex(exEflags);
	}

    /**
     * Visit all objects on the stack and register state of this thread.
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
                stackEnd = Address.fromAddress(esp);
            }
            
            Address ptr = stackTop;
            final int slotSize = VmX86Architecture.SLOT_SIZE;
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
        Address addr = Address.fromIntZeroExtend(eax);
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = Address.fromIntZeroExtend(ebx);
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = Address.fromIntZeroExtend(ecx);
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = Address.fromIntZeroExtend(edx);
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = Address.fromIntZeroExtend(esi);
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        addr = Address.fromIntZeroExtend(edi);
        if (heapManager.isObject(addr)) {
            if (!visitor.visit(addr)) {
                return false;
            }
        }
        return true;
    }
}
