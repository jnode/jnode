/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import java.util.Iterator;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.util.BootableArrayList;
import org.jnode.util.BootableHashMap;
import org.jnode.vm.compiler.ir.Operand;

/**
* @author Madhu Siddalingaiah
* @author Patrik Reali
 *
 * Handle the pool of registers
 * Taken from l2 compiler. Should be merged with it in the end, integrating the changes.
 */

//TODO: merge with l2's version of X86RegisterPool
//TODO: not all registers are equivalent; try to return EBX and ECX when possible, spare EAX, EDX

final class X86RegisterPool extends org.jnode.vm.compiler.ir.RegisterPool {

	private BootableArrayList registers;
	private BootableHashMap allocated;

	public X86RegisterPool() {
		initialize();
	}
	
	/**
	 * Initialize register pool
	 *
	 */
	private void initialize() {
		registers = new BootableArrayList();
		registers.add(Register.EAX);
		registers.add(Register.EBX);
		registers.add(Register.ECX);
		registers.add(Register.EDX);
		registers.add(Register.ESI);
		// EDI always points to the statics, do not use
		allocated = new BootableHashMap();
	}
	
	/**
	 * require a register from the pool
	 * 
	 * @param type the register type (from Operand)
	 * @param owner the register owner
	 * @return the allocated register or null
	 */
	public Object request(int type, Object owner) {
		if (type == Operand.LONG) {
			return null;
		}
		if (type == Operand.FLOAT || type == Operand.DOUBLE) {
			return null;
		}
		if (registers.size() == 0) {
			return null;
		} else {
			Object reg =registers.remove(registers.size() - 1);
			allocated.put(reg, owner);
			return reg;
		}
	}
	

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.RegisterPool#request(int)
	 */
	public Object request(int type) {
		return request(type, null);
	}
	
	/**
	 * Check whether the given register is free
	 * 
	 * @param register
	 * @return true, when register is free
	 */
	public boolean isFree(Object register) {
		return registers.contains(register);
	}
	
	/**
	 * Require a particular register
	 * 
	 * @param register
	 * @return false, if the register is already in use
	 */
	public boolean request(Object register, Object owner) {
		boolean free = isFree(register);
		if (free) {
			registers.remove(register);
			allocated.put(register, owner);
		}
		return free;
	}

	/**
	 * Require a particular register
	 * 
	 * @param register
	 * @return false, if the register is already in use
	 */
	public boolean request(Object register) {
		return request(register, null);
	}

	/**
	 * return the register's owner
	 * 
	 * @param register
	 * @return the owner (may be null if not set or when register not allocated)
	 */
	public Object getOwner(Object register) {
		return allocated.get(register);
	}
	
	/**
	 * transfer ownership of a register
	 * 
	 * @param register the register to be transferred
	 * @param newOwner the register's new owner
	 */
	public void transferOwnerTo(Object register, Object newOwner) {
		allocated.put(register, newOwner);
	}
	
	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.RegisterPool#release(java.lang.Object)
	 */
	public void release(Object register) {
		registers.add(register);
		allocated.remove(register);
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.RegisterPool#supports3AddrOps()
	 */
	public boolean supports3AddrOps() {
		return false;
	}

	/**
	 * Reset the register pool
	 * 
	 * @param os stream for issuing warning messages
	 */
	public void reset(AbstractX86Stream os) {
		if (allocated.size() != 0) {
			// resetting a register pool with items in use
			Iterator inUse = allocated.keySet().iterator();
			while (inUse.hasNext()) {
				os.log("Warning: register in use"+inUse.next().toString());
			}
		}
		initialize();
	}

}
