/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class ItemStack {

    private final int expectedKind;

    private Item[] stack;

    private int tos;

    /**
     * 
     * Constructor; create and initialize stack with default size
     */
    ItemStack(int expectedKind) {
        this.expectedKind = expectedKind;
        reset();
    }

    void reset() {
        stack = new Item[ 8];
        tos = 0;
    }

    // operations on the operand stack
    // used to check sanity
    private final void grow() {
        final Item[] tmp = new Item[ stack.length * 2];
        System.arraycopy(stack, 0, tmp, 0, stack.length);
        stack = tmp;
    }

    final void push(Item item) {
        Item.myAssert(item.getKind() == expectedKind);
        if (tos == stack.length) grow();
        stack[ tos++] = item;
    }

    final void pop(Item item) {
        if (tos <= 0) { throw new Error("Stack is empty"); }
        if (stack[ --tos] != item) {
            int i = tos - 1;
            while ((i >= 0) && (stack[ i] != item))
                i--;

            throw new Error("OperandStack[" + tos
                    + "] is not the expected element (found at " + i + ")");
        }
        stack[ tos] = null;
    }

}