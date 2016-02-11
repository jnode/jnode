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

/**
 * @author Levente S\u00e1ntha
 */
public class CheckcastQuad<T> extends Quad<T> {
    private Operand<T>[] refs;
    private VmConstClass clazz;

    public CheckcastQuad(int address, IRBasicBlock<T> block, VmConstClass clazz, int refIndex) {
        super(address, block);
        this.refs = new Operand[]{getOperand(refIndex)};
        this.clazz = clazz;
    }

    @Override
    public Operand<T> getDefinedOp() {
        return null;
    }

    @Override
    public Operand<T>[] getReferencedOps() {
        return refs;
    }

    public VmConstClass getConstClass() {
        return clazz;
    }

    public Operand getRef() {
        return refs[0];
    }

    @Override
    public void doPass2() {
        refs[0] = refs[0].simplify();
    }

    @Override
    public void generateCode(CodeGenerator<T> cg) {
        cg.generateCodeFor(this);
    }

    public String toString() {
        return getAddress() + ": checkcast " + refs[0] + " " + clazz.getClassName();
    }
}
