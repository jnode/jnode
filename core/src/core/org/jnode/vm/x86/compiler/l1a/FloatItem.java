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
final class FloatItem extends WordItem implements FPItem {

    static FloatItem createConst(float val) {
        return new FloatItem(Kind.CONSTANT, null, 0, val);
    }

    static FloatItem createFPUStack() {
        return new FloatItem(Kind.FPUSTACK, null, 0, 0.0f);
    }

    static FloatItem createLocal(int offsetToFP) {
        return new FloatItem(Kind.LOCAL, null, offsetToFP, 0.0f);
    }

    static FloatItem createReg(Register reg) {
        return new FloatItem(Kind.REGISTER, reg, 0, 0.0f);
    }

    static FloatItem createStack() {
        return new FloatItem(Kind.STACK, null, 0, 0.0f);
    }

    private final float value;

    /**
     * @param kind
     * @param offsetToFP
     * @param value
     */
    private FloatItem(int kind, Register reg, int offsetToFP, float value) {
        super(kind, reg, offsetToFP);

        this.value = value;
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.FPItem#getFPValue()
     */
    public double getFPValue() {
        assertCondition(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
        return value;
    }

    /**
     * Load my constant to the given os.
     * 
     * @param os
     * @param reg
     */
    protected void loadToConstant(EmitterContext ec, AbstractX86Stream os,
            Register reg) {
        os.writeMOV_Const(reg, Float.floatToIntBits(value));
    }

    /**
     * Pop the top of the FPU stack into the given memory location.
     * 
     * @param os
     * @param reg
     * @param disp
     */
    protected void popFromFPU(AbstractX86Stream os, Register reg, int disp) {
        os.writeFSTP32(reg, disp);
    }

    /**
     * Push my constant on the stack using the given os.
     * 
     * @param os
     */
    protected void pushConstant(EmitterContext ec, AbstractX86Stream os) {
        os.writePUSH(Float.floatToIntBits(value));
    }

    /**
     * Push the given memory location on the FPU stack.
     * 
     * @param os
     * @param reg
     * @param disp
     */
    protected void pushToFPU(AbstractX86Stream os, Register reg, int disp) {
        os.writeFLD32(reg, disp);
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
        return JvmType.FLOAT;
    }

    /**
     * Gets the constant value.
     * 
     * @return
     */
    float getValue() {
        assertCondition(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
        return value;
    }
}