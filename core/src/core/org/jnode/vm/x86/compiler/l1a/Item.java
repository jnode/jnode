/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.Register;
import org.jnode.util.Counter;
import org.jnode.vm.Vm;

/**
 * This class is the base of all virtual stack items. To improve performance and
 * avoid type casts, all accessor methods are defined here (as abstract),
 * subclasses shall only implement those that make sense to them and throw an
 * exception otherwise (i.e. a reference item cannot implement load64 in any
 * meaningful way). The compiler knows about the various types and must access
 * only the legal methods.
 * 
 * @author Patrik Reali
 * 
 * TODO: make Item a subclass of operand
 */
abstract class Item {

	/**
	 * Description of the virtual stack entry kind - STACK: the item is on the
	 * stack - REGISTER: a register contains the item - FREGISTER: a fpu
	 * register contains the item - LOCAL: a local variable contains the item -
	 * CONSTANT: the item is a constant
	 * 
	 * An item has only one kind; flags are used for masking purposes (detect
	 * multiple kinds in one operation)
	 */
	static class Kind {

		static final int STACK = 0x1;

		static final int REGISTER = 0x2;

		static final int FPUSTACK = 0x4;

		static final int LOCAL = 0x8;

		static final int CONSTANT = 0x10;

		public static final String toString(int kind) {
			switch (kind) {
			case STACK:
				return "STACK";
			case REGISTER:
				return "REG";
			case FPUSTACK:
				return "FPU";
			case LOCAL:
				return "LOCAL";
			case CONSTANT:
				return "CONST";
			default:
				return String.valueOf(kind);
			}
		}
	}

	/*
	 * Virtual Stack Item
	 */
	protected int kind; // entry kind

	protected int offsetToFP; // kind == local only

	protected final ItemFactory factory;
	
	/**
	 * Initialize a blank item.
	 */
	protected Item(ItemFactory factory) {
	    this.factory = factory;
		if (true) {
		    final Vm vm = Vm.getVm();
		    final String name = getClass().getName();
		    final Counter cnt = vm.getCounter(name);
		    cnt.inc();
		}
	}

	/**
	 * Initialize this instance.
	 * 
	 * @param kind
	 * @param offsetToFP
	 */
	protected final void initialize(int kind, int offsetToFP) {
		assertCondition(kind > 0, "Invalid kind");
		this.kind = kind;
		this.offsetToFP = offsetToFP;		
	}

	/**
	 * Throw a not implemented error.
	 */
	static void notImplemented() {
		throw new Error("NotImplemented");
	}

	/**
	 * Assert.
	 * 
	 * @param cond
	 */
	static void assertCondition(boolean cond, String message) {
		if (!cond) {
			throw new Error("Assertion failure: " + message);
		}
	}

	/**
	 * Get the JVM type of this item
	 * 
	 * @return the JVM type
	 */
	abstract int getType();

	/**
	 * Get the item kind (STACK, REGISTER, ....)
	 * 
	 * @return the item kind
	 */
	final int getKind() {
		return kind;
	}

	final boolean isStack() {
		return (kind == Kind.STACK);
	}

	final boolean isRegister() {
		return (kind == Kind.REGISTER);
	}

	final boolean isFPUStack() {
		return (kind == Kind.FPUSTACK);
	}

	final boolean isLocal() {
		return (kind == Kind.LOCAL);
	}

	final boolean isConstant() {
		return (kind == Kind.CONSTANT);
	}

	/**
	 * Return the current item's computational type category (JVM Spec, p. 83).
	 * In practice, this is the number of double words needed by the item (1 or
	 * 2)
	 * 
	 * @return computational type category
	 */
	int getCategory() {
		return 1;
	}

	/**
	 * Gets the offset from this item to the FramePointer register. This is only
	 * valid if this item has a LOCAL kind.
	 * 
	 * @return
	 */
	int getOffsetToFP() {
		assertCondition(kind == Kind.LOCAL, "kind == Kind.LOCAL");
		return offsetToFP;
	}

	/**
	 * Is this item located at the given FP offset.
	 * 
	 * @return
	 */
	boolean isAtOffset(int offset) {
		assertCondition(kind == Kind.LOCAL, "kind == Kind.LOCAL");
		return (offsetToFP == offset);
	}

	/**
	 * Load item into a register / two registers / an FPU register depending on
	 * its type.
	 * 
	 * @param ec
	 *            the EmitterContext
	 */
	abstract void load(EmitterContext ec);

	/**
	 * Load this item to a general purpose register(s).
	 * 
	 * @param ec
	 */
	abstract void loadToGPR(EmitterContext ec);

	/**
	 * Load item into a register / two registers / an FPU register depending on
	 * its type, if its kind matches the mask
	 */
	final void loadIf(EmitterContext eContext, int mask) {
		if ((kind & mask) > 0)
			load(eContext);
	}

	/**
	 * Clone item
	 * 
	 * @param ec
	 *            the EmitterContext
	 * @return a clone of the item
	 */
	abstract Item clone(EmitterContext ec);

	/**
	 * Push item onto the stack
	 * 
	 * @param ec
	 *            the EmitterContext
	 */
	abstract void push(EmitterContext ec);

	/**
	 * Push the value of this item on the FPU stack. The item itself is not
	 * changed in any way.
	 */
	abstract void pushToFPU(EmitterContext ec);

	/**
	 * Release the registers associated to this item
	 * 
	 * @param ec
	 *            the EmitterContext
	 */
	abstract void release(EmitterContext ec);

	/**
	 * Is this obsolete?
	 * 
	 * @param ec
	 */
	final void release1(EmitterContext ec) {
		if (VirtualStack.checkOperandStack) {
			VirtualStack vs = ec.getVStack();
			vs.operandStack.pop(this);
		}
		release(ec);
	}

	/**
	 * Spill the registers associated to this item
	 * 
	 * @param ec
	 *            the EmitterContext
	 * @param reg
	 *            the register to be spilled
	 */
	abstract void spill(EmitterContext ec, Register reg);

	/**
	 * Spill this item if it uses the given register.
	 */
	final void spillIfUsing(EmitterContext ec, Register reg) {
		if (uses(reg)) {
			spill(ec, reg);
		}
	}

	/**
	 * enquire whether the item uses this register
	 * 
	 * @param reg
	 * @return true, when reg is used by this item
	 */
	abstract boolean uses(Register reg);

	/**
	 * enquire whether the item uses a volatile register
	 * 
	 * @param reg
	 * @return true, when this item uses a volatile register.
	 */
	abstract boolean usesVolatileRegister(X86RegisterPool pool);

	public String toString() {
		return getType() + "," + getKind() + " ("
				+ System.identityHashCode(this) + ")";
	}
}