/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.JvmType;

/**
 * @author Patrik Reali
 */
final class DoubleItem extends DoubleWordItem implements FPItem {

    static DoubleItem createConst(double val) {
        return new DoubleItem(Kind.CONSTANT, 0, null, null, val);
    }

    static DoubleItem createFPUStack() {
        return new DoubleItem(Kind.FPUSTACK, 0, null, null, 0.0);
    }

    static DoubleItem createLocal(int offsetToFP) {
        return new DoubleItem(Kind.LOCAL, offsetToFP, null, null, 0.0);
    }

    static DoubleItem createReg(Register lsb, Register msb) {
        return new DoubleItem(Kind.REGISTER, 0, lsb, msb, 0.0);
    }

    static DoubleItem createStack() {
        return new DoubleItem(Kind.STACK, 0, null, null, 0.0);
    }

    private final double value;

    /**
     * @param kind
     * @param offsetToFP
     * @param value
     */
    public DoubleItem(int kind, int offsetToFP, Register lsb, Register msb,
            double value) {
        super(kind, offsetToFP, lsb, msb);
        this.value = value;
    }

    /**
     * Gets the constant floating point value.
     * 
     * @return
     */
    public double getFPValue() {
        assertCondition(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
        return value;
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
        final long lvalue = Double.doubleToLongBits(value);
        final int lsbv = (int) (lvalue & 0xFFFFFFFFL);
        final int msbv = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);

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
        os.writeFSTP64(reg, disp);
    }

    /**
     * Push my constant on the stack using the given os.
     * 
     * @param os
     */
    protected final void pushConstant(EmitterContext ec, AbstractX86Stream os) {
        final long lvalue = Double.doubleToLongBits(value);
        final int lsbv = (int) (lvalue & 0xFFFFFFFFL);
        final int msbv = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);
        os.writePUSH(msbv);
        os.writePUSH(lsbv);
    }

    /**
     * Push the given memory location on the FPU stack.
     * 
     * @param os
     * @param reg
     * @param disp
     */
    protected void pushToFPU(AbstractX86Stream os, Register reg, int disp) {
        os.writeFLD64(reg, disp);
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
     */
    final Item clone(EmitterContext ec) {
        Item res = null;
        switch (getKind()) {
        case Kind.REGISTER:
            //TODO
            notImplemented();
            break;

        case Kind.LOCAL:
            //TODO
            notImplemented();
            break;

        case Kind.CONSTANT:
            //TODO
            notImplemented();
            break;

        case Kind.FPUSTACK:
            //TODO
            notImplemented();
            break;

        case Kind.STACK:
            //TODO
            notImplemented();
            break;
        }
        return res;
    }

    /**
     * Get the JVM type of this item
     * 
     * @return the JVM type
     */
    final int getType() {
        return JvmType.DOUBLE;
    }

    /**
     * Gets the constant value.
     * 
     * @return
     */
    double getValue() {
        assertCondition(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
        return value;
    }
}