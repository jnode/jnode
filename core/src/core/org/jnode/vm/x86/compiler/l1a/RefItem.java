/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.x86.compiler.X86CompilerConstants;
import org.jnode.vm.x86.compiler.X86CompilerHelper;

/**
 * @author Patrik Reali
 */
final class RefItem extends WordItem implements X86CompilerConstants {

    private VmConstString value;

    // generate unique labels for writeStatics (should use current label)
    private long labelCounter;

    /**
     * @param kind
     * @param reg
     * @param val
     * @param offsetToFP
     */
    private RefItem(int kind, Register reg, VmConstString val, int offsetToFP) {
        super(kind, reg, offsetToFP);
        this.value = val;
    }

    /**
     * Get the JVM type of this item
     * 
     * @return the JVM type
     */
    int getType() {
        return JvmType.REFERENCE;
    }

    /**
     * Gets the value of this reference. Item must have a CONSTANT kind.
     * 
     * @return
     */
    VmConstString getValue() {
        myAssert(getKind() == Kind.CONSTANT);
        return value;
    }

    /**
     * Load my constant to the given os.
     * 
     * @param os
     * @param reg
     */
    protected void loadToConstant(EmitterContext ec, AbstractX86Stream os, Register reg) {
        if (value == null) {
            os.writeMOV_Const(reg, value);
        } else {
            X86CompilerHelper helper = ec.getHelper();
            Label l = new Label(Long.toString(labelCounter++));
            helper.writeGetStaticsEntry(l, reg, value);
        }
    }

    /**
     * Push my constant on the stack using the given os.
     * 
     * @param os
     */
    protected void pushConstant(EmitterContext ec, AbstractX86Stream os) {
        if (value == null) {
            os.writePUSH_Const(null);
        } else {
            X86CompilerHelper helper = ec.getHelper();
            Label l = new Label(Long.toString(labelCounter++));
            helper.writePushStaticsEntry(l, value);
        }
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
            res = createRegister(r);
            pool.transferOwnerTo(r, res);
            break;

        case Kind.LOCAL:
            res = createLocal(getOffsetToFP());
            break;

        case Kind.CONSTANT:
            res = createConst(value);
            break;

        case Kind.FREGISTER:
            //TODO
            notImplemented();
            break;

        case Kind.STACK:
            AbstractX86Stream os = ec.getStream();
            os.writePUSH(Register.SP, 0);
            res = createStack();
            if (VirtualStack.checkOperandStack) {
                final VirtualStack stack = ec.getVStack();
                stack.pushOnOperandStack(res);
            }
            break;
        }
        return res;
    }

    static RefItem createRegister(Register reg) {
        return new RefItem(Kind.REGISTER, reg, null, 0);
    }

    static RefItem createConst(VmConstString value) {
        return new RefItem(Kind.CONSTANT, null, value, 0);
    }

    static RefItem createLocal(int offsetToFP) {
        return new RefItem(Kind.LOCAL, null, null, offsetToFP);
    }

    static RefItem createStack() {
        return new RefItem(Kind.STACK, null, null, 0);
    }

}