/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.vm.bytecode;

/**
 * Stack of JvmType's.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class TypeStack {

    private byte[] stack;

    private int tos;

    /**
     * Initialize a new instance.
     */
    public TypeStack() {
        stack = new byte[8];
        tos = 0;
    }

    /**
     * Initialize a new instance.
     */
    public TypeStack(TypeStack src) {
        copyFrom(src);
    }

    /**
     * Initialize a new instance.
     */
    public void copyFrom(TypeStack src) {
        if (src != null) {
            stack = new byte[src.stack.length];
            System.arraycopy(src.stack, 0, stack, 0, stack.length);
            tos = src.tos;
        } else {
            stack = new byte[8];
            tos = 0;
        }
    }

    /**
     * Empty the tstack.
     */
    public void clear() {
        tos = 0;
    }

    /**
     * Is this stack empty.
     *
     * @return
     */
    public final boolean isEmpty() {
        return (tos == 0);
    }

    /**
     * Is this stack equal to the givn object?
     */
    public boolean equals(TypeStack tso) {
        if ((this.tos == 0) && (tso == null)) {
            return true;
        }
        if (this.tos != tso.tos) {
            return false;
        }
        for (int i = 0; i < tos; i++) {
            if (this.stack[i] != tso.stack[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Is this stack equal to the givn object?
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object o) {
        if (o instanceof TypeStack) {
            return equals((TypeStack) o);
        } else {
            return false;
        }
    }

    /**
     * Push a type of the stack.
     *
     * @param type
     */
    public final void push(int type) {
        if (tos == stack.length) grow();
        stack[tos++] = (byte) type;
    }

    /**
     * Pop an item of the stack and return its given type.
     */
    public final int pop() {
        if (tos <= 0) {
            throw new Error("Stack is empty");
        }
        return stack[--tos];
    }

    /**
     * Pop an item of the stack and expect a given type.
     *
     * @param type
     */
    public final void pop(int type) {
        if (tos <= 0) {
            throw new Error("Stack is empty");
        }
        if (stack[--tos] != type) {
            throw new Error("TypeStack[" + tos
                + "] is not the expected element " + type + " but "
                + stack[tos]);
        }
    }

    /**
     * Gets the number of elements in this stack.
     *
     * @return
     */
    public final int size() {
        return tos;
    }

    /**
     * Gets an entry of the stack.
     *
     * @param stackIndex 0..size-1; stackIndex == size-1 means top of stack.
     * @return
     */
    public final int getType(int stackIndex) {
        return stack[stackIndex];
    }

    /**
     * Convert to a string representation
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append('{');
        for (int i = 0; i < tos; i++) {
            if (i > 0) {
                buf.append(',');
            }
            buf.append(stack[i]);
        }
        buf.append('}');
        return buf.toString();
    }

    /**
     * Grow the stack space.
     */
    private final void grow() {
        final byte[] tmp = new byte[stack.length * 2];
        System.arraycopy(stack, 0, tmp, 0, stack.length);
        stack = tmp;
    }
}
