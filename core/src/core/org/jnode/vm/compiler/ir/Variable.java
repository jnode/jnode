/*
 * $Id$
 *
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
 
package org.jnode.vm.compiler.ir;

import org.jnode.vm.compiler.ir.quad.AssignQuad;

/**
 * @author Madhu Siddalingaiah
 */
public abstract class Variable<T> extends Operand<T> implements Cloneable {
    private int index;
    private int ssaValue;
    private Location<T> location;

    /*
      * The operation where this variable is assigned
      */
    private AssignQuad<T> assignQuad;

    /*
      * The address where this variable is last used
      */
    private int lastUseAddress;

    public Variable(int type, int index) {
        super(type);
        this.index = index;
    }

    /**
     * @return the variable's index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the variable's SSA value
     */
    public int getSSAValue() {
        return ssaValue;
    }

    /**
     * @param i
     */
    public void setSSAValue(int i) {
        ssaValue = i;
    }

    public abstract Object clone();

    /**
     * Returns the AssignQuad where this variable was last assigned.
     *
     * @return the Quad.
     */
    public AssignQuad<T> getAssignQuad() {
        return assignQuad;
    }

    /**
     * @return the address of the last use of this variable.
     */
    public int getLastUseAddress() {
        return lastUseAddress;
    }

    /**
     * @param assignQuad
     */
    public void setAssignQuad(AssignQuad<T> assignQuad) {
        this.assignQuad = assignQuad;
    }

    public int getAssignAddress() {
        if (assignQuad == null) {
            return 0;
        }
        // Add one so this live range starts just after this operation.
        // This way live range interference computation is simplified.
        return assignQuad.getLHSLiveAddress();
    }

    /**
     * @param address
     */
    public void setLastUseAddress(int address) {
        if (address > lastUseAddress) {
            lastUseAddress = address;
        }
    }

    public Operand<T> simplify() {
        Operand<T> op = assignQuad.propagate(this);
        return op;
    }

    /**
     * @return the assigned variable location
     */
    public Location<T> getLocation() {
        return this.location;
    }

    /**
     * @param loc
     */
    public void setLocation(Location<T> loc) {
        this.location = loc;
    }

    /**
     * @see org.jnode.vm.compiler.ir.Operand#getAddressingMode()
     */
    public AddressingMode getAddressingMode() {
        if (location instanceof StackLocation) {
            return AddressingMode.STACK;
        } else if (location instanceof RegisterLocation) {
            return AddressingMode.REGISTER;
        } else {
            throw new IllegalArgumentException("Undefined location: " + toString());
        }
    }

    public boolean equals(Object other) {
        if (other instanceof Variable) {
            Variable v = (Variable) other;
            return index == v.getIndex() &&
                ssaValue == v.getSSAValue();
        }
        return false;
    }
}
