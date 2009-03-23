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
 
package org.jnode.vm.compiler.ir.quad;

import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.Constant;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Madhu Siddalingaiah
 */
public class ConstantRefAssignQuad<T> extends AssignQuad<T> {
    private Constant<T> rhs;

    /**
     * @param address
     * @param block
     * @param lhsIndex
     */
    public ConstantRefAssignQuad(int address, IRBasicBlock<T> block, int lhsIndex,
                                 Constant<T> rhs) {
        super(address, block, lhsIndex);
        this.rhs = rhs;
    }

    /**
     * @see org.jnode.vm.compiler.ir.quad.Quad#getReferencedOps()
     */
    public Operand<T>[] getReferencedOps() {
        return null;
    }

    public String toString() {
        return getAddress() + ": " + getLHS().toString() + " = "
            + rhs.toString();
    }

    /**
     * @return the RHS of the assignment
     */
    public Constant<T> getRHS() {
        return rhs;
    }

    public Operand<T> propagate(Variable<T> operand) {
        setDeadCode(true);
        return rhs;
    }

    public void doPass2() {
    }

    public void generateCode(CodeGenerator<T> cg) {
        cg.generateCodeFor(this);
    }

    public int getLHSLiveAddress() {
        return this.getAddress() + 1;
    }
}
