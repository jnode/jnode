/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.x86.compiler.*;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Patrik Reali
 */
final class FloatItem extends WordItem implements X86CompilerConstants {

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
     * Get the JVM type of this item
     * 
     * @return the JVM type
     */
    final int getType() {
        return JvmType.FLOAT;
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
     * Push my constant on the stack using the given os.
     * 
     * @param os
     */
    protected void pushConstant(EmitterContext ec, AbstractX86Stream os) {
        os.writePUSH(Float.floatToIntBits(value));
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

    static FloatItem createStack() {
        return new FloatItem(Kind.STACK, null, 0, 0.0f);
    }

    static FloatItem createLocal(int offsetToFP) {
        return new FloatItem(Kind.LOCAL, null, offsetToFP, 0.0f);
    }

    static FloatItem createConst(float val) {
        return new FloatItem(Kind.CONSTANT, null, 0, val);
    }

    static FloatItem createReg(Register reg) {
        return new FloatItem(Kind.REGISTER, reg, 0, 0.0f);
    }

    static FloatItem createFPUStack() {
        return new FloatItem(Kind.FPUSTACK, null, 0, 0.0f);
    }
}