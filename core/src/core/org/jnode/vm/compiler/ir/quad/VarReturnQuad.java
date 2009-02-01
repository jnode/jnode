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
 
package org.jnode.vm.compiler.ir.quad;

import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;

/**
 * @author Madhu Siddalingaiah
 */
public class VarReturnQuad<T> extends Quad<T> {
    private Operand<T> refs[];

    /**
     * @param address
     */
    public VarReturnQuad(int address, IRBasicBlock<T> block, int varIndex) {
        super(address, block);
        refs = new Operand[]{getOperand(varIndex)};
    }

    /**
     * @see org.jnode.vm.compiler.ir.quad.Quad#getDefinedOp()
     */
    public Operand<T> getDefinedOp() {
        return null;
    }

    /**
     * @see org.jnode.vm.compiler.ir.quad.Quad#getReferencedOps()
     */
    public Operand<T>[] getReferencedOps() {
        return refs;
    }

    public Operand<T> getOperand() {
        return refs[0];
    }

    public String toString() {
        return getAddress() + ": return " + refs[0];
    }

    /**
     * @see org.jnode.vm.compiler.ir.Quad#doPass2(org.jnode.util.BootableHashMap)
     */
    public void doPass2() {
        refs[0] = refs[0].simplify();
    }

    /**
     * @see org.jnode.vm.compiler.ir.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
     */
    public void generateCode(CodeGenerator<T> cg) {
        cg.generateCodeFor(this);
    }
}
