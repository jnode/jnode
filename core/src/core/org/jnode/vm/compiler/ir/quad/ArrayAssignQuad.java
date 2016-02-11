/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Levente S\u00e1ntha
 */
public class ArrayAssignQuad extends AssignQuad {
    private int type;
    private Operand[] refs;

    public ArrayAssignQuad(int address, IRBasicBlock block, int lhsIndex, int indIndex, int refIndex, int type) {
        super(address, block, lhsIndex);
        this.type = type;
        this.refs = new Operand[]{getOperand(indIndex), getOperand(refIndex)};
        getLHS().setTypeFromJvmType(type);
    }

    public Variable getRef() {
        return (Variable) refs[1];
    }

    public Operand getInd() {
        return refs[0];
    }

    @Override
    public Operand propagate(Variable operand) {
        return operand;
    }

    @Override
    public int getLHSLiveAddress() {
        return this.getAddress() + 1;
    }

    @Override
    public Operand[] getReferencedOps() {
        return refs;
    }

    @Override
    public void doPass2() {
        refs[0] = refs[0].simplify();
        refs[1] = refs[1].simplify();
    }

    @Override
    public void generateCode(CodeGenerator cg) {
        cg.generateCodeFor(this);
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return getAddress() + ": " + getLHS() + " = " + refs[1] + '[' + refs[0] + ']';
    }
}
