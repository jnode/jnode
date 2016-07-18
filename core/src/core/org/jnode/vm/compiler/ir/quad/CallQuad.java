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

import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;

/**
 * @author Levente S\u00e1ntha
 */
public abstract class CallQuad<T> extends Quad<T> {
    protected VmConstMethodRef methodRef;
    protected Operand<T>[] refs;

    protected CallQuad(int address, IRBasicBlock<T> block,
                       VmConstMethodRef methodRef, int[] offs) {
        super(address, block);
        this.methodRef = methodRef;
        refs = new Operand[offs.length];
        for (int i = 0; i < offs.length; i++) {
            refs[i] = getOperand(offs[i]);
        }
    }

    @Override
    public Operand<T> getDefinedOp() {
        return null;
    }

    @Override
    public Operand<T>[] getReferencedOps() {
        return refs;
    }

    @Override
    public void doPass2() {
        for (int i = 0; i < refs.length; i++)
            refs[i] = refs[i].simplify();
    }

    public VmConstMethodRef getMethodRef() {
        return methodRef;
    }
}
