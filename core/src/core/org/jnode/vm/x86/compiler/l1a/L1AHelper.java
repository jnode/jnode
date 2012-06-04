/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.vm.JvmType;
import org.jnode.vm.compiler.IllegalModeException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class L1AHelper {

    static void assertCondition(boolean cond, String message) {
        if (!cond)
            throw new Error("assert failed: " + message);
    }

    static void assertCondition(boolean cond, String message, Object param) {
        if (!cond) {
            throw new Error("assert failed: " + message + param);
        }
    }

    /**
     * Gets the 64-bit equivalent of the given 32-bit register.
     *
     * @param eContext
     * @param src
     * @return the 64-bit register.
     */
    static GPR64 get64BitReg(EmitterContext eContext, GPR src) {
        return (GPR64) eContext.getGPRPool().getRegisterInSameGroup(src, JvmType.LONG);
    }

    /**
     * Release a register.
     *
     * @param eContext
     * @param reg
     */
    static void releaseRegister(EmitterContext eContext, X86Register reg) {
        final X86RegisterPool pool = eContext.getGPRPool();
        pool.release(reg);
    }

    /**
     * Request two register for a 8-byte item.
     *
     * @param eContext
     * @param jvmType
     * @return
     */
    static DoubleWordItem requestDoubleWordRegisters(
        EmitterContext eContext, int jvmType) {
        final X86RegisterPool pool = eContext.getGPRPool();
        final X86Assembler os = eContext.getStream();
        final ItemFactory ifac = eContext.getItemFactory();
        final DoubleWordItem result;
        if (os.isCode32()) {
            final X86Register.GPR lsb = (X86Register.GPR) requestRegister(eContext, JvmType.INT, false);
            final X86Register.GPR msb = (X86Register.GPR) requestRegister(eContext, JvmType.INT, false);
            result = ifac.createReg(eContext, jvmType, lsb,
                msb);
            pool.transferOwnerTo(lsb, result);
            pool.transferOwnerTo(msb, result);
        } else {
            final GPR64 reg = (GPR64) requestRegister(eContext, jvmType, false);
            result = ifac.createReg(eContext, jvmType, reg);
            pool.transferOwnerTo(reg, result);
        }
        return result;
    }

    /**
     * Request two register for a 8-byte item.
     *
     * @param eContext
     * @param jvmType
     * @param lsb
     * @param msb
     * @return
     */
    static DoubleWordItem requestDoubleWordRegisters(
        EmitterContext eContext, int jvmType, X86Register.GPR lsb, X86Register.GPR msb) {
        if (!eContext.getStream().isCode32()) {
            throw new IllegalModeException("Only support in 32-bit mode");
        }
        final X86RegisterPool pool = eContext.getGPRPool();
        final ItemFactory ifac = eContext.getItemFactory();
        requestRegister(eContext, lsb);
        requestRegister(eContext, msb);
        final DoubleWordItem result = ifac.createReg(eContext, jvmType, lsb,
            msb);
        pool.transferOwnerTo(lsb, result);
        pool.transferOwnerTo(msb, result);
        return result;
    }

    /**
     * Request a 64-bit register for a 8-byte item.
     *
     * @param eContext
     * @param jvmType
     * @param reg
     * @return
     */
    static DoubleWordItem requestDoubleWordRegister(
        EmitterContext eContext, int jvmType, GPR64 reg) {
        if (!eContext.getStream().isCode64()) {
            throw new IllegalModeException("Only support in 64-bit mode");
        }
        final X86RegisterPool pool = eContext.getGPRPool();
        final ItemFactory ifac = eContext.getItemFactory();
        requestRegister(eContext, reg);
        final DoubleWordItem result = ifac.createReg(eContext, jvmType, reg);
        pool.transferOwnerTo(reg, result);
        return result;
    }

    /**
     * Request a register for calcuation, not tied to an item. Make sure to
     * release the register afterwards.
     *
     * @param eContext
     * @param reg
     */
    static void requestRegister(EmitterContext eContext, X86Register reg) {
        final X86RegisterPool pool = eContext.getGPRPool();
        if (!pool.isFree(reg)) {
            final Item i = (Item) pool.getOwner(reg);
            i.spill(eContext, reg);
            assertCondition(pool.isFree(reg),
                "register is not free after spill");
        }
        assertCondition(pool.request(reg), "Request of register failed: ", reg);
    }

    /**
     * Request a register of a given type, not tied to an item. Make sure to
     * release the register afterwards.
     *
     * @param eContext
     * @param type
     * @param supportsBits8
     * @return
     */
    static X86Register requestRegister(EmitterContext eContext, int type,
                                       boolean supportsBits8) {
        final X86RegisterPool pool = eContext.getGPRPool();
        X86Register r = pool.request(type, supportsBits8);
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
     * @param eContext
     * @param reg      the register to reserve
     * @param it       the item requiring the register
     */
    static void requestRegister(EmitterContext eContext, X86Register reg,
                                Item it) {
        final X86RegisterPool pool = eContext.getGPRPool();

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
            assertCondition(pool.request(reg, it), "Request of register failed: ", reg);
        }
    }

    /**
     * Request one register for a single word item.
     *
     * @param eContext
     * @param jvmType
     * @param supportsBits8
     * @return
     */
    static WordItem requestWordRegister(EmitterContext eContext,
                                        int jvmType, boolean supportsBits8) {
        final X86RegisterPool pool = eContext.getGPRPool();
        final ItemFactory ifac = eContext.getItemFactory();
        final X86Register reg = requestRegister(eContext, jvmType,
            supportsBits8);
        final WordItem result = ifac.createReg(eContext, jvmType, reg);
        pool.transferOwnerTo(reg, result);
        return result;
    }

    /**
     * Request specific one register for a single word item.
     *
     * @param eContext
     * @param jvmType
     * @param reg
     * @return
     */
    static WordItem requestWordRegister(EmitterContext eContext,
                                        int jvmType, X86Register reg) {
        final X86RegisterPool pool = eContext.getGPRPool();
        final ItemFactory ifac = eContext.getItemFactory();
        assertCondition(pool.request(reg), "Request of register failed: ", reg);
        final WordItem result = ifac.createReg(eContext, jvmType, reg);
        pool.transferOwnerTo(reg, result);
        return result;
    }
}
