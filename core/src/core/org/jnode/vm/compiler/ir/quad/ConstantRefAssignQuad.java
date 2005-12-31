/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
     * @return
     */
    public Constant<T> getRHS() {
        return rhs;
    }

    /**
     * @param operand
     * @return
     */
    public Operand<T> propagate(Variable<T> operand) {
        setDeadCode(true);
        return rhs;
    }

    /**
     * @see org.jnode.vm.compiler.ir.Quad#doPass2(org.jnode.util.BootableHashMap)
     */
    public void doPass2() {
    }

    /**
     * @see org.jnode.vm.compiler.ir.Quad#generateCode(org.jnode.vm.compiler.ir.CodeGenerator)
     */
    public void generateCode(CodeGenerator<T> cg) {
        cg.generateCodeFor(this);
    }

    /**
     * @see org.jnode.vm.compiler.ir.AssignQuad#getLHSLiveAddress()
     */
    public int getLHSLiveAddress() {
        return this.getAddress() + 1;
    }
}
