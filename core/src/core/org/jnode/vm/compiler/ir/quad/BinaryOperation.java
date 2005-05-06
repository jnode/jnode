/*
 * $Id$
 */
package org.jnode.vm.compiler.ir.quad;

public enum BinaryOperation {
    IADD("+"),
    LADD("+"),
    FADD("+"),
    DADD("+"),
    ISUB("-"),
    LSUB("-"),
    FSUB("-"),
    DSUB("-"),
    IMUL("*"),
    LMUL("*"),
    FMUL("*"),
    DMUL("*"),
    IDIV("/"),
    LDIV("/"),
    FDIV("/"),
    DDIV("/"),
    IREM("%"),
    LREM("%"),
    FREM("%"),
    DREM("%"),
    ISHL("<<"),
    LSHL("<<"),
    ISHR(">>"),
    LSHR(">>"),
    IUSHR(">>>"),
    LUSHR(">>>"),
    IAND("&"),
    LAND("&"),
    IOR("|"),
    LOR("|"),
    IXOR("^"),
    LXOR("^");
    
    private final String v;
    private BinaryOperation(String v) {
        this.v = v;
    }
    
    public final String getOperation() {
        return v;
    }
}