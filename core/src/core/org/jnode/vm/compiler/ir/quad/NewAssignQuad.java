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

import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Levente S\u00e1ntha
 */
public class NewAssignQuad<T> extends AssignQuad<T> {
    private Operand<T>[] refs;
    private VmConstClass clazz;

    public NewAssignQuad(int address, IRBasicBlock<T> block, int lhsIndex, VmConstClass clazz) {
        super(address, block, lhsIndex);
        this.clazz = clazz;
        refs = new Operand[0];
        getLHS().setType(Operand.REFERENCE);
    }

    public VmConstClass getType() {
        return clazz;
    }

    @Override
    public Operand<T> propagate(Variable<T> operand) {
        return operand;
    }

    @Override
    public int getLHSLiveAddress() {
        return getAddress() + 1;
    }

    @Override
    public Operand<T>[] getReferencedOps() {
        return refs;
    }

    @Override
    public void doPass2() {

    }

    @Override
    public void generateCode(CodeGenerator<T> cg) {
        cg.generateCodeFor(this);
    }

    public String toString() {
        return getAddress() + ": " + getLHS().toString() + " = new " + clazz.getClassName();
    }
}
