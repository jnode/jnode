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
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.PhiOperand;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Madhu Siddalingaiah
 */
public class PhiAssignQuad<T> extends AssignQuad<T> {
    private PhiOperand<T> phi;

    /**
     * @param address
     * @param block
     * @param lhsIndex
     */
    public PhiAssignQuad(int address, IRBasicBlock<T> block, int lhsIndex) {
        super(address, block, lhsIndex);
        phi = new PhiOperand<T>();
    }

    /**
     * @param dfb
     * @param def
     */
    public PhiAssignQuad(IRBasicBlock<T> dfb, int lhsIndex) {
        this(dfb.getStartPC(), dfb, lhsIndex);
    }

    public PhiOperand<T> getPhiOperand() {
        return phi;
    }

    public Operand<T> propagate(Variable<T> operand) {
        return operand;
    }

    public int getLHSLiveAddress() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Operand<T>[] getReferencedOps() {
        // TODO Auto-generated method stub
        return null;
    }

    public void doPass2() {
        phi.simplify();
    }

    public void generateCode(CodeGenerator cg) {
        // TODO Auto-generated method stub

    }

    public boolean equals(Object obj) {
        if (obj instanceof PhiAssignQuad) {
            PhiAssignQuad<T> paq = (PhiAssignQuad<T>) obj;
            return getLHS().equals(paq.getLHS());
        }
        return false;
    }

    public String toString() {
        if (isDeadCode()) {
            return getAddress() + ": " + "            nop (pruned phi)";
        } else {
            return getAddress() + ": " + getLHS() + " = " + phi;
        }
    }
}
