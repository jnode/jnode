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

/**
 * VM representation of a non-static method.
 *
 * @author epr
 */
public final class VmInstanceMethod extends VmMethod {

    /**
     * Offset of this method in the VMT of its declaring class
     */
    private int tibOffset;

    /**
     * @param name
     * @param signature
     * @param modifiers
     * @param declaringClass
     */
    public VmInstanceMethod(
        String name,
        String signature,
        int modifiers,
        VmType declaringClass) {
        super(name, signature, modifiers, declaringClass);
    }

    /**
     * Create a clone of an abstract method.
     *
     * @param method
     */
    public VmInstanceMethod(VmInstanceMethod method) {
        super(method);
        if (!method.isAbstract()) {
            throw new IllegalArgumentException("Method must be abstract");
        }
    }

    /**
     * Gets the offset of this method in the TIB of its declaring class
     *
     * @return offset
     */
    public int getTibOffset() {
        return tibOffset;
    }

    /**
     * Sets the offset of this method in the VMT of its declaring class
     *
     * @param offset
     */
    protected void setTibOffset(int offset) {
        this.tibOffset = offset;
    }

}
