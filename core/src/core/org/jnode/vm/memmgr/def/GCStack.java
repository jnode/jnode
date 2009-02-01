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
 
package org.jnode.vm.memmgr.def;

import org.jnode.vm.VmSystemObject;
import org.jnode.vm.annotation.Inline;
import org.vmmagic.pragma.Uninterruptible;

/**
 * @author epr
 */
final class GCStack extends VmSystemObject implements Uninterruptible {

    /**
     * The default size of a stack
     */
    public static final int DEFAULT_STACK_SIZE = 4096;
    /**
     * The actual stack
     */
    private final Object[] stack;
    /**
     * The size of the stack (in objects)
     */
    private final int size;
    /**
     * The stackpointer
     */
    private int stackPtr;
    /**
     * Has the stack occurred an overflow?
     */
    private boolean overflow;

    /**
     * Create a new instance
     */
    public GCStack() {
        this.stack = new Object[DEFAULT_STACK_SIZE];
        this.size = stack.length;
    }

    /**
     * Push a given object on the stack. If an overflow occurs, mark
     * the overflow and do not push the object.
     *
     * @param object
     */
    @Inline
    public final void push(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Cannot push null object");
        }
        if (stackPtr == size) {
            overflow = true;
        } else {
            stack[stackPtr++] = object;
        }
    }

    /**
     * Gets the last pushed object of the stack and remove it from the stack.
     *
     * @return The object
     */
    @Inline
    public final Object pop() {
        if (stackPtr == 0) {
            return null;
        } else {
            stackPtr--;
            Object result = stack[stackPtr];
            if (result == null) {
                throw new IllegalStateException("Null object found on GCStack");
            }
            stack[stackPtr] = null;
            return result;
        }
    }

    /**
     * Is this stack empty?
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return (stackPtr == 0);
    }

    /**
     * Has a stackoverflow occurred?
     *
     * @return boolean
     */
    @Inline
    public final boolean isOverflow() {
        return overflow;
    }

    /**
     * Reset the contents of this stack to its original state
     */
    public void reset() {
        stackPtr = 0;
        overflow = false;
    }
}
