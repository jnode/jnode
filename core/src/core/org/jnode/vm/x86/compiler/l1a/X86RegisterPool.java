/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.JvmType;

/**
 * @author Madhu Siddalingaiah
 * @author Patrik Reali
 * @author Ewout Prangsma
 * 
 * Handle the pool of registers Taken from l2 compiler. Should be merged with it
 * in the end, integrating the changes.
 * 
 * TODO: merge with l2's version of X86RegisterPool
 */
final class X86RegisterPool {

	/** All available registers and their current usage */
	private final RegisterUsage[] registers;

	/** Length of registers */
	private final int regCount;

	/**
	 * Initialize this instance.
	 */
	public X86RegisterPool() {
		this.registers = initialize();
		this.regCount = registers.length;
	}

	/**
	 * Initialize register pool
	 *  
	 */
	private final RegisterUsage[] initialize() {
		// The order of this array determines the cost of using the register.
		// The cost of a register is lower when its index in this
		// array is higher.
		return new RegisterUsage[] { new RegisterUsage(Register.EAX, false),
				new RegisterUsage(Register.EDX, false),
				new RegisterUsage(Register.ECX, false),
				new RegisterUsage(Register.EBX, false),
				new RegisterUsage(Register.ESI, false) };
		// EDI always points to the statics, do not use
	}

	/**
	 * require a register from the pool
	 * 
	 * @param type
	 *            the register type (from Operand)
	 * @param owner
	 *            the register owner
	 * @return the allocated register or null
	 */
	public Register request(int type, Object owner) {
		return request(type, owner, false);
	}

	/**
	 * require a register from the pool
	 * 
	 * @param type
	 *            the register type (from Operand)
	 * @param owner
	 *            the register owner
	 * @return the allocated register or null
	 */
	public Register request(int type, Object owner, boolean supportBits8) {
		if (type == JvmType.LONG || type == JvmType.DOUBLE) {
			return null;
		}

		for (int i = regCount - 1; i >= 0; i--) {
			final RegisterUsage ru = registers[i];
			if (!supportBits8 || ru.reg.isSuitableForBits8()) {
				if (ru.request(owner)) {
					return ru.reg;
				}
			}
		}
		return null;
	}

	/**
	 * @see org.jnode.vm.compiler.ir.RegisterPool#request(int)
	 */
	public Register request(int type) {
		return request(type, null, false);
	}

	/**
	 * @see org.jnode.vm.compiler.ir.RegisterPool#request(int)
	 */
	public Register request(int type, boolean supportsBits8) {
		return request(type, null, supportsBits8);
	}

	/**
	 * Check whether the given register is free
	 * 
	 * @param register
	 * @return true, when register is free
	 */
	public boolean isFree(Register register) {
		return get(register).isFree();
	}

	/**
	 * Check whether the given register is to be saved by a called method.
	 * 
	 * @param register
	 * @return true, when register is to be saved by a called method
	 */
	public boolean isCallerSaved(Register register) {
		return get(register).isCallerSaved();
	}

	/**
	 * Require a particular register
	 * 
	 * @param register
	 * @return false, if the register is already in use
	 */
	public boolean request(Register register, Object owner) {
		final RegisterUsage ru = get(register);
		final boolean free = ru.isFree();
		if (free) {
			ru.request(owner);
		}
		return free;
	}

	/**
	 * Require a particular register
	 * 
	 * @param register
	 * @return false, if the register is already in use
	 */
	public boolean request(Register register) {
		return request(register, null);
	}

	/**
	 * return the register's owner
	 * 
	 * @param register
	 * @return the owner (may be null if not set or when register not allocated)
	 */
	public Object getOwner(Register register) {
		return get(register).getOwner();
	}

	/**
	 * transfer ownership of a register
	 * 
	 * @param register
	 *            the register to be transferred
	 * @param newOwner
	 *            the register's new owner
	 */
	public void transferOwnerTo(Register register, Object newOwner) {
		get(register).setOwner(newOwner);
	}

	/**
	 * @see org.jnode.vm.compiler.ir.RegisterPool#release(java.lang.Object)
	 */
	public void release(Register register) {
		get(register).release();
	}

	/**
	 * Reset the register pool
	 * 
	 * @param os
	 *            stream for issuing warning messages
	 */
	public void reset(AbstractX86Stream os) {
		boolean inuse = false;
		for (int i = regCount - 1; i >= 0; i--) {
			if (!registers[i].isFree()) {
				os.log("Warning: register in use" + registers[i].reg);
				inuse = true;
			}
		}
		if (inuse) {
			throw new Error("Register(s) in use");
		}
	}

	/**
	 * Gets the register usage for a given register.
	 * 
	 * @param reg
	 * @return
	 */
	private RegisterUsage get(Register reg) {
		for (int i = regCount - 1; i >= 0; i--) {
			final RegisterUsage ru = registers[i];
			if (ru.reg == reg) {
				return ru;
			}
		}
		throw new IllegalArgumentException("Unknown register " + reg);
	}

	/**
	 * Invoke the visit method on the given visitor for all used registers.
	 * 
	 * @param visitor
	 */
	public void visitUsedRegisters(RegisterVisitor visitor) {
		for (int i = 0; i < regCount; i++) {
			final RegisterUsage ru = registers[i];
			if (!ru.isFree()) {
				visitor.visit(ru.reg);
			}
		}
	}

	/**
	 * Convert to a string representation.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuffer buf = new StringBuffer();
		for (int i = 0; i < regCount; i++) {
			buf.append(registers[i].toString());
			buf.append("\n");
		}
		return buf.toString();
	}

	/**
	 * Register usage information for a single register.
	 * 
	 * @author Ewout Prangsma (epr@users.sourceforge.net)
	 */
	private static final class RegisterUsage {
		final Register reg;

		private Object owner;

		private boolean inuse;

		private final boolean callerSaved;

		/**
		 * Initialize this instance.
		 * 
		 * @param reg
		 */
		public RegisterUsage(Register reg, boolean callerSaved) {
			this.reg = reg;
			this.inuse = false;
			this.callerSaved = callerSaved;
		}

		/**
		 * Register this register for the given owner.
		 * 
		 * @param owner
		 * @return True if request succeeds, false if this register is already
		 *         used.
		 */
		public boolean request(Object owner) {
			if (this.inuse) {
				return false;
			} else {
				this.owner = owner;
				this.inuse = true;
				return true;
			}
		}

		/**
		 * Release the given owner from this register.
		 */
		public void release() {
			this.owner = null;
			this.inuse = false;
		}

		/**
		 * Is this register currently not used.
		 * 
		 * @return
		 */
		public boolean isFree() {
			return (!this.inuse);
		}

		/**
		 * Gets the current owner of this register.
		 * 
		 * @return
		 */
		public Object getOwner() {
			return owner;
		}

		/**
		 * Sets the current owner of this register.
		 */
		public void setOwner(Object owner) {
			if (!this.inuse) {
				throw new IllegalStateException("Must be inuse");
			}
			this.owner = owner;
		}

		/**
		 * Is this register saved by any called method.
		 * 
		 * @return
		 */
		public boolean isCallerSaved() {
			return callerSaved;
		}

		/**
		 * Convert to a string representation.
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			if (inuse) {
				return reg + " used by " + owner;
			} else {
				return reg + " free";
			}
		}
	}
}