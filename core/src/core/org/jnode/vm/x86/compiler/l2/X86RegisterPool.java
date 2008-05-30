/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm.x86.compiler.l2;

import org.jnode.assembler.x86.X86Register;
import org.jnode.util.BootableArrayList;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.RegisterPool;

/**
 * @author Madhu Siddalingaiah
 * @author Levente S\u00e1ntha
 */
public class X86RegisterPool extends RegisterPool<X86Register> {

    private final BootableArrayList<X86Register> registers;

    public X86RegisterPool() {
        registers = new BootableArrayList<X86Register>();
        registers.add(X86Register.EDX);
        registers.add(X86Register.ECX);
        registers.add(X86Register.EBX);
        registers.add(X86Register.EAX);
        // registers.add(Register.ESI);
        // registers.add(Register.EDI);
        // // not sure what to do with ESI and EDI just yet...
    }

    /**
     * @see org.jnode.vm.compiler.ir.RegisterPool#request(int)
     */
    public X86Register request(int type) {
        if (type == Operand.LONG) {
            return null;
        }
        if (/* type == Operand.FLOAT || */type == Operand.DOUBLE) {
            // throw new IllegalArgumentException("floats and double not yet
            // supported");
            return null;
        }
        int size = registers.size();
        if (size == 0) {
            return null;
        } else {
            return registers.remove(size - 1);
        }
    }

    /**
     * @see org.jnode.vm.compiler.ir.RegisterPool#release(java.lang.Object)
     */
    public void release(X86Register register) {
        registers.add(register);
    }

    /**
     * @see org.jnode.vm.compiler.ir.RegisterPool#supports3AddrOps()
     */
    public boolean supports3AddrOps() {
        return false;
    }
}
