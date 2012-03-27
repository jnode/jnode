/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

package org.jnode.vm.x86.compiler.l1a;

import org.jnode.vm.JvmType;
import org.jnode.vm.bytecode.StackException;
import org.jnode.vm.facade.VmUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
class ItemStack {

    /**
     * Expected item kind
     */
    private final int expectedKind;

    /**
     * Maximum stack size
     */
    private final int maxSize;

    /**
     * Actual stack
     */
    protected Item[] stack;

    /**
     * Top of stack
     */
    protected int tos;

    /**
     * Constructor; create and initialize stack with default size
     *
     * @param expectedKind
     * @param maxSize
     */
    ItemStack(int expectedKind, int maxSize) {
        this.expectedKind = expectedKind;
        this.maxSize = maxSize;
        stack = new Item[Math.min(8, maxSize)];
        tos = 0;
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
    private void grow() {
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

    /**
     * Finds the position of the specified item on the stack starting from the top.
     *
     * @param item the item to find
     * @return the position of the item or -1 if not found
     */
    final int stackLocation(Item item) {
        int ret = -1;

        int i = tos - 1;
        while ((i >= 0) && (stack[i] != item))
            i--;

        if (i >= 0)
            ret = tos - 1 - i;

        return ret;
    }

    /**
     * Exchanges the item at the specified position with the top item.
     *
     * @param pos the position of the item
     */
    final void makeTop(int pos) {
        Item tmp = stack[tos - 1];
        stack[tos - 1] = stack[tos - 1 - pos];
        stack[tos - 1 - pos] = tmp;
    }

    final void pop(EmitterContext ec) {
        if (tos <= 0) {
            throw new Error("Stack is empty");
        }
        tos--;
        Item item = stack[tos];
        if (item.getKind() != 0)
            item.release(ec);
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
                + this + ')');
        }
        stack[tos] = null;
    }

    final void push(Item item) {
        if (VmUtils.verifyAssertions())
            VmUtils._assert(item.getKind() == expectedKind,
                "item.getKind() == expectedKind");
        if (tos == stack.length) {
            grow();
        }
        stack[tos++] = item;
    }

    /**
     * Reset this stack. The stack will be empty afterwards.
     *
     * @param ec
     */
    final void reset(EmitterContext ec) {
        while (tos != 0) {
            pop(ec);
        }

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
