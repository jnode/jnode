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

import org.jnode.vm.classmgr.VmConstFieldRef;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.StaticField;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Levente S\u00e1ntha
 */
public class RefAssignQuad<T> extends AssignQuad<T> {
    private StaticField rhs;
    private Operand<T> refs[];

    /**
     * @param address
     * @param block
     * @param lhsIndex
     * @param refIndex
     */
    public RefAssignQuad(int address, IRBasicBlock<T> block, int lhsIndex,
                         VmConstFieldRef fieldRef, int refIndex) {
        super(address, block, lhsIndex);
        this.rhs = new StaticField(fieldRef);
        refs = new Operand[]{getOperand(refIndex)};
        getLHS().setTypeFromJvmType(fieldRef.getResolvedVmField().getType().getJvmType());
    }

    public VmConstFieldRef getFieldRef() {
        return rhs.getFiledRef();
    }

    public Operand getRef() {
        return refs[0];
    }

    /**
     * @see org.jnode.vm.compiler.ir.quad.Quad#getReferencedOps()
     */
    public Operand<T>[] getReferencedOps() {
        return refs;
    }

    public Operand<T> propagate(Variable<T> operand) {
        return operand;
    }

    public void doPass2() {
        refs[0] = refs[0].simplify();
    }

    public void generateCode(CodeGenerator<T> cg) {
        cg.generateCodeFor(this);
    }

    public int getLHSLiveAddress() {
        return this.getAddress() + 1;
    }

    public String toString() {
        return getAddress() + ": " + getLHS().toString() + " = " + refs[0] + "." + rhs.toString();
    }
}
