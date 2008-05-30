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

package org.jnode.vm.x86.compiler;

import org.jnode.assembler.x86.X86Register;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface AbstractX86StackManager {

    /**
     * Write code to push the contents of the given register on the stack
     *
     * @param reg
     * @see JvmType
     */
    public void writePUSH(int jvmType, X86Register.GPR reg);

    /**
     * Write code to push a 64-bit word on the stack
     *
     * @param lsbReg
     * @param msbReg
     * @see JvmType
     */
    public void writePUSH64(int jvmType, X86Register.GPR lsbReg,
                            X86Register.GPR msbReg);

    /**
     * Write code to push a 64-bit word on the stack
     *
     * @param reg
     * @see JvmType
     */
    public void writePUSH64(int jvmType, X86Register.GPR64 reg);
}
