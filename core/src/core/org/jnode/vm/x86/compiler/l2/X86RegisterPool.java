/*
 * $Id$
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
		registers.add(Register.EAX);
		registers.add(Register.EBX);
		registers.add(Register.ECX);
		registers.add(Register.EDX);
        //registers.add(Register.ESI);
        //registers.add(Register.EDI);
		// not sure what to do with ESI and EDI just yet...
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.RegisterPool#request(int)
	 */
	public Object request(int type) {
		if (type == Operand.LONG) {
			return null;
		}
		if (type == Operand.FLOAT || type == Operand.DOUBLE) {
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
