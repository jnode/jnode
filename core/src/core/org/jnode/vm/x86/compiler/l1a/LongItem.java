/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.JvmType;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Patrik Reali
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
final class LongItem extends DoubleWordItem implements X86CompilerConstants {

    private long value;

    /**
     * Initialize a blank item.
     */
    LongItem(ItemFactory factory) {
        super(factory);
    }

    /**
     * @param kind
     * @param offsetToFP
     * @param lsb
     * @param msb
     * @param val
     */
    final void initialize(int kind, int offsetToFP, Register lsb, Register msb,
            long val) {
        super.initialize(kind, offsetToFP, lsb, msb);
        this.value = val;
    }

    /**
     * Load my constant to the given os.
     * 
     * @param os
     * @param lsb
     * @param msb
     */
    protected final void loadToConstant(EmitterContext ec,
            AbstractX86Stream os, Register lsb, Register msb) {
        final int lsbv = (int) (value & 0xFFFFFFFFL);
        final int msbv = (int) ((value >>> 32) & 0xFFFFFFFFL);

        os.writeMOV_Const(lsb, lsbv);
        os.writeMOV_Const(msb, msbv);
    }

    /**
     * Pop the top of the FPU stack into the given memory location.
     * 
     * @param os
     * @param reg
     * @param disp
     */
    protected void popFromFPU(AbstractX86Stream os, Register reg, int disp) {
        os.writeFISTP64(reg, disp);
    }

    /**
     * Push my constant on the stack using the given os.
     * 
     * @param os
     */
    protected final void pushConstant(EmitterContext ec, AbstractX86Stream os) {
        os.writePUSH(getMsbValue());
        os.writePUSH(getLsbValue());
    }

    /**
     * Push the given memory location on the FPU stack.
     * 
     * @param os
     * @param reg
     * @param disp
     */
    protected void pushToFPU(AbstractX86Stream os, Register reg, int disp) {
        os.writeFILD64(reg, disp);
    }

    /**
     * Gets the LSB part of the constant value of this item.
     * 
     * @return
     */
    final int getLsbValue() {
        assertCondition(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
        return (int) (value & 0xFFFFFFFFL);
    }

    /**
     * Gets the MSB part of the constant value of this item.
     * 
     * @return
     */
    final int getMsbValue() {
        assertCondition(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
        return (int) ((value >>> 32) & 0xFFFFFFFFL);
    }

    /**
     * Get the JVM type of this item
     * 
     * @return the JVM type
     */
    final int getType() {
        return JvmType.LONG;
    }

    /**
     * Gets the constant value of this item.
     * 
     * @return
     */
    final long getValue() {
        assertCondition(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
        return value;
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.DoubleWordItem#cloneConstant()
     */
    protected DoubleWordItem cloneConstant() {
        return factory.createLConst(getValue());
    }
}