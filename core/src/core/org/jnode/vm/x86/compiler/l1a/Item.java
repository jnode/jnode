/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.Register;

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

        static final int FREGISTER = 0x4;

        static final int LOCAL = 0x8;

        static final int CONSTANT = 0x10;
        
        static final int RELEASED = 0x8000;
    }

    /**
     * JVM types
     */
    static class JvmType {

        static final int UNKNOWN = 0;

        static final int BYTE = 1;

        static final int SHORT = 2;

        static final int CHAR = 3;

        static final int INT = 4;

        static final int LONG = 5;

        static final int FLOAT = 6;

        static final int DOUBLE = 7;

        static final int REFERENCE = 8;
    }

    /*
     * Virtual Stack Item
     */
    protected int kind; // entry kind

    int offsetToFP; // kind == local only

    /**
     * Initialize this instance.
     * @param kind
     * @param offsetToFP
     */
    Item(int kind, int offsetToFP) {
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
     * @param cond
     */
    static void myAssert(boolean cond) {
        if (!cond) { throw new Error("assertion failure"); }
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
    final int getOffsetToFP() {
        myAssert(kind == Kind.LOCAL);
        return offsetToFP;
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
     * Load item into a register / two registers / an FPU register depending on
     * its type, if its kind matches the mask
     */
    final void loadIf(EmitterContext eContext, int mask) {
        if ((kind & mask) > 0) load(eContext);
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
     * Release the registers associated to this item
     * 
     * @param ec
     *            the EmitterContext
     */
    abstract void release(EmitterContext ec);

    /**
     * Is this obsolete?
     * @param ec
     */
    final void release1(EmitterContext ec) {
        if (VirtualStack.checkOperandStack) {
            VirtualStack vs = ec.getVStack();
            vs.popFromOperandStack(this);
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
     * @param type
     * @return the internal type value
     */
    static int SignatureToType(char type) {
        int res;
        switch (type) {
        case 'Z':
        // Boolean
        case 'B':
        // Byte
        case 'C':
        // Character
        case 'S':
        // Short
        case 'I':
            // Integer
            res = JvmType.INT;
            break;
        case 'F':
            // Float
            res = JvmType.FLOAT;
            break;
        case 'L':
        // Object
        case ';':
        // Object
        case '[':
            // Array
            res = JvmType.REFERENCE;
            break;
        case 'J':
            // Long
            res = JvmType.LONG;
            break;
        case 'D':
            // Double
            res = JvmType.DOUBLE;
            break;
        default:
            throw new IllegalArgumentException("Unknown type" + type);
        }
        return res;
    }

}