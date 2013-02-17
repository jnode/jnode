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
 
package org.jnode.vm.classmgr;

import org.jnode.vm.objects.VmSystemObject;

/**
 * This local variable must have a value at indices into the code
 * array in the interval [startPC, startPC + length].
 *
 * @author epr
 */
public final class VmLocalVariable extends VmSystemObject {

    /**
     * Sharable empty array.
     */
    static final VmLocalVariable[] EMPTY = new VmLocalVariable[0];

    /**
     * Start of the value value range.
     */
    private final char startPC;
    
    /**
     * Length of the value value range.
     */
    private final char length;
    
    /**
     * Name of the variable
     */
    private final char nameIndex;
    
    /**
     * Type descriptor of the variable
     */
    private final char descriptorIndex;
    
    /**
     * Local variable index (on the stack) of the variable
     */
    private final char index;

    /**
     * Create a new instance
     *
     * @param startPC
     * @param length
     * @param nameIndex
     * @param descriptorIndex
     * @param index
     */
    public VmLocalVariable(char startPC, char length, char nameIndex, char descriptorIndex, char index) {
        this.startPC = startPC;
        this.length = length;
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
        this.index = index;
    }


    /**
     * @return The index of this variable
     */
    public char getIndex() {
        return this.index;
    }

    /**
     * @return The length from startPc where this variable is valid
     * @see #getStartPC()
     */
    public char getLength() {
        return this.length;
    }

    /**
     * @return The name of this variable
     */
    public String getName(VmType<?> declClass) {
        return declClass.getCP().getUTF8(nameIndex);
    }

    /**
     * @return The signature of this variable
     */
    public String getDescriptor(VmType<?> declClass) {
        return declClass.getCP().getUTF8(descriptorIndex);
    }

    /**
     * @return The start PC where this variable is valid
     */
    public char getStartPC() {
        return this.startPC;
    }

    /**
     * Gets the startPC + length
     *
     * @return The (inclusive) end PC where this variable is valid
     */
    public int getEndPC() {
        return this.startPC + this.length;
    }

    /**
     * Does this variable match the given pc and index.
     *
     * @param pc
     * @param index
     * @return
     */
    final boolean matches(int pc, int index) {
        return (this.index == index) && (pc >= startPC) && (pc <= startPC + length);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append((int) startPC);
        sb.append('-');
        sb.append(startPC + length);
        sb.append(" idx:");
        sb.append((int) index);
        sb.append(']');
        return sb.toString();
    }
}
