/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.Register;
import org.jnode.vm.JvmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class L1AHelper {

	static final void assertCondition(boolean cond, String message) {
		if (!cond)
			throw new Error("assert failed: " + message);
	}

	static final void assertCondition(boolean cond, String message, Object param) {
		if (!cond)
			throw new Error("assert failed: " + message + param);
	}

	/**
	 * Release a register.
	 * 
	 * @param reg
	 */
	static final void releaseRegister(EmitterContext eContext, Register reg) {
		final X86RegisterPool pool = eContext.getPool();
		pool.release(reg);
	}

	/**
	 * Request two register for a 8-byte item.
	 */
	static final DoubleWordItem requestDoubleWordRegisters(
			EmitterContext eContext, int jvmType) {
		final X86RegisterPool pool = eContext.getPool();
		final Register lsb = requestRegister(eContext, JvmType.INT, false);
		final Register msb = requestRegister(eContext, JvmType.INT, false);
		final DoubleWordItem result = DoubleWordItem.createReg(jvmType, lsb,
				msb);
		pool.transferOwnerTo(lsb, result);
		pool.transferOwnerTo(msb, result);
		return result;
	}

	/**
	 * Request two register for a 8-byte item.
	 */
	static final DoubleWordItem requestDoubleWordRegisters(
			EmitterContext eContext, int jvmType, Register lsb, Register msb) {
		final X86RegisterPool pool = eContext.getPool();
		requestRegister(eContext, lsb);
		requestRegister(eContext, msb);
		final DoubleWordItem result = DoubleWordItem.createReg(jvmType, lsb,
				msb);
		pool.transferOwnerTo(lsb, result);
		pool.transferOwnerTo(msb, result);
		return result;
	}

	/**
	 * Request a register for calcuation, not tied to an item. Make sure to
	 * release the register afterwards.
	 * 
	 * @param reg
	 */
	static final void requestRegister(EmitterContext eContext, Register reg) {
		final X86RegisterPool pool = eContext.getPool();
		if (!pool.isFree(reg)) {
			final Item i = (Item) pool.getOwner(reg);
			i.spill(eContext, reg);
			assertCondition(pool.isFree(reg),
					"register is not free after spill");
		}
		pool.request(reg);
	}

	/**
	 * Request a register of a given type, not tied to an item. Make sure to
	 * release the register afterwards.
	 */
	static final Register requestRegister(EmitterContext eContext, int type,
			boolean supportsBits8) {
		final X86RegisterPool pool = eContext.getPool();
		Register r = pool.request(type, supportsBits8);
		if (r == null) {
			eContext.getVStack().push(eContext);
			r = pool.request(type, supportsBits8);
		}
		assertCondition(r != null, "failed to request register");
		return r;
	}

	/**
	 * reserve a register for an item. The item is not loaded with the register.
	 * The register is spilled if another item holds it.
	 * 
	 * @param reg
	 *            the register to reserve
	 * @param it
	 *            the item requiring the register
	 */
	static final void requestRegister(EmitterContext eContext, Register reg,
			Item it) {
		final X86RegisterPool pool = eContext.getPool();

		// check item doesn't already use register
		if (!it.uses(reg)) {
			if (!pool.isFree(reg)) {
				//TODO: spill register; make sure that the stack items
				// and floating items are handled correctly
				final Item i = (Item) pool.getOwner(reg);
				i.spill(eContext, reg);
				assertCondition(pool.isFree(reg),
						"register is not free after spill");
			}
			pool.request(reg, it);
		}
	}

	/**
	 * Request one register for a 4-byte item.
	 */
	static final WordItem requestWordRegister(EmitterContext eContext,
			int jvmType, boolean supportsBits8) {
		final X86RegisterPool pool = eContext.getPool();
		final Register reg = requestRegister(eContext, JvmType.INT,
				supportsBits8);
		final WordItem result = WordItem.createReg(jvmType, reg);
		pool.transferOwnerTo(reg, result);
		return result;
	}
}