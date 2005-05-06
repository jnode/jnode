/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

public enum AddressingMode {
    CONSTANT(0x01),
    REGISTER(0x02),
    STACK(0x03);
    
    final int value;
    private AddressingMode(int value) {
        this.value = value;
    }
    public final int getValue() {
        return value;
    }
}