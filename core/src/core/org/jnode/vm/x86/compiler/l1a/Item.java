/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;


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
	private int kind;			// entry kind
	private int type;			// entry type
//	Register reg;	// kind == REGISTER only
	int	local;		// kind == local only
//	Constant con;	// kind == CONSTANT only
	
//	Item(int kind, int type, Register reg, int local, Constant con) {
	Item(int kind, int type, int local) {
		this.kind = kind;
		this.type = type;
//		this.reg = reg;
		this.local = local;
//		this.con = con;
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
	 * @return computational type category
	 */
	int getCategory() {
		if ((kind == LONG)||(kind == DOUBLE))
			return 2;
		else
			return 1;
	}
	
//	Register getRegister() {
//		myAssert (kind == REGISTER);
//		return reg;
//	}
//	
//	Constant getConstant() {
//		myAssert (kind == CONSTANT);
//		return con;
//	}
	
	int getLocal() {
		myAssert(kind == LOCAL);
		return local;
	}

	/**
	 * Load item into a register / two registers / an FPU register
	 * depending on its type
	 */
	abstract void load();
	
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
	abstract void loadToFPU();
	
	/**
	 * Load item into a register / two registers / an FPU register
	 * depending on its type, if its kind matches the mask
	 */
	void loadIf(int mask) {
		if ((kind & mask) > 0)
			load();
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
	void loadToFPUIf(int mask) {
		if ((kind & mask) > 0)
			loadToFPU();
	}
	
	/**
	 * return the register containing a 32 bit value
	 * (Register kind only, Int and Reference types only)
	 * 
	 * @return Register
	 */
//	abstract Register getRegister();
	
	/**
	 * Return the register containing the LSB of a 64 bit value
	 *
	 * @return Register with LSB value
	 */
//	abstract Register getLSBRegister();
	
	/**
	 * Return the register containing the MSB of a 64 bit value
	 *
	 * @return Register with MSB value
	 */
//	abstract Register getMSBRegister();
	
//	abstract int getIntValue();
//	abstract long getLongValue();
//	abstract float getFloatValue();
//	abstract double getDoubleValue();
//	abstract Object getObjectValue();
	
	/**
	 * Push item onto the stack
	 *
	 */
	abstract void push();
	
	/**
	 * Release the registers associated to this item
	 *
	 */
	abstract void release();
	
	/**
	 * @param type
	 * @return
	 */
	 static int SignatureToType(char type) {
		switch (type) {
			case 'Z' : // Boolean
			case 'B' : // Byte
			case 'C' : // Character
			case 'S' : // Short
			case 'I' : // Integer
				return INT;
			case 'F' : // Float
				return FLOAT;
			case 'L' : // Object
			case '[' : // Array
				return REFERENCE;
			case 'J' : // Long
				return LONG;
			case 'D' : // Double
				return DOUBLE;
			default :
				throw new IllegalArgumentException("Unknown type");
		}
	}
}
