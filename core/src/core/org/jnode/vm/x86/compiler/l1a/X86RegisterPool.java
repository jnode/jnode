/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
import org.jnode.vm.JvmType;

/**
 * @author Madhu Siddalingaiah
 * @author Patrik Reali
 * @author Ewout Prangsma
 *         <p/>
 *         Handle the pool of registers Taken from l2 compiler. Should be merged with it
 *         in the end, integrating the changes.
 *         <p/>
 *         TODO: merge with l2's version of X86RegisterPool
 */
abstract class X86RegisterPool {

    /**
     * All available registers and their current usage
     */
    private final RegisterGroupUsage[] registers;

    /**
     * Length of registers
     */
    private final int regCount;

    private final boolean lastFirst;

    /**
     * Minimum index of the register that can be requested if !lastFirst
     */
    private final int minimumRequestIndex;

    /**
     * Index of the last requested register
     */
    private int lastRequestIndex;

    /**
     * Initialize this instance.
     */
    public X86RegisterPool(boolean lastFirst, int minimumRequestIndex) {
        this.lastFirst = lastFirst;
        this.minimumRequestIndex = minimumRequestIndex;
        this.registers = initialize();
        this.regCount = registers.length;
        this.lastRequestIndex = regCount;
    }

    /**
     * Initialize register pool The order of this array determines the cost of
     * using the register. The cost of a register is lower when its index in
     * this array is higher.
     */
    protected abstract RegisterGroupUsage[] initialize();

    /**
     * require a register from the pool
     *
     * @param type  the register type (from Operand)
     * @param owner the register owner
     * @return the allocated register or null
     */
    public final X86Register request(int type, Item owner) {
        return request(type, owner, false);
    }

    /**
     * require a register from the pool
     *
     * @param type  the register type (from Operand)
     * @param owner the register owner
     * @return the allocated register or null
     */
    public final X86Register request(int type, Item owner, boolean supportBits8) {
        if (!lastFirst) {
            for (int i = regCount - 1 - minimumRequestIndex; i >= 0; i--) {
                lastRequestIndex--;
                if (lastRequestIndex < minimumRequestIndex) {
                    lastRequestIndex = regCount - 1;
                }
                final RegisterGroupUsage ru = registers[lastRequestIndex];
                final X86Register reg = ru.request(owner, type, supportBits8);
                if (reg != null) {
                    return reg;
                }
            }
        }
        for (int i = regCount - 1; i >= 0; i--) {
            final RegisterGroupUsage ru = registers[i];
            final X86Register reg = ru.request(owner, type, supportBits8);
            if (reg != null) {
                return reg;
            }
        }
        return null;
    }

    /**
     * @see org.jnode.vm.compiler.ir.RegisterPool#request(int)
     */
    public final X86Register request(int type) {
        return request(type, null, false);
    }

    /**
     * @see org.jnode.vm.compiler.ir.RegisterPool#request(int)
     */
    public final X86Register request(int type, boolean supportsBits8) {
        return request(type, null, supportsBits8);
    }

    /**
     * Check whether the given register is free
     *
     * @param register
     * @return true, when register is free
     */
    public final boolean isFree(X86Register register) {
        return get(register).isFree();
    }

    /**
     * Check whether the given register is to be saved by a called method.
     *
     * @param register
     * @return true, when register is to be saved by a called method
     */
    public final boolean isCallerSaved(X86Register register) {
        return get(register).isCallerSaved();
    }

    /**
     * Require a particular register
     *
     * @param register
     * @return false, if the register is already in use
     */
    public final boolean request(X86Register register, Item owner) {
        final RegisterGroupUsage ru = get(register);
        final boolean free = ru.isFree();
        if (free) {
            ru.request(owner, register);
        }
        return free;
    }

    /**
     * Require a particular register
     *
     * @param register
     * @return false, if the register is already in use
     */
    public final boolean request(X86Register register) {
        return request(register, null);
    }

    /**
     * return the register's owner
     *
     * @param register
     * @return the owner (may be null if not set or when register not allocated)
     */
    public final Object getOwner(X86Register register) {
        return get(register).getOwner();
    }

    /**
     * transfer ownership of a register
     *
     * @param register the register to be transferred
     * @param newOwner the register's new owner
     */
    public final void transferOwnerTo(X86Register register, Item newOwner) {
        get(register).setOwner(newOwner);
    }

    /**
     * @see org.jnode.vm.compiler.ir.RegisterPool#release(java.lang.Object)
     */
    public final void release(X86Register register) {
        get(register).release();
    }

    /**
     * Reset the register pool
     *
     * @param os stream for issuing warning messages
     */
    public final void reset(X86Assembler os) {
        boolean inuse = false;
        for (int i = regCount - 1; i >= 0; i--) {
            if (!registers[i].isFree()) {
                os.log("Warning: register in use"
                    + registers[i].getUsedRegister());
                inuse = true;
            }
        }
        this.lastRequestIndex = regCount;
        if (inuse) {
            throw new Error("Register(s) in use");
        }
    }

    /**
     * Gets the register in the same group as the given register, but now for
     * the new type. E.g. getRegisterInSameGroup(X86Register.RAX, JvmType.INT)
     * returns X86Register.EAX;
     *
     * @param src
     * @param newType
     * @return
     */
    public final X86Register getRegisterInSameGroup(X86Register src, int newType) {
        final RegisterGroupUsage ru = get(src);
        return ru.find(newType);
    }

    /**
     * Gets the register usage for a given register.
     *
     * @param reg
     * @return
     */
    private final RegisterGroupUsage get(X86Register reg) {
        for (int i = regCount - 1; i >= 0; i--) {
            final RegisterGroupUsage ru = registers[i];
            if (ru.contains(reg)) {
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
    public final void visitUsedRegisters(RegisterVisitor visitor) {
        for (int i = 0; i < regCount; i++) {
            final RegisterGroupUsage ru = registers[i];
            if (!ru.isFree()) {
                visitor.visit(ru.getUsedRegister());
            }
        }
    }

    /**
     * Convert to a string representation.
     *
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < regCount; i++) {
            buf.append(registers[i].toString());
            buf.append("\n");
        }
        return buf.toString();
    }

    /**
     * Pool of GPR registers in 32-bit mode.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static final class GPRs32 extends X86RegisterPool {

        /**
         * Initialize this instance.
         */
        public GPRs32() {
            super(false, 2);
        }

        /**
         * Initialize register pool
         */
        protected RegisterGroupUsage[] initialize() {
            // The order of this array determines the cost of using the
            // register.
            // The cost of a register is lower when its index in this
            // array is higher.
            return new RegisterGroupUsage[]{
                new RegisterGroupUsage(new RegisterEntry(X86Register.EAX, JvmType.INT),
                    new RegisterEntry(X86Register.EAX, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.EAX, JvmType.FLOAT), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.EDX, JvmType.INT),
                    new RegisterEntry(X86Register.EDX, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.EDX, JvmType.FLOAT), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.ECX, JvmType.INT),
                    new RegisterEntry(X86Register.ECX, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.ECX, JvmType.FLOAT), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.ESI, JvmType.INT),
                    new RegisterEntry(X86Register.ESI, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.ESI, JvmType.FLOAT), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.EBX, JvmType.INT),
                    new RegisterEntry(X86Register.EBX, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.EBX, JvmType.FLOAT), false),
            };
            // EDI always points to the statics, do not use
        }

    }

    /**
     * Pool of GPR registers in 64-bit mode.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static final class GPRs64 extends X86RegisterPool {

        /**
         * Initialize this instance.
         */
        public GPRs64() {
            super(false, 2);
        }

        /**
         * Initialize register pool
         */
        protected RegisterGroupUsage[] initialize() {
            // The order of this array determines the cost of using the
            // register.
            // The cost of a register is lower when its index in this
            // array is higher.
            return new RegisterGroupUsage[]{
                new RegisterGroupUsage(new RegisterEntry(X86Register.EAX, JvmType.INT),
                    new RegisterEntry(X86Register.EAX, JvmType.FLOAT),
                    new RegisterEntry(X86Register.RAX, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.RAX, JvmType.LONG),
                    new RegisterEntry(X86Register.RAX, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.EDX, JvmType.INT),
                    new RegisterEntry(X86Register.EDX, JvmType.FLOAT),
                    new RegisterEntry(X86Register.RDX, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.RDX, JvmType.LONG),
                    new RegisterEntry(X86Register.RDX, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.ECX, JvmType.INT),
                    new RegisterEntry(X86Register.ECX, JvmType.FLOAT),
                    new RegisterEntry(X86Register.RCX, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.RCX, JvmType.LONG),
                    new RegisterEntry(X86Register.RCX, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.EBX,
                    JvmType.INT), new RegisterEntry(X86Register.EBX, JvmType.FLOAT),
                    new RegisterEntry(X86Register.RBX, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.RBX, JvmType.LONG),
                    new RegisterEntry(X86Register.RBX, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.ESI, JvmType.INT),
                    new RegisterEntry(X86Register.ESI, JvmType.FLOAT),
                    new RegisterEntry(X86Register.RSI, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.RSI, JvmType.LONG),
                    new RegisterEntry(X86Register.RSI, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.R8d, JvmType.INT),
                    new RegisterEntry(X86Register.R8d, JvmType.FLOAT),
                    new RegisterEntry(X86Register.R8, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.R8, JvmType.LONG),
                    new RegisterEntry(X86Register.R8, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.R9d, JvmType.INT),
                    new RegisterEntry(X86Register.R9d, JvmType.FLOAT),
                    new RegisterEntry(X86Register.R9, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.R9, JvmType.LONG),
                    new RegisterEntry(X86Register.R9, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.R10d, JvmType.INT),
                    new RegisterEntry(X86Register.R10d, JvmType.FLOAT),
                    new RegisterEntry(X86Register.R10, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.R10, JvmType.LONG),
                    new RegisterEntry(X86Register.R10, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.R11d, JvmType.INT),
                    new RegisterEntry(X86Register.R11d, JvmType.FLOAT),
                    new RegisterEntry(X86Register.R11, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.R11, JvmType.LONG),
                    new RegisterEntry(X86Register.R11, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.R13d, JvmType.INT),
                    new RegisterEntry(X86Register.R13d, JvmType.FLOAT),
                    new RegisterEntry(X86Register.R13, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.R13, JvmType.LONG),
                    new RegisterEntry(X86Register.R13, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.R14d, JvmType.INT),
                    new RegisterEntry(X86Register.R14d, JvmType.FLOAT),
                    new RegisterEntry(X86Register.R14, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.R14, JvmType.LONG),
                    new RegisterEntry(X86Register.R14, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.R15d, JvmType.INT),
                    new RegisterEntry(X86Register.R15d, JvmType.FLOAT),
                    new RegisterEntry(X86Register.R15, JvmType.REFERENCE),
                    new RegisterEntry(X86Register.R15, JvmType.LONG),
                    new RegisterEntry(X86Register.R15, JvmType.DOUBLE), false)};
            // RDI always points to the statics, do not use
            // R12 always points to the VmProcessor, do not use
        }

    }

    /**
     * Pool of XMM registers in 32-bit mode.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static final class XMMs32 extends X86RegisterPool {

        /**
         * Initialize this instance.
         */
        public XMMs32() {
            super(false, 0);
        }

        /**
         * Initialize register pool
         */
        protected RegisterGroupUsage[] initialize() {
            // The order of this array determines the cost of using the
            // register.
            // The cost of a register is lower when its index in this
            // array is higher.
            return new RegisterGroupUsage[]{
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM0, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM0, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM1, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM1, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM2, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM2, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM3, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM3, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM4, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM4, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM5, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM5, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM6, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM6, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM7, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM7, JvmType.DOUBLE), false)};
        }
    }

    /**
     * Pool of XMM registers in 64-bit mode.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    public static final class XMMs64 extends X86RegisterPool {

        /**
         * Initialize this instance.
         */
        public XMMs64() {
            super(false, 0);
        }

        /**
         * Initialize register pool
         */
        protected RegisterGroupUsage[] initialize() {
            // The order of this array determines the cost of using the
            // register.
            // The cost of a register is lower when its index in this
            // array is higher.
            return new RegisterGroupUsage[]{
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM0, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM0, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM1, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM1, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM2, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM2, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM3, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM3, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM4, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM4, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM5, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM5, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM6, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM6, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM7, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM7, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM8, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM8, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM9, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM9, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM10, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM10, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM11, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM11, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM12, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM12, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM13, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM13, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM14, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM14, JvmType.DOUBLE), false),
                new RegisterGroupUsage(new RegisterEntry(X86Register.XMM15, JvmType.FLOAT),
                    new RegisterEntry(X86Register.XMM15, JvmType.DOUBLE), false)};
        }
    }

    /**
     * Register & type information.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    private static final class RegisterEntry {
        final X86Register reg;

        final int jvmType;

        /**
         * @param reg
         * @param jvmType
         */
        public RegisterEntry(X86Register reg, int jvmType) {
            this.reg = reg;
            this.jvmType = jvmType;
        }
    }

    /**
     * Register usage information for a group of registers that shared bits.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    private static final class RegisterGroupUsage {
        /**
         * The registers in this group
         */
        private final RegisterEntry[] regs;

        private Item owner;

        private boolean inuse;

        private final boolean callerSaved;

        /**
         * The register in this group that is actually in use
         */
        private X86Register usedReg;

        /**
         * Initialize this instance.
         *
         * @param reg
         */
        public RegisterGroupUsage(RegisterEntry reg, boolean callerSaved) {
            this(new RegisterEntry[]{reg}, callerSaved);
        }

        /**
         * Initialize this instance.
         *
         * @param reg1
         * @param reg2
         */
        public RegisterGroupUsage(RegisterEntry reg1, RegisterEntry reg2,
                                  boolean callerSaved) {
            this(new RegisterEntry[]{reg1, reg2}, callerSaved);
        }

        /**
         * Initialize this instance.
         *
         * @param reg1
         * @param reg2
         * @param reg3
         */
        public RegisterGroupUsage(RegisterEntry reg1, RegisterEntry reg2,
                                  RegisterEntry reg3, boolean callerSaved) {
            this(new RegisterEntry[]{reg1, reg2, reg3}, callerSaved);
        }

        /**
         * Initialize this instance.
         *
         * @param reg1
         * @param reg2
         * @param reg3
         * @param reg4
         */
        public RegisterGroupUsage(RegisterEntry reg1, RegisterEntry reg2,
                                  RegisterEntry reg3, RegisterEntry reg4, boolean callerSaved) {
            this(new RegisterEntry[]{reg1, reg2, reg3, reg4}, callerSaved);
        }

        /**
         * Initialize this instance.
         *
         * @param reg1
         * @param reg2
         * @param reg3
         * @param reg4
         * @param reg5
         */
        public RegisterGroupUsage(RegisterEntry reg1, RegisterEntry reg2,
                                  RegisterEntry reg3, RegisterEntry reg4, RegisterEntry reg5,
                                  boolean callerSaved) {
            this(new RegisterEntry[]{reg1, reg2, reg3, reg4, reg5},
                callerSaved);
        }

        /**
         * Initialize this instance.
         *
         * @param reg
         */
        public RegisterGroupUsage(RegisterEntry[] regs, boolean callerSaved) {
            this.regs = regs;
            this.inuse = false;
            this.callerSaved = callerSaved;
        }

        /**
         * Register this register for the given owner.
         *
         * @param owner
         * @return The register if request succeeds, null if this register group
         *         is already used.
         */
        public X86Register request(Item owner, int jvmType, boolean supportBits8) {
            final int cnt = regs.length;
            for (int i = 0; i < cnt; i++) {
                final RegisterEntry re = regs[i];
                if (re.jvmType == jvmType) {
                    if (!supportBits8 || re.reg.isSuitableForBits8()) {
                        return request(owner, re.reg);
                    }
                }
            }
            return null;
        }

        /**
         * Register this register for the given owner.
         *
         * @param owner
         * @return The register if request succeeds, null if this register group
         *         is already used.
         */
        public X86Register request(Item owner, X86Register reg) {
            if (this.inuse) {
                return null;
            } else {
                this.owner = owner;
                this.inuse = true;
                this.usedReg = reg;
                return reg;
            }
        }

        /**
         * Release the given owner from this register.
         */
        public void release() {
            this.owner = null;
            this.inuse = false;
            this.usedReg = null;
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
        public void setOwner(Item owner) {
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
         * Is the given register contains in this instance.
         *
         * @param reg
         * @return true / false
         */
        public boolean contains(X86Register reg) {
            final int cnt = regs.length;
            for (int i = 0; i < cnt; i++) {
                final RegisterEntry re = regs[i];
                if (re.reg == reg) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Gets the register in this group with the given type.
         *
         * @param type
         * @return The register found, or null if not found.
         */
        public final X86Register find(int type) {
            final int cnt = regs.length;
            for (int i = 0; i < cnt; i++) {
                final RegisterEntry re = regs[i];
                if (re.jvmType == type) {
                    return re.reg;
                }
            }
            return null;
        }

        /**
         * Gets the register in this group that is actually in use.
         *
         * @return
         */
        public X86Register getUsedRegister() {
            return usedReg;
        }

        /**
         * Convert to a string representation.
         *
         * @see java.lang.Object#toString()
         */
        public String toString() {
            if (inuse) {
                return usedReg + " used by " + owner;
            } else {
                return "free";
            }
        }
    }
}
