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
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;

/**
 * @author Levente S\u00e1ntha
 */
public class StaticCallQuad<T> extends CallQuad<T> {

    public StaticCallQuad(int address, IRBasicBlock<T> block, VmConstMethodRef methodRef, int[] offs) {
        super(address, block, methodRef, offs);
    }

    @Override
    public void generateCode(CodeGenerator<T> cg) {
        cg.generateCodeFor(this);
    }

    @Override
    public String toString() {
        String s = getAddress() + ": " + methodRef.getClassName() + "." + methodRef.getName() + '(';
        for (int i = refs.length - 1; i >= 0; i--) {
            if (i < refs.length - 1) {
                s += ", ";
            }
            s += refs[i];
        }
        s += ')';
        return s;
    }

}
