/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.vm.x86.compiler.l2;

import org.jnode.assembler.x86.Register;
import org.jnode.util.BootableArrayList;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.RegisterPool;

/**
 * @author Madhu Siddalingaiah
 * @author Levente Sántha
 * 
 */
public class X86RegisterPool extends RegisterPool {
	BootableArrayList registers;

	public X86RegisterPool() {
		registers = new BootableArrayList();
		registers.add(Register.EDX);
		registers.add(Register.ECX);
		registers.add(Register.EBX);
		registers.add(Register.EAX);
//        registers.add(Register.ESI);
//        registers.add(Register.EDI);
//		// not sure what to do with ESI and EDI just yet...
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.RegisterPool#request(int)
	 */
	public Object request(int type) {
		if (type == Operand.LONG) {
			return null;
		}
		if (/*type == Operand.FLOAT || */type == Operand.DOUBLE) {
			//throw new IllegalArgumentException("floats and double not yet supported");
            return null;
		}
        int size = registers.size();
        if(size == 0){
            return null;
        }else{
		    return registers.remove(size - 1);
        }
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.RegisterPool#release(java.lang.Object)
	 */
	public void release(Object register) {
		registers.add(register);
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.RegisterPool#supports3AddrOps()
	 */
	public boolean supports3AddrOps() {
		return false;
	}
}
