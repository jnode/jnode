/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.vm.bytecode.StackException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
class ItemStack {

    private final int expectedKind;

    private final int maxSize;

    protected Item[] stack;

    protected int tos;

    /**
     * 
     * Constructor; create and initialize stack with default size
     */
    ItemStack(int expectedKind, int maxSize) {
        this.expectedKind = expectedKind;
        this.maxSize = maxSize;
        reset();
    }

    void reset() {
        stack = new Item[ Math.min(8, maxSize)];
        tos = 0;
    }

    // operations on the operand stack
    // used to check sanity
    private final void grow() {
        if (stack.length == maxSize) {
            throw new StackException("Stack full");
        } else {
            final Item[] tmp = new Item[ Math.min(maxSize, stack.length * 2)];
            System.arraycopy(stack, 0, tmp, 0, stack.length);
            stack = tmp;
        }
    }

    final void push(Item item) {
        Item.assertCondition(item.getKind() == expectedKind,
                "item.getKind() == expectedKind");
        if (tos == stack.length) grow();
        stack[ tos++] = item;
    }

    final void pop(Item item) {
        if (tos <= 0) { throw new Error("Stack is empty"); }
        if (stack[ --tos] != item) {
            int i = tos - 1;
            while ((i >= 0) && (stack[ i] != item))
                i--;

            throw new StackException("OperandStack[" + tos
                    + "] is not the expected element (found at " + i + ", "
                    + this + ")");
        }
        stack[ tos] = null;
    }

    final void pop() {
        if (tos <= 0) { throw new Error("Stack is empty"); }
        tos--;
    }

    /**
     * Gets the item at the top of the stack.
     * 
     * @return
     */
    final Item tos() {
        return stack[ tos - 1];
    }

    /**
     * Is the top of stack equal to the given item
     * 
     * @param item
     * @return
     */
    final boolean isTos(Item item) {
        if (tos <= 0) { throw new StackException("Stack is empty"); }
        return (stack[ tos - 1] == item);
    }

    /**
     * Is the item at the offset relative to the top of stack equal to the given
     * item.
     * 
     * @param item
     * @return
     */
    final boolean isAt(Item item, int offset) {
        if (tos <= 0) { throw new StackException("Stack is empty"); }
        return (stack[ (tos - 1) - offset] == item);
    }

    public String toString() {
        final StringBuffer buf = new StringBuffer();
        for (int i = 0; i < tos; i++) {
            if (i != 0) {
                buf.append(',');
            }
            buf.append('(');
            buf.append(stack[ i].getType());
            buf.append(',');
            buf.append(stack[ i].getKind());
            buf.append(')');
        }
        return buf.toString();
    }

}