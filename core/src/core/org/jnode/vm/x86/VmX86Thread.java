/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.util.NumberUtils;
import org.jnode.vm.Address;
import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmThread;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.VmHeapManager;

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
	volatile Address eip;
	volatile Address esp;
	volatile Address ebp;
	
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
	protected Address getStackFrame() {
		return ebp;
	}
	
	/**
	 * Gets the most current instruction pointer of this thread.
	 * This method is only valid when this thread is not running.
	 * @return IP 
	 */
	protected Address getInstructionPointer() {
		return eip;
	}

    /**
     * Gets the stackframe of the last system exception of this thread. 
     */
    protected Address getExceptionStackFrame() {
        return Address.valueOf(exEbp);
    }

    /**
     * Gets the instruction pointer of the last system exception of this thread. 
     */
    protected Address getExceptionInstructionPointer() {
        return Address.valueOf(exEip);
    }

	/**
	 * Calculate the end of the stack. 
	 * @param stack
	 * @param stackSize
	 * @return End address of the stack
	 */
	protected Address getStackEnd(Object stack, int stackSize) {
		return Address.add(Address.valueOf(stack), STACK_OVERFLOW_LIMIT);
	}
	
	/**
	 * Calculate the end of the stack. 
	 * @param stack
	 * @param stackSize
	 * @return End address of the stack
	 */
	private static Address getStackEnd(byte[] stack, int stackSize) {
		return Address.add(Address.addressOfArrayData(stack), STACK_OVERFLOW_LIMIT);
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
    public void visit(ObjectVisitor visitor, VmHeapManager heapManager, HeapHelper helper) {
        // For now do it stupid, but safe, just scan the whole stack.
        final int stackSize = getStackSize();
        final Object stack = getStack();
        if (stack != null) {
            final int slotSize = VmX86Architecture.SLOT_SIZE;
            for (int i = 0; i < stackSize; i += slotSize) {
                final Address child = helper.getAddress(stack, i);
                if (child != null) {
                    if (heapManager.isObject(child)) {
                        visitor.visit(child);
                    }
                }
            }
        }
        // Scan registers
        Address addr = Address.valueOf(eax);
        if (heapManager.isObject(addr)) {
            visitor.visit(addr);
        }
        addr = Address.valueOf(ebx);
        if (heapManager.isObject(addr)) {
            visitor.visit(addr);
        }
        addr = Address.valueOf(ecx);
        if (heapManager.isObject(addr)) {
            visitor.visit(addr);
        }
        addr = Address.valueOf(edx);
        if (heapManager.isObject(addr)) {
            visitor.visit(addr);
        }
        addr = Address.valueOf(esi);
        if (heapManager.isObject(addr)) {
            visitor.visit(addr);
        }
        addr = Address.valueOf(edi);
        if (heapManager.isObject(addr)) {
            visitor.visit(addr);
        }
    }
}
