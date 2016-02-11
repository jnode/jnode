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

import org.jnode.vm.JvmType;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Levente S\u00e1ntha
 */
public class NewMultiArrayAssignQuad<T> extends AssignQuad<T> {
    private Operand<T>[] refs;
    private VmConstClass clazz;

    public NewMultiArrayAssignQuad(int address, IRBasicBlock<T> block, int lhsIndex, VmConstClass clazz,
                                   int[] sizeIndexes) {
        super(address, block, lhsIndex);
        this.clazz = clazz;
        refs = new Operand[sizeIndexes.length];
        for (int i = 0; i < sizeIndexes.length; i++) {
            refs[i] = getOperand(sizeIndexes[i]);
        }
        getLHS().setType(Operand.REFERENCE);
    }

    public VmConstClass getComponentType() {
        return clazz;
    }

    public Operand<T>[] getSizes() {
        return refs;
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
        for (int i = 0; i < refs.length; i++) {
            refs[i] = refs[i].simplify();
        }
    }

    @Override
    public void generateCode(CodeGenerator<T> cg) {
        cg.generateCodeFor(this);
    }

    public String toString() {
        String className = clazz.getClassName();
        if (className.charAt(refs.length) == 'L') {
            className = className.substring(refs.length + 1, className.length() - 1);
        } else {
            className = className.substring(refs.length, className.length());
            className = JvmType.toString(JvmType.SignatureToType(className)).toLowerCase();
        }
        String s = getAddress() + ": " + getLHS().toString() + " = new " + className;
        for (Operand<T> ref : refs) {
            s += "[" + ref + "]";
        }
        return s;
    }
}
