/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 * A TypeStack is a stack of internal type numbers represented as {@link org.jnode.vm.JvmType} 
 * values; i.e. integers.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class TypeStack {

    private byte[] stack;

    private int tos;

    /**
     * Create a new empty TypeStack instance.
     */
    public TypeStack() {
        stack = new byte[8];
        tos = 0;
    }

    /**
     * Create a new empty TypeStack instance as copy of an existing one.
     * @param src the stack whose contents is to be copied.
     */
    public TypeStack(TypeStack src) {
        copyFrom(src);
    }

    /**
     * Set this stack's contents to be same as another stack.  The
     * current state of the stack (if any) is discarded.
     * 
     * @param src the stack whose contents is to be copied.
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
     * Empty the TypeStack.
     */
    public void clear() {
        tos = 0;
    }

    /**
     * Is this TypeStack empty.
     *
     * @return {@code true} if the stack is empty, otherwise {@code false}.
     */
    public final boolean isEmpty() {
        return (tos == 0);
    }

    /**
     * Is this TypeStack equal to the given TypeStack.  Note that this is an
     * overload for {@link java.lang.Object#equals(java.lang.Object)} not
     * an override.
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

    public boolean equals(Object o) {
        if (o instanceof TypeStack) {
            return equals((TypeStack) o);
        } else {
            return false;
        }
    }

    /**
     * Push a type onto the TypeStack.
     *
     * @param type a {@link org.jnode.vm.JvmType} value
     */
    public final void push(int type) {
        if (tos == stack.length) grow();
        stack[tos++] = (byte) type;
    }

    /**
     * Pop a type from the TypeStack and return it.
     * 
     * @return a {@link org.jnode.vm.JvmType} value
     * @throws Error if the stack is empty
     */
    public final int pop() {
        if (tos <= 0) {
            throw new Error("Stack is empty");
        }
        return stack[--tos];
    }

    /**
     * Pop a type from the stack and check that it is the expected type.
     *
     * @param type the expected {@link org.jnode.vm.JvmType} value.
     * @throws Error if there is a type mismatch
     */
    public final void pop(int type) throws Error {
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
     * @return the number of elements.
     */
    public final int size() {
        return tos;
    }

    /**
     * Get the element at a particular stack index.
     *
     * @param stackIndex the stack index.  This should be a number in the range
     * {@code 0 .. size() - 1} inclusive where {@code size() - 1} is the top element 
     * on the stack.
     * @return the {@link org.jnode.vm.JvmType} value at the given offset.
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
