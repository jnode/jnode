/*
 * $Id$
 */
package org.jnode.vm.compiler.ir.quad;

public enum BranchCondition {

    IFEQ("== 0", false), IFNE("!= 0", false), IFLT("< 0", false), IFGE(
            ">= 0", false), IFGT("> 0", false), IFLE("<= 0", false),

    IFNONNULL("!= null", false), IFNULL("== null", false),

    IF_ICMPEQ("==", true), IF_ICMPNE("!=", true), IF_ICMPLT("<", true), IF_ICMPGE(
            ">=", true), IF_ICMPGT(">", true), IF_ICMPLE("<=", true),

    IF_ACMPEQ("==", true), IF_ACMPNE("!=", true);

    private final String v;

    final boolean binary;

    private BranchCondition(String v, boolean binary) {
        this.v = v;
        this.binary = binary;
    }

    public String getCondition() {
        return v;
    }

    public boolean isBinary() {
        return binary;
    }

    public boolean isUnary() {
        return !binary;
    }
}