/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.Register;


/**
 * This class is the base of all virtual stack items.
 * To improve performance and avoid type casts,
 * all accessor methods are defined here (as abstract),
 * subclasses shall only implement those that make sense to 
 * them and throw an exception otherwise
 * (i.e. a reference item cannot implement load64 in any
 * meaningful way). The compiler knows about the various
 * types and must access only the legal methods.
 * 
 * @author Patrik Reali
 *
 */

//TODO: make Item a subclass of operand
abstract class Item {
	/*
	 * Description of the virtual stack entry kind
	 * - STACK: the item is on the stack
	 * - REGISTER: a register contains the item
	 * - FREGISTER: a fpu register contains the item
	 * - LOCAL: a local variable contains the item
	 * - CONSTANT: the item is a constant
	 * 
	 * An item has only one kind; flags are used for masking purposes
	 * (detect multiple kinds in one operation)
	 */
	static final int STACK = 0x1;
	static final int REGISTER = 0x2;
	static final int FREGISTER = 0x4;
	static final int LOCAL = 0x8;
	static final int CONSTANT = 0x10;
	
	/*
	 * JVM types
	 */
	static final int UNKNOWN = 0;
	static final int BYTE = 1;
	static final int SHORT = 2;
	static final int CHAR = 3;
	static final int INT = 4;
	static final int LONG = 5;
	static final int FLOAT = 6;
	static final int DOUBLE = 7;
	static final int REFERENCE = 8;

	/*
	 * Virtual Stack Item
	 */
	protected int kind;			// entry kind
	private int type;			// entry type
	
	int	offsetToFP;		// kind == local only
	
	Item(int kind, int type, int offsetToFP) {
		this.kind = kind;
		this.type = type;
		this.offsetToFP = offsetToFP;
	}
	
	static void notImplemented() {
		throw new Error("NotImplemented");
	}
	
	static void myAssert(boolean cond) {
		if (!cond) {
			throw new Error("assertion failure");
		}
	}
	
	/**
	 * Get the JVM type of this item
	 * @return the JVM type
	 */
	int getType() {
		return type;
	}
	
	/**
	 * Get the item kind (STACK, REGISTER, ....)
	 * @return the item kind
	 */
	int getKind() {
		return kind;
	}
	
	/**
	 * Return the current item's computational type category
	 * (JVM Spec, p. 83). In practice, this is the number of
	 * double words needed by the item (1 or 2)
         *
	 * @return computational type category
	 */
	int getCategory() {
		if ((kind == LONG)||(kind == DOUBLE))
			return 2;
		else
			return 1;
	}
	
	int getOffsetToFP() {
		myAssert(kind == LOCAL);
		return offsetToFP;
	}

	/**
	 * Load item into a register / two registers / an FPU register
	 * depending on its type
	 *
	 * @param ec the EmitterContext
	 */
	abstract void load(EmitterContext ec);
	
	/**
	 * Load item into the given register (only for Category 1 items).
	 * Also allocate the register t0.
	 * 
	 * @param t0 the destination register
	 */
//	abstract void loadTo(Register t0);
	
	/**
	 * Load item into the given registers (only for Category 2 items)
	 * 
	 * @param t0 MSB
	 * @param t1 LSB
	 */
//	abstract void loadTo(Register t0, Register t1);
	
	/**
	 * Load item into ST(0) FPU register (push on FPU stack)
	 *
	 */
//	abstract void loadToFPU(EmitterContext ec);
	
	/**
	 * Load item into a register / two registers / an FPU register
	 * depending on its type, if its kind matches the mask
	 */
	void loadIf(EmitterContext eContext, int mask) {
		if ((kind & mask) > 0)
			load(eContext);
	}
	
	/**
	 * Load item into the given register (only for Category 1 items), if its kind
	 * matches the mask.
	 * Also allocate the register t0.
	 * 
	 * @param t0 the destination register
	 */
//	void loadToIf(int mask, Register t0) {
//		if ((kind & mask) > 0)
//			loadTo(t0);
//	}
	
	/**
	 * Load item into the given registers (only for Category 2 items), if its kind
	 * matches the mask.
	 * Allocates t0 and t1
	 * 
	 * @param t0 MSB
	 * @param t1 LSB
	 */
//	void loadToIf(int mask, Register t0, Register t1) {
//		if ((kind & mask) > 0)
//			loadTo(t0,t1);
//	}

	/**
	 * Load item into ST(0) FPU register (push on FPU stack), if its
	 * kind matches the mask
	 *
	 */
//	void loadToFPUIf(EmitterContext ec, int mask) {
//		if ((kind & mask) > 0)
//			loadToFPU(ec);
//	}
	
	/**
	 * Clone item 
	 *
	 * @param ec the EmitterContext
	 * @return a clone of the item
	 */
	abstract Item clone(EmitterContext ec);
	
	/**
	 * Push item onto the stack
	 *
	 * @param ec the EmitterContext
	 */
	abstract void push(EmitterContext ec);
	
	/**
	 * Release the registers associated to this item
	 *
	 * @param ec the EmitterContext
	 */
	abstract void release(EmitterContext ec);

	/**
	 * enquire whether the item uses this register
	 * @param reg
	 * @return true, when reg is used by this item
	 */
	abstract boolean uses(Register reg);

	/**
	 * @param type
	 * @return the internal type value
	 */
	 static int SignatureToType(char type) {
		int res;
		switch (type) {
			case 'Z' : // Boolean
			case 'B' : // Byte
			case 'C' : // Character
			case 'S' : // Short
			case 'I' : // Integer
				res = INT;
				break;
			case 'F' : // Float
				res = FLOAT;
				break;
			case 'L' : // Object
			case ';' : // Object
			case '[' : // Array
				res = REFERENCE;
				break;
			case 'J' : // Long
				res = LONG;
				break;
			case 'D' : // Double
				res = DOUBLE;
				break;
			default :
				throw new IllegalArgumentException("Unknown type"+type);
		}
		return res;
	}

}
