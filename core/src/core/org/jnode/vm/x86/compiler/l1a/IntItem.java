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
 * IntItems are items with type INT
 */

final class IntItem extends WordItem implements X86CompilerConstants {

    private final int value;

    private IntItem(int kind, Register reg, int value, int local) {
        super(kind, reg, local);
        this.value = value;
    }

    /**
     * Get the JVM type of this item
     * 
     * @return the JVM type
     */
    int getType() {
        return JvmType.INT;
    }

    int getValue() {
        assertCondition(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
        return value;
    }

    /**
     * Load my constant to the given os.
     * 
     * @param os
     * @param reg
     */
    protected void loadToConstant(EmitterContext ec, AbstractX86Stream os, Register reg) {
        if (value != 0) {
            os.writeMOV_Const(reg, value);
        } else {
            os.writeXOR(reg, reg);
        }
    }

    /**
     * Push my constant on the stack using the given os.
     * 
     * @param os
     */
    protected void pushConstant(EmitterContext ec, AbstractX86Stream os) {
        os.writePUSH(value);
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.Item#clone()
     */
    Item clone(EmitterContext ec) {
        Item res = null;
        switch (getKind()) {
        case Kind.REGISTER:
            final X86RegisterPool pool = ec.getPool();
            final Register r = pool.request(JvmType.INT);
            res = createReg(r);
            pool.transferOwnerTo(r, res);
            break;

        case Kind.LOCAL:
            res = createLocal(getOffsetToFP());
            break;

        case Kind.CONSTANT:
            res = createConst(value);
            break;

        case Kind.FPUSTACK:
            //TODO
            notImplemented();
            break;

        case Kind.STACK:
            AbstractX86Stream os = ec.getStream();
            os.writePUSH(Register.SP, 0);
            res = createStack();
            if (VirtualStack.checkOperandStack) {
                final VirtualStack stack = ec.getVStack();
                stack.operandStack.push(res);
            }
            break;
        }
        return res;
    }

    static IntItem createReg(Register reg) {
        return new IntItem(Kind.REGISTER, reg, 0, 0);
    }

    static IntItem createConst(int value) {
        return new IntItem(Kind.CONSTANT, null, value, 0);
    }

    static IntItem createLocal(int offsetToFP) {
        return new IntItem(Kind.LOCAL, null, 0, offsetToFP);
    }

    static IntItem createStack() {
        return new IntItem(Kind.STACK, null, 0, 0);
    }

}