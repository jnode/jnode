/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.vm.x86.compiler.l1b;

import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.bytecode.StackException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
class ItemStack {

    /** Expected item kind */
    private final int expectedKind;

    /** Maximum stack size */
    private final int maxSize;

    /** Actual stack */
    protected Item[] stack;

    /** Top of stack */
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

//    /**
//     * Does this stack contain the given item.
//     * 
//     * @param item
//     * @return
//     */
//    final boolean contains(Item item) {
//        for (int i = 0; i < tos; i++) {
//            if (stack[i] == item)
//                return true;
//        }
//        return false;
//    }
//
    /**
     * Grow the stack capacity.
     */
    private final void grow() {
        if (stack.length == maxSize) {
            throw new StackException("Stack full");
        } else {
            final Item[] tmp = new Item[Math.min(maxSize, stack.length * 2)];
            System.arraycopy(stack, 0, tmp, 0, stack.length);
            stack = tmp;
        }
    }

    /**
     * Is there room on the stack for a given number of items.
     * 
     * @param items
     * @return
     */
    final boolean hasCapacity(int items) {
        return (tos + items <= maxSize);
    }

//    /**
//     * Is the item at the offset relative to the top of stack equal to the given
//     * item.
//     * 
//     * @param item
//     * @return
//     */
//    final boolean isAt(Item item, int offset) {
//        if (tos <= 0) {
//            throw new StackException("Stack is empty");
//        }
//        return (stack[(tos - 1) - offset] == item);
//    }
//
    /**
     * Is the top of stack equal to the given item
     * 
     * @param item
     * @return
     */
    final boolean isTos(Item item) {
        if (tos <= 0) {
            throw new StackException("Stack is empty");
        }
        return (stack[tos - 1] == item);
    }

    final void pop() {
        if (tos <= 0) {
            throw new Error("Stack is empty");
        }
        tos--;
    }

    final void pop(Item item) {
        if (tos <= 0) {
            throw new Error("Stack is empty");
        }
        if (stack[--tos] != item) {
            int i = tos - 1;
            while ((i >= 0) && (stack[i] != item))
                i--;

            throw new StackException("OperandStack[" + tos + "](" + item
                    + ") is not the expected element (found at " + i + ", "
                    + this + ")");
        }
        stack[tos] = null;
    }

    final void push(Item item) {
        if (Vm.VerifyAssertions)
            Vm._assert(item.getKind() == expectedKind,
                    "item.getKind() == expectedKind");
        if (tos == stack.length) {
            grow();
        }
        stack[tos++] = item;
    }

    /**
     * Reset this stack. The stack will be empty afterwards.
     */
    final void reset() {
        stack = new Item[Math.min(8, maxSize)];
        tos = 0;
    }

    /**
     * Gets the item at the top of the stack.
     * 
     * @return
     */
    final Item tos() {
        return stack[tos - 1];
    }

    public String toString() {
        if (tos == 0) {
            return "EMPTY";
        }
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < tos; i++) {
            if (i != 0) {
                buf.append(',');
            }
            buf.append('(');
            buf.append(JvmType.toString(stack[i].getType()));
            buf.append(',');
            buf.append(Item.Kind.toString(stack[i].getKind()));
            buf.append(')');
        }
        buf.append("TOS");
        return buf.toString();
    }

    final void visitItems(ItemVisitor visitor) {
        for (int i = 0; i < tos; i++) {
            visitor.visit(stack[i]);
        }
    }

}
