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

package org.jnode.assembler.x86;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.vm.CpuID;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.X86CpuID;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
public abstract class X86Assembler extends NativeStream implements X86Constants {

    /** Current mode is 32-bit */
    protected final boolean code32;
    /** Current mode is 64-bit */
    protected final boolean code64;

	protected final X86CpuID cpuId;

	protected final Mode mode;

	/**
	 * Initialize this instance
	 * 
	 * @param cpuId
	 */
	public X86Assembler(X86CpuID cpuId, Mode mode) {
		this.cpuId = cpuId;
		this.mode = mode;
        this.code32 = mode.is32();
        this.code64 = mode.is64();
	}

	/**
	 * Align on a given value
	 * 
	 * @param value
	 * @return The number of bytes needed to align.
	 */
	public abstract int align(int value);

    /**
     * Gets a 32-bit integer from a given offset.
     * @param offset
     * @return int
     */
	public abstract int get32(int offset);

    /**
     * Gets an 8-bit integer from a given offset.
     * @param offset
     * @return int
     */
	public abstract int get8(int offset);

	/**
	 * Returns the base address.
	 * 
	 * @return long
	 */
	public abstract long getBaseAddr();

	/**
	 * Return the actual bytes. This array may be longer then getLength() *
	 * 
	 * @return The actual bytes
	 */
	public abstract byte[] getBytes();

	/**
	 * Gets the identification of the CPU for which this stream will produce
	 * data.
	 */
	public final CpuID getCPUID() {
		return cpuId;
	}

	/**
	 * Get the length in bytes of valid data
	 * 
	 * @return the length of valid data
	 */
	public abstract int getLength();

	/**
	 * Gets the operating mode.
	 * 
	 * @return the target operating mode.
	 */
	public final X86Constants.Mode getMode() {
		return mode;
	}

	/**
	 * Gets an objectref for a given object.
	 * 
	 * @param keyObj
	 * @return ObjectRef
	 */
	public abstract ObjectRef getObjectRef(Object keyObj);

	/**
	 * Gets all references of objects as instanceof ObjectRef
	 * 
	 * @return Collection
	 */
	public abstract Collection getObjectRefs();

	/**
	 * @return ObjectResolver
	 */
	public abstract ObjectResolver getResolver();

	/**
	 * Gets all unresolved references of objects as instanceof ObjectRef
	 * 
	 * @return Collection
	 */
	public abstract Collection getUnresolvedObjectRefs();

	/**
	 * Gets the size of a word in bytes.
	 * 
	 * @return 4 or 8 depending on the operating mode.
	 */
	public final int getWordSize() {
		return mode.is32() ? 4 : 8;
	}

	/**
	 * Gets the identification of the CPU for which this stream will produce
	 * data.
	 */
	public final X86CpuID getX86CPUID() {
		return cpuId;
	}

	/**
	 * Are there unresolved references?
	 * 
	 * @return True if there are unresolved references, false otherwise
	 */
	public abstract boolean hasUnresolvedObjectRefs();

    /**
     * @return Returns the code32.
     */
    public final boolean isCode32() {
        return this.code32;
    }
    /**
     * @return Returns the code64.
     */
    public final boolean isCode64() {
        return this.code64;
    }

	/**
	 * Is logging enabled. This method will only return true on on debug like
	 * implementations.
	 * 
	 * @return boolean
	 */
	public abstract boolean isLogEnabled();

	/**
	 * Is this a text stream. In that case there is not actual generated code,
	 * so length calculations are invalid.
	 * 
	 * @return true or false
	 */
	public abstract boolean isTextStream();

	/**
	 * Write a log message. This method is only implemented on debug like
	 * implementations.
	 * 
	 * @param msg
	 */
	public abstract void log(Object msg);

    /**
     * Sets a 32-bit integer at a given offset.
     * @param offset
     * @param v32
     */
	public abstract void set32(int offset, int v32);

    /**
     * Sets an 8-bit integer at a given offset.
     * @param offset
     * @param v8
     */
	public abstract void set8(int offset, int v8);

    /**
     * Sets the target offset of a given label to the current position.
     * @param label
     */
	public abstract ObjectRef setObjectRef(Object label);

	/**
	 * Sets the resolver.
	 * 
	 * @param resolver
	 *            The resolver to set
	 */
	public abstract void setResolver(ObjectResolver resolver);

	/**
	 * Start a new object and write its header. An ObjectInfo object is
	 * returned, on which the <code>markEnd</code> mehod must be called after
	 * all data has been written into the object.
	 * 
	 * @param cls
	 * @see ObjectInfo
	 * @return The info for the started object
	 */
	public abstract ObjectInfo startObject(VmType cls);

	/**
	 * Remove count bytes from the end of the generated stream.
	 * 
	 * @param count
	 */
	public abstract void trim(int count);

    /**
     * Append a series of bytes from the current position. 
     * @param data
     * @param ofs
     * @param len
     */
	public abstract void write(byte[] data, int ofs, int len);

    /**
     * Append a 16-bit int from the current position. 
     * @param v16
     */
	public abstract void write16(int v16);

    /**
     * Append a 32-bit int from the current position. 
     * @param v32
     */
	public abstract void write32(int v32);

    /**
     * Append a 64-bit int from the current position. 
     * @param v64
     */
	public abstract void write64(long v64);

    /**
     * Append a 8-bit int from the current position. 
     * @param v8
     */
	public abstract void write8(int v8);

	/**
	 * Create a ADC dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public abstract void writeADC(GPR dstReg, GPR srcReg);

	/**
	 * Create a ADC dstReg, [srcReg+srcDisp]
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeADC(GPR dstReg, GPR srcReg, int srcDisp);

	/**
	 * Create a ADC dstReg, imm32
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public abstract void writeADC(GPR dstReg, int imm32);

	/**
	 * Create a ADC [dstReg+dstDisp], srcReg
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public abstract void writeADC(GPR dstReg, int dstDisp, GPR srcReg);

	/**
	 * Create a ADC [dstReg+dstDisp], imm32
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public abstract void writeADC(int operandSize, GPR dstReg, int dstDisp, int imm32);

	/**
	 * Create a ADD dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public abstract void writeADD(GPR dstReg, GPR srcReg);

	/**
	 * Create a ADD dstReg, [srcReg+srcDisp]
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeADD(GPR dstReg, GPR srcReg, int srcDisp);

	/**
	 * @param dstReg
	 * @param imm32
	 */
	public abstract void writeADD(GPR dstReg, int imm32);

	/**
	 * Create a ADD [dstReg+dstDisp], <srcReg>
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public abstract void writeADD(GPR dstReg, int dstDisp, GPR srcReg);

	/**
	 * Create a ADD [dstReg+dstDisp], imm32
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public abstract void writeADD(int operandSize, GPR dstReg, int dstDisp, int imm32);

	/**
	 * Create a AND dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public abstract void writeAND(GPR dstReg, GPR srcReg);

	/**
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeAND(GPR dstReg, GPR srcReg, int srcDisp);

	/**
	 * Create a AND reg, imm32
	 * 
	 * @param reg
	 * @param imm32
	 */
	public abstract void writeAND(GPR reg, int imm32);

	/**
	 * Create a AND [dstReg+dstDisp], srcReg
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public abstract void writeAND(GPR dstReg, int dstDisp, GPR srcReg);

	/**
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public abstract void writeAND(int operandSize, GPR dstReg, int dstDisp, int imm32);

	/**
	 * Create a OPERATION dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public final void writeArithOp(int operation, GPR dstReg, GPR srcReg) {
		switch (operation) {
		case X86Operation.ADD:
			writeADD(dstReg, srcReg);
			break;
		case X86Operation.ADC:
			writeADC(dstReg, srcReg);
			break;
		case X86Operation.SUB:
			writeSUB(dstReg, srcReg);
			break;
		case X86Operation.SBB:
			writeSBB(dstReg, srcReg);
			break;
		case X86Operation.AND:
			writeAND(dstReg, srcReg);
			break;
		case X86Operation.OR:
			writeOR(dstReg, srcReg);
			break;
		case X86Operation.XOR:
			writeXOR(dstReg, srcReg);
			break;
		default:
			throw new IllegalArgumentException("Invalid operation " + operation);
		}
	}

	/**
	 * Create a OPERATION dstReg, [srcReg+srcDisp]
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public final void writeArithOp(int operation, GPR dstReg, GPR srcReg,
			int srcDisp) {
		switch (operation) {
		case X86Operation.ADD:
			writeADD(dstReg, srcReg, srcDisp);
			break;
		case X86Operation.ADC:
			writeADC(dstReg, srcReg, srcDisp);
			break;
		case X86Operation.SUB:
			writeSUB(dstReg, srcReg, srcDisp);
			break;
		case X86Operation.SBB:
			writeSBB(dstReg, srcReg, srcDisp);
			break;
		case X86Operation.AND:
			writeAND(dstReg, srcReg, srcDisp);
			break;
		case X86Operation.OR:
			writeOR(dstReg, srcReg, srcDisp);
			break;
		case X86Operation.XOR:
			writeXOR(dstReg, srcReg, srcDisp);
			break;
		default:
			throw new IllegalArgumentException("Invalid operation " + operation);
		}
	}

	/**
	 * @param dstReg
	 * @param imm32
	 */
	public final void writeArithOp(int operation, GPR dstReg, int imm32) {
		switch (operation) {
		case X86Operation.ADD:
			writeADD(dstReg, imm32);
			break;
		case X86Operation.ADC:
			writeADC(dstReg, imm32);
			break;
		case X86Operation.SUB:
			writeSUB(dstReg, imm32);
			break;
		case X86Operation.SBB:
			writeSBB(dstReg, imm32);
			break;
		case X86Operation.AND:
			writeAND(dstReg, imm32);
			break;
		case X86Operation.OR:
			writeOR(dstReg, imm32);
			break;
		case X86Operation.XOR:
			writeXOR(dstReg, imm32);
			break;
		default:
			throw new IllegalArgumentException("Invalid operation " + operation);
		}
	}

	/**
	 * Create a OPERATION [dstReg+dstDisp], <srcReg>
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public final void writeArithOp(int operation, GPR dstReg, int dstDisp,
			GPR srcReg) {
		switch (operation) {
		case X86Operation.ADD:
			writeADD(dstReg, dstDisp, srcReg);
			break;
		case X86Operation.ADC:
			writeADC(dstReg, dstDisp, srcReg);
			break;
		case X86Operation.SUB:
			writeSUB(dstReg, dstDisp, srcReg);
			break;
		case X86Operation.SBB:
			writeSBB(dstReg, dstDisp, srcReg);
			break;
		case X86Operation.AND:
			writeAND(dstReg, dstDisp, srcReg);
			break;
		case X86Operation.OR:
			writeOR(dstReg, dstDisp, srcReg);
			break;
		case X86Operation.XOR:
			writeXOR(dstReg, dstDisp, srcReg);
			break;
		default:
			throw new IllegalArgumentException("Invalid operation " + operation);
		}
	}

	/**
	 * Create a OPERATION [dstReg+dstDisp], imm32
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public final void writeArithOp(int operation, int operandSize, GPR dstReg, int dstDisp,
			int imm32) {
		testOperandSize(operandSize, BITS32 | BITS64);
		switch (operation) {
		case X86Operation.ADD:
			writeADD(operandSize, dstReg, dstDisp, imm32);
			break;
		case X86Operation.ADC:
			writeADC(operandSize, dstReg, dstDisp, imm32);
			break;
		case X86Operation.SUB:
			writeSUB(operandSize, dstReg, dstDisp, imm32);
			break;
		case X86Operation.SBB:
			writeSBB(operandSize, dstReg, dstDisp, imm32);
			break;
		case X86Operation.AND:
			writeAND(operandSize, dstReg, dstDisp, imm32);
			break;
		case X86Operation.OR:
			writeOR(operandSize, dstReg, dstDisp, imm32);
			break;
		case X86Operation.XOR:
			writeXOR(operandSize, dstReg, dstDisp, imm32);
			break;
		default:
			throw new IllegalArgumentException("Invalid operation " + operation);
		}
	}

	/**
	 * Write an sseSD dst, [src+srcDisp] operation for 64-bit fp operations.
	 * 
	 * @param dst
	 *            Must be an xmm register
	 * @param src
	 *            Must be an gpr register
	 * @param srcDisp
	 */
	public abstract void writeArithSSEDOp(int operation, X86Register.XMM dst,
			X86Register.GPR src, int srcDisp);

	/**
	 * Write an sseSD dst, src operation for 64-bit fp operations.
	 * 
	 * @param dst
	 *            Must be an xmm register
	 * @param src
	 *            Must be an xmm register
	 */
	public abstract void writeArithSSEDOp(int operation, X86Register.XMM dst,
			X86Register.XMM src);

	/**
	 * Write an sseSS dst, [src+srcDisp] operation for 32-bit fp operations.
	 * 
	 * @param dst
	 *            Must be an xmm register
	 * @param src
	 *            Must be an gpr register
	 * @param srcDisp
	 */
	public abstract void writeArithSSESOp(int operation, X86Register.XMM dst,
			X86Register.GPR src, int srcDisp);

	/**
	 * Write an sseSD dst, src operation for 32-bit fp operations.
	 * 
	 * @param dst
	 *            Must be an xmm register
	 * @param src
	 *            Must be an xmm register
	 */
	public abstract void writeArithSSESOp(int operation, X86Register.XMM dst,
			X86Register.XMM src);

	/**
	 * Create a bound lReg, [rReg+rDisp]
     * Only valid in 32-bit mode.
	 * 
	 * @param lReg
	 * @param rReg
	 * @param rDisp
     * @throws InvalidOpcodeException in 64-bit mode.
	 */
	public abstract void writeBOUND(GPR lReg, GPR rReg, int rDisp)
    throws InvalidOpcodeException;

	/**
	 * Create a int3
	 */
	public abstract void writeBreakPoint();

	/**
	 * Create a call to address stored in the given register.
	 * 
	 * @param reg
	 */
	public abstract void writeCALL(GPR reg);

	/**
	 * Create a call to address stored at the given
	 * [regBase+regIndex*scale+disp].
	 * 
	 * @param regBase
	 * @param regIndex
	 * @param scale
	 * @param disp
	 */
	public abstract void writeCALL(GPR regBase, GPR regIndex, int scale,
			int disp);

	/**
	 * Create a call to address stored at the given [reg+offset].
	 * 
	 * @param reg
	 * @param offset
	 */
	public abstract void writeCALL(GPR reg, int offset);

	/**
	 * Create a relative call to a given label
	 * 
	 * @param label
	 */
	public abstract void writeCALL(Label label);

	/**
	 * Create a call to address stored at the given offset in the given table
	 * pointer.
	 * 
	 * @param tablePtr
	 * @param offset
	 * @param rawAddress
	 *            If true, tablePtr is a raw address
	 */
	public abstract void writeCALL(Object tablePtr, int offset,
			boolean rawAddress);

	/**
	 * Create a cdq
	 */
	public abstract void writeCDQ();

	/**
	 * Create a CMOVcc dst,src
	 * 
	 * @param ccOpcode
	 * @param dst
	 * @param src
     * @throws InvalidOpcodeException If no CMOV feature.
	 */
	public abstract void writeCMOVcc(int ccOpcode, GPR dst, GPR src)
    throws InvalidOpcodeException;

	/**
	 * Create a CMOVcc dst,[src+srcDisp]
	 * 
	 * @param dst
	 * @param src
	 * @param srcDisp
     * @throws InvalidOpcodeException If no CMOV feature.
	 */
	public abstract void writeCMOVcc(int ccOpcode, GPR dst, GPR src, int srcDisp)
    throws InvalidOpcodeException;

	/**
	 * Create a CMP reg1, reg2
	 * 
	 * @param reg1
	 * @param reg2
	 */
	public abstract void writeCMP(GPR reg1, GPR reg2);

	/**
	 * Create a CMP reg1, [reg2+disp]
	 * 
	 * @param reg1
	 * @param reg2
	 * @param disp
	 */
	public abstract void writeCMP(GPR reg1, GPR reg2, int disp);

	/**
	 * Create a CMP [reg1+disp], reg2
	 * 
	 * @param reg1
	 * @param disp
	 * @param reg2
	 */
	public abstract void writeCMP(GPR reg1, int disp, GPR reg2);

	/**
	 * Create a CMP reg, imm32
	 * 
	 * @param reg
	 * @param imm32
	 */
	public abstract void writeCMP_Const(GPR reg, int imm32);

	/**
	 * Create a CMP [reg+disp], imm32
	 * 
	 * @param reg
	 * @param disp
	 * @param imm32
	 */
	public abstract void writeCMP_Const(int operandSize, GPR reg, int disp, int imm32);

	/**
	 * Create a CMP EAX, imm32 or CMP rax, imm32
	 * 
	 * @param operandSize BITS32 or BITS64
	 * @param imm32
	 */
	public abstract void writeCMP_EAX(int operandSize, int imm32);

	/**
	 * Create a CMP reg,[memPtr]
	 * 
	 * @param reg
	 * @param memPtr
	 */
	public abstract void writeCMP_MEM(GPR reg, int memPtr);

	/**
	 * Create a CMP size [memPtr], imm32
	 * 
	 * @param operandSize BITS32 or BITS64
	 * @param memPtr
	 * @param imm32
	 */
	public abstract void writeCMP_MEM(int operandSize, int memPtr, int imm32);

	/**
	 * Create a CMPXCHG dword [dstReg], srcReg
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 * @param lock
	 */
	public abstract void writeCMPXCHG_EAX(GPR dstReg, int dstDisp, GPR srcReg,
			boolean lock);

	/**
	 * Create a dec reg32
	 * 
	 * @param dstReg
	 */
	public abstract void writeDEC(GPR dstReg);

	/**
	 * Create a dec size [dstReg+dstDisp]
	 * 
	 * @param operandSize BITS32 or BITS64
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeDEC(int operandSize, GPR dstReg, int dstDisp);

	/**
	 * Create a fadd dword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeFADD32(GPR srcReg, int srcDisp);

	/**
	 * Create a fadd qword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeFADD64(GPR srcReg, int srcDisp);

	/**
	 * Create a faddp fpuReg fpuReg + ST0 to fpuReg and pop ST0
	 * 
	 * @param fpuReg
	 */
	public abstract void writeFADDP(X86Register fpuReg);

	/**
	 * Create a fchs
	 */
	public abstract void writeFCHS();

	/**
	 * Create a fdiv dword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeFDIV32(GPR srcReg, int srcDisp);

	/**
	 * Create a fdiv qword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeFDIV64(GPR srcReg, int srcDisp);

	/**
	 * Create a fdivp fpuReg fpuReg / ST0 to fpuReg ; pop ST0
	 * 
	 * @param fpuReg
	 */
	public abstract void writeFDIVP(X86Register fpuReg);

	/**
	 * Create a ffree
	 * 
	 * @param fReg
	 */
	public abstract void writeFFREE(X86Register fReg);

	/**
	 * Create a fild dword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeFILD32(GPR dstReg, int dstDisp);

	/**
	 * Create a fild qword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeFILD64(GPR dstReg, int dstDisp);

	/**
	 * Create a fistp dword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeFISTP32(GPR dstReg, int dstDisp);

	/**
	 * Create a fistp qword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeFISTP64(GPR dstReg, int dstDisp);

	/**
	 * Create a fld dword [srcBaseReg+scrIndexReg*srcScale+srcDisp]
	 * 
	 * @param srcBaseReg
	 * @param srcIndexReg
	 * @param srcScale
	 * @param srcDisp
	 */
	public abstract void writeFLD32(GPR srcBaseReg, GPR srcIndexReg,
			int srcScale, int srcDisp);

	/**
	 * Create a fld dword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeFLD32(GPR srcReg, int srcDisp);

	/**
	 * Create a fld qword [srcBaseReg+scrIndexReg*srcScale+srcDisp]
	 * 
	 * @param srcBaseReg
	 * @param srcIndexReg
	 * @param srcScale
	 * @param srcDisp
	 */
	public abstract void writeFLD64(GPR srcBaseReg, GPR srcIndexReg,
			int srcScale, int srcDisp);

	/**
	 * Create a fld qword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeFLD64(GPR srcReg, int srcDisp);

	/**
	 * Create a fmul dword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeFMUL32(GPR srcReg, int srcDisp);

	/**
	 * Create a fmul qword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeFMUL64(GPR srcReg, int srcDisp);

	/**
	 * Create a fmulp fpuReg fpuReg * ST0 to fpuReg ; pop ST0
	 * 
	 * @param fpuReg
	 */
	public abstract void writeFMULP(X86Register fpuReg);

	/**
	 * Create a fnstsw, Store fp status word in AX
	 */
	public abstract void writeFNSTSW_AX();

	/**
	 * Create a fprem
	 */
	public abstract void writeFPREM();

	/**
	 * Create a fstp fpuReg
	 * 
	 * @param fpuReg
	 */
	public abstract void writeFSTP(X86Register fpuReg);

	/**
	 * Create a fstp dword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeFSTP32(GPR dstReg, int dstDisp);

	/**
	 * Create a fstp qword [dstReg+dstDisp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeFSTP64(GPR dstReg, int dstDisp);

	/**
	 * Create a fsub dword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeFSUB32(GPR srcReg, int srcDisp);

	/**
	 * Create a fsub qword [srcReg+srcDisp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeFSUB64(GPR srcReg, int srcDisp);

	/**
	 * Create a fsubp fpuReg fpuReg - ST0 to fpuReg & pop ST0
	 * 
	 * @param fpuReg
	 */
	public abstract void writeFSUBP(X86Register fpuReg);

	/**
	 * Create a fucompp, Compare - Pop twice
	 */
	public abstract void writeFUCOMPP();

	/**
	 * Create a fxch fpuReg Swap ST0 and fpuReg
	 */
	public abstract void writeFXCH(X86Register fpuReg);

	/**
	 * Create a idiv eax, srcReg
	 * 
	 * @param srcReg
	 */
	public abstract void writeIDIV_EAX(GPR srcReg);

	/**
	 * Create a idiv eax, [src+srcDisp] or idiv rax, [src+srcDisp]
	 * @param operandSize BITS32 or BITS64
	 * @param src
	 * @param srcDisp
	 */
	public abstract void writeIDIV_EAX(int operandSize, GPR src, int srcDisp);

	/**
	 * @param dstReg
	 * @param srcReg
	 */
	public abstract void writeIMUL(GPR dstReg, GPR srcReg);

	/**
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeIMUL(GPR dstReg, GPR srcReg, int srcDisp);

	/**
	 * @param dstReg
	 * @param srcReg
	 * @param imm32
	 */
	public abstract void writeIMUL_3(GPR dstReg, GPR srcReg, int imm32);

	/**
	 * Create a three operand imul.
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 * @param imm32
	 */
	public abstract void writeIMUL_3(GPR dstReg, GPR srcReg, int srcDisp,
			int imm32);

	/**
	 * Create a imul eax, srcReg
	 * 
	 * @param srcReg
	 */
	public abstract void writeIMUL_EAX(GPR srcReg);

	/**
	 * Create a inc dstReg
	 * 
	 * @param dstReg
	 */
	public abstract void writeINC(GPR dstReg);

	/**
	 * Create a inc size [dstReg+disp]
	 * 
	 * @param operandSize BITS32 or BITS64
	 * @param dstReg
	 * @param disp
	 */
	public abstract void writeINC(int operandSize, GPR dstReg, int disp);

	/**
	 * Create a int vector
	 * 
	 * @param vector
	 */
	public abstract void writeINT(int vector);

	/**
	 * Create a conditional jump to a label The opcode sequence is: 0x0f
	 * <jumpOpcode><rel32>
	 * 
	 * @param label
	 * @param jumpOpcode
	 */
	public abstract void writeJCC(Label label, int jumpOpcode);

	/**
	 * Create a absolute jump to address in register
	 * 
	 * @param reg32
	 */
	public abstract void writeJMP(GPR reg32);

	/**
	 * Create a absolute jump to [reg32+disp]
	 * 
	 * @param reg32
	 */
	public abstract void writeJMP(GPR reg32, int disp);

	/**
	 * Create a relative jump to a given label
	 * 
	 * @param label
	 */
	public abstract void writeJMP(Label label);

	/**
	 * Create a absolute jump to address stored at the given offset (in
	 * register) in the given table pointer.
	 * 
	 * @param tablePtr
	 * @param offsetReg
	 */
	public abstract void writeJMP(Object tablePtr, GPR offsetReg);

	/**
	 * Create a absolute jump to address stored at the given offset in the given
	 * table pointer.
	 * 
	 * @param tablePtr
	 * @param offset
	 * @param rawAddress
	 *            If true, tablePtr is a raw address
	 */
	public abstract void writeJMP(Object tablePtr, int offset,
			boolean rawAddress);

	/**
	 * Create a lea dstReg,[srcReg+srcIdxReg*scale+disp]
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcIdxReg
	 * @param scale
	 * @param disp
	 */
	public abstract void writeLEA(GPR dstReg, GPR srcReg, GPR srcIdxReg,
			int scale, int disp);

	/**
	 * Create a lea dstReg,[srcReg+disp]
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param disp
	 */
	public abstract void writeLEA(GPR dstReg, GPR srcReg, int disp);

	/**
	 * Create a LODSD
	 */
	public abstract void writeLODSD();

	/**
	 * Create a LOOP label instruction. The given label must have be resolved
	 * before!
	 * 
	 * @param label
	 * @throws UnresolvedObjectRefException
	 */
	public abstract void writeLOOP(Label label)
			throws UnresolvedObjectRefException;

	/**
	 * Create a mov <dstReg>, <srcReg>
	 * 
	 * @param operandSize
	 * @param dstReg
	 * @param srcReg
	 */
	public abstract void writeMOV(int operandSize, GPR dstReg, GPR srcReg);

	/**
	 * Create a mov dstReg, [srcReg+srcIdxReg*scale+srcDisp]
	 * 
	 * @param operandSize
	 * @param dstReg
	 * @param srcReg
	 * @param srcIdxReg
	 * @param scale
	 * @param srcDisp
	 */
	public abstract void writeMOV(int operandSize, GPR dstReg, GPR srcReg,
			GPR srcIdxReg, int scale, int srcDisp);

	/**
	 * Create a mov dstReg, [srcReg+srcDisp]
	 * 
	 * @param operandSize
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeMOV(int operandSize, GPR dstReg, GPR srcReg,
			int srcDisp);

	/**
	 * Create a mov [dstReg+dstIdxReg*scale+dstDisp], <srcReg>
	 * 
	 * @param operandSize
	 * @param dstReg
	 * @param dstIdxReg
	 * @param scale
	 * @param dstDisp
	 * @param srcReg
	 */
	public abstract void writeMOV(int operandSize, GPR dstReg, GPR dstIdxReg,
			int scale, int dstDisp, GPR srcReg);

	/**
	 * Create a mov [dstReg+dstDisp], <srcReg>
	 * 
	 * @param operandSize
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public abstract void writeMOV(int operandSize, GPR dstReg, int dstDisp,
			GPR srcReg);

	/**
	 * Create a mov <reg>, <imm32>
	 * 
	 * @param destReg
	 * @param imm32
	 */
	public abstract void writeMOV_Const(GPR destReg, int imm32);

	/**
	 * Create a mov size [destReg+destDisp], imm32
	 * 
	 * @param destReg
	 * @param destDisp
	 * @param imm32
	 */
	public abstract void writeMOV_Const(int operandSize, GPR destReg, int destDisp, int imm32);

	/**
	 * Create a mov <reg>, <label>
	 * 
	 * @param dstReg
	 * @param label
	 */
	public abstract void writeMOV_Const(GPR dstReg, Object label);

	/**
<<<<<<< X86Assembler.java
=======
	 * Create a mov size [destReg+dstIdxReg*scale+destDisp], imm32
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public abstract void writeMOV_Const(int operandSize, GPR dstReg, GPR dstIdxReg, int scale,
			int dstDisp, int imm32);

	/**
>>>>>>> 1.10
	 * Create a movsd [dst+dstDisp],src
	 * 
	 * @param dst
	 * @param src
	 */
	public abstract void writeMOVSD(X86Register.GPR dst, int dstDisp,
			X86Register.XMM src);

	/**
	 * Create a movsd dst,[src+srcDisp]
	 * 
	 * @param dst
	 * @param src
	 */
	public abstract void writeMOVSD(X86Register.XMM dst, X86Register.GPR src,
			int srcDisp);

	/**
	 * Create a movsd dst,src
	 * 
	 * @param dst
	 * @param src
	 */
	public abstract void writeMOVSD(X86Register.XMM dst, X86Register.XMM src);

	/**
	 * Create a movss [dst+dstDisp],src
	 * 
	 * @param dst
	 * @param src
	 */
	public abstract void writeMOVSS(X86Register.GPR dst, int dstDisp,
			X86Register.XMM src);

	/**
	 * Create a movss dst,[src+srcDisp]
	 * 
	 * @param dst
	 * @param src
	 */
	public abstract void writeMOVSS(X86Register.XMM dst, X86Register.GPR src,
			int srcDisp);

	/**
	 * Create a movss dst,src
	 * 
	 * @param dst
	 * @param src
	 */
	public abstract void writeMOVSS(X86Register.XMM dst, X86Register.XMM src);

	/**
	 * Create a movsx <dstReg>, <srcReg>
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcSize
	 */
	public abstract void writeMOVSX(GPR dstReg, GPR srcReg, int srcSize);

	public abstract void writeMOVSX(GPR dstReg, GPR srcReg, int srcDisp,
			int size);

	/**
	 * Create a movzx <dstReg>, <srcReg>
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcSize
	 */
	public abstract void writeMOVZX(GPR dstReg, GPR srcReg, int srcSize);

	public abstract void writeMOVZX(GPR dstReg, GPR srcReg, int srcDisp,
			int size);

	/**
	 * Create a mul eax, srcReg
	 * 
	 * @param srcReg
	 */
	public abstract void writeMUL_EAX(GPR srcReg);

	/**
	 * Create a neg dstReg
	 * 
	 * @param dstReg
	 */
	public abstract void writeNEG(GPR dstReg);

	/**
	 * Create a neg size [dstReg+dstDisp]
	 * 
	 * @param operandSize BITS32 or BITS64
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeNEG(int operandSize, GPR dstReg, int dstDisp);

	/**
	 * Create a nop
	 */
	public abstract void writeNOP();

	/**
	 * Create a not dstReg
	 * 
	 * @param dstReg
	 */
	public abstract void writeNOT(GPR dstReg);

	/**
	 * Create a not size [dstReg+dstDisp]
	 * 
	 * @param operandSize BITS32 or BITS64
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeNOT(int operandSize, GPR dstReg, int dstDisp);

	/**
	 * Create a OR dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public abstract void writeOR(GPR dstReg, GPR srcReg);

	// LS
	/**
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeOR(GPR dstReg, GPR srcReg, int srcDisp);

	// LS
	/**
	 * @param dstReg
	 * @param imm32
	 */
	public abstract void writeOR(GPR dstReg, int imm32);

	/**
	 * Create a OR [dstReg+dstDisp], srcReg
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public abstract void writeOR(GPR dstReg, int dstDisp, GPR srcReg);

	/**
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public abstract void writeOR(int operandSize, GPR dstReg, int dstDisp, int imm32);

	/**
	 * Create a pop reg32
	 * 
	 * @param dstReg
	 */
	public abstract void writePOP(GPR dstReg);

	/**
	 * Create a pop dword [reg32+disp]
	 * 
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writePOP(GPR dstReg, int dstDisp);

	/**
	 * Create a popa
     * @throws InvalidOpcodeException In 64-bit mode.
	 */
	public abstract void writePOPA()
    throws InvalidOpcodeException;

	/**
	 * Write an prefix byte
	 * 
	 * @param prefix
	 */
	public abstract void writePrefix(int prefix);

	/**
	 * Create a push reg32
	 * 
	 * @param srcReg
	 * @return The ofset of the start of the instruction.
	 */
	public abstract int writePUSH(GPR srcReg);

	/**
	 * Create a push dword [baseReg+indexReg*scale+disp]
	 * 
	 * @param srcBaseReg
	 * @param srcIndexReg
	 * @param srcScale
	 * @param srcDisp
	 * @return The ofset of the start of the instruction.
	 */
	public abstract int writePUSH(GPR srcBaseReg, GPR srcIndexReg,
			int srcScale, int srcDisp);

	/**
	 * Create a push dword [reg32+disp]
	 * 
	 * @param srcReg
	 * @param srcDisp
	 * @return The ofset of the start of the instruction.
	 */
	public abstract int writePUSH(GPR srcReg, int srcDisp);

	/**
	 * Create a push dword <imm32>
	 * 
	 * @param imm32
	 * @return The ofset of the start of the instruction.
	 */
	public abstract int writePUSH(int imm32);

	// PR
	/**
	 * Create a push dword <object>
	 * 
	 * @param objRef
	 * @return The offset of the start of the instruction.
	 */
	public abstract int writePUSH_Const(Object objRef);

	/**
	 * Create a pusha
     * @throws InvalidOpcodeException In 64-bit mode.
	 */
	public abstract void writePUSHA()
    throws InvalidOpcodeException;

	/**
	 * Create a RDTSC (get timestamp into edx:eax
	 */
	public abstract void writeRDTSC();

	/**
	 * Create 32-bit offset relative to the current (after this offset) offset.
	 * 
	 * @param label
	 */
	public abstract void writeRelativeObjectRef(Label label);

	/**
	 * Create a ret near to caller
	 */
	public abstract void writeRET();

	/**
	 * Create a ret imm16 near to caller
	 * 
	 * @param imm16
	 */
	public abstract void writeRET(int imm16);

	/**
	 * Create a sahf
	 */
	public abstract void writeSAHF();

	/**
	 * Create a SAL dstReg,imm8
	 * 
	 * @param dstReg
	 * @param imm8
	 */
	public abstract void writeSAL(GPR dstReg, int imm8);

	/**
	 * Create a SAL size [dstReg+dstDisp], imm8
	 * @param operandSize BITS32 or BITS64
	 * @param dstReg
	 * @param dstDisp
	 * @param imm8
	 */
	public abstract void writeSAL(int operandSize, GPR dstReg, int dstDisp, int imm8);

	/**
	 * Create a SAL dstReg,cl
	 * 
	 * @param dstReg
	 */
	public abstract void writeSAL_CL(GPR dstReg);

	/**
	 * Create a SAL size [dstReg+dstDisp], CL
	 * @param operandSize BITS32 or BITS64
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeSAL_CL(int operandSize, GPR dstReg, int dstDisp);

	/**
	 * Create a SAR dstReg,imm8
	 * 
	 * @param dstReg
	 * @param imm8
	 */
	public abstract void writeSAR(GPR dstReg, int imm8);

	/**
	 * Create a SAR size [dstReg+dstDisp], imm8
	 * @param operandSize BITS32 or BITS64
	 * @param dstReg
	 * @param dstDisp
	 * @param imm8
	 */
	public abstract void writeSAR(int operandSize, GPR dstReg, int dstDisp, int imm8);

	/**
	 * Create a SAR dstReg,cl
	 * 
	 * @param dstReg
	 */
	public abstract void writeSAR_CL(GPR dstReg);

	/**
	 * Create a SAL size [dstReg+dstDisp], CL
	 * @param operandSize BITS32 or BITS64
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeSAR_CL(int operandSize, GPR dstReg, int dstDisp);

	/**
	 * Create a SBB dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public abstract void writeSBB(GPR dstReg, GPR srcReg);

	/**
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeSBB(GPR dstReg, GPR srcReg, int srcDisp);

	/**
	 * Create a SBB dstReg, imm32
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public abstract void writeSBB(GPR dstReg, int imm32);

	/**
	 * Create a SBB [dstReg+dstDisp], <srcReg>
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public abstract void writeSBB(GPR dstReg, int dstDisp, GPR srcReg);

	/**
	 * Create a SBB dword [dstReg+dstDisp], <imm32>
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public abstract void writeSBB(int operandSize, GPR dstReg, int dstDisp, int imm32);

	/**
	 * Create a SETcc dstReg. Sets the given 8-bit operand to zero if its
	 * condition is not satisfied, and to 1 if it is.
	 * 
	 * @param dstReg
	 * @param cc
	 */
	public abstract void writeSETCC(GPR dstReg, int cc);

	/**
	 * Write a shift operation. OPERATION dst,imm8
	 * 
	 * @param operation
	 * @param dst
	 * @param imm8
	 */
	public final void writeShift(int operation, GPR dst, int imm8) {
		switch (operation) {
		case X86Operation.SAL:
			writeSAL(dst, imm8);
			break;
		case X86Operation.SAR:
			writeSAR(dst, imm8);
			break;
		case X86Operation.SHL:
			writeSHL(dst, imm8);
			break;
		case X86Operation.SHR:
			writeSHR(dst, imm8);
			break;
		default:
			throw new IllegalArgumentException("Invalid operation " + operation);
		}
	}

	/**
	 * Write a shift operation. OPERATION [dst+dstDisp],imm8
	 * 
	 * @param operation
	 * @param dst
	 * @param imm8
	 */
	public final void writeShift(int operation, int operandSize, GPR dst, int dstDisp, int imm8) {
		testOperandSize(operandSize, BITS32 | BITS64);
		switch (operation) {
		case X86Operation.SAL:
			writeSAL(operandSize, dst, dstDisp, imm8);
			break;
		case X86Operation.SAR:
			writeSAR(operandSize, dst, dstDisp, imm8);
			break;
		case X86Operation.SHL:
			writeSHL(operandSize, dst, dstDisp, imm8);
			break;
		case X86Operation.SHR:
			writeSHR(operandSize, dst, dstDisp, imm8);
			break;
		default:
			throw new IllegalArgumentException("Invalid operation " + operation);
		}
	}

	/**
	 * Write a shift operation. OPERATION dst,CL
	 * 
	 * @param operation
	 * @param dst
	 */
	public final void writeShift_CL(int operation, GPR dst) {
		switch (operation) {
		case X86Operation.SAL:
			writeSAL_CL(dst);
			break;
		case X86Operation.SAR:
			writeSAR_CL(dst);
			break;
		case X86Operation.SHL:
			writeSHL_CL(dst);
			break;
		case X86Operation.SHR:
			writeSHR_CL(dst);
			break;
		default:
			throw new IllegalArgumentException("Invalid operation " + operation);
		}
	}

	/**
	 * Write a shift operation. OPERATION [dst+dstDisp],CL
	 * 
	 * @param operation
	 * @param dst
	 * @param dstDisp
	 */
	public final void writeShift_CL(int operation, int operandSize, GPR dst, int dstDisp) {
		testOperandSize(operandSize, BITS32 | BITS64);
		switch (operation) {
		case X86Operation.SAL:
			writeSAL_CL(operandSize, dst, dstDisp);
			break;
		case X86Operation.SAR:
			writeSAR_CL(operandSize, dst, dstDisp);
			break;
		case X86Operation.SHL:
			writeSHL_CL(operandSize, dst, dstDisp);
			break;
		case X86Operation.SHR:
			writeSHR_CL(operandSize, dst, dstDisp);
			break;
		default:
			throw new IllegalArgumentException("Invalid operation " + operation);
		}
	}

	/**
	 * Create a SHL dstReg,imm8
	 * 
	 * @param dstReg
	 * @param imm8
	 */
	public abstract void writeSHL(GPR dstReg, int imm8);

	/**
	 * Create a SHL size [dstReg+dstDisp],imm8
	 * 
	 * @param operandSize BITS32 or BITS64
	 * @param dstReg
	 * @param dstDisp
	 * @param imm8
	 */
	public abstract void writeSHL(int operandSize, GPR dstReg, int dstDisp, int imm8);

	/**
	 * Create a SHL dstReg,cl
	 * 
	 * @param dstReg
	 */
	public abstract void writeSHL_CL(GPR dstReg);

	/**
	 * Create a SHL size [dstReg+dstDisp],cl
	 * 
	 * @param operandSize BITS32 or BITS64
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeSHL_CL(int operandSize, GPR dstReg, int dstDisp);

	/**
	 * Create a SHLD dstReg,srcReg,cl
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public abstract void writeSHLD_CL(GPR dstReg, GPR srcReg);

	/**
	 * Create a SHL dstReg,imm8
	 * 
	 * @param dstReg
	 * @param imm8
	 */
	public abstract void writeSHR(GPR dstReg, int imm8);

	/**
	 * Create a SHR size [dstReg+dstDisp], imm8
	 * @param operandSize BITS32 or BITS64
	 * @param dstReg
	 * @param dstDisp
	 * @param imm8
	 */
	public abstract void writeSHR(int operandSize, GPR dstReg, int dstDisp, int imm8);

	/**
	 * Create a SHR dstReg,cl
	 * 
	 * @param dstReg
	 */
	public abstract void writeSHR_CL(GPR dstReg);

	/**
	 * Create a SHR size [dstReg+dstDisp], CL
	 * @param operandSize BITS32 or BITS64
	 * @param dstReg
	 * @param dstDisp
	 */
	public abstract void writeSHR_CL(int operandSize, GPR dstReg, int dstDisp);

	/**
	 * Create a SHRD dstReg,srcReg,cl
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public abstract void writeSHRD_CL(GPR dstReg, GPR srcReg);

	/**
	 * Create a SUB dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public abstract void writeSUB(GPR dstReg, GPR srcReg);

	// LS
	/**
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeSUB(GPR dstReg, GPR srcReg, int srcDisp);

	/**
	 * Create a SUB reg, imm32
	 * 
	 * @param reg
	 * @param imm32
	 */
	public abstract void writeSUB(GPR reg, int imm32);

	/**
	 * Create a SUB [dstReg+dstDisp], <srcReg>
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public abstract void writeSUB(GPR dstReg, int dstDisp, GPR srcReg);

	// LS
	/**
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public abstract void writeSUB(int operandSize, GPR dstReg, int dstDisp, int imm32);

	/**
	 * Create a TEST reg1, reg2
	 * 
	 * @param reg1
	 * @param reg2
	 */
	public abstract void writeTEST(GPR reg1, GPR reg2);

	/**
	 * Create a TEST reg, imm32
	 * 
	 * @param reg
	 * @param imm32
	 */
	public abstract void writeTEST(GPR reg, int imm32);

	/**
	 * Create a TEST size [reg+disp], imm32
	 * 
	 * @param operandSize BITS32 or BITS64
	 * @param reg
	 * @param disp
	 * @param imm32
	 */
	public abstract void writeTEST(int operandSize, GPR reg, int disp, int imm32);

	/**
	 * Create a TEST al, imm8
	 * 
	 * @param value
	 */
	public abstract void writeTEST_AL(int value);

	/**
	 * Create a TEST eax, imm32, TEST rax, imm32
	 * 
	 * @param operandSize BITS32 or BITS64
	 * @param value
	 */
	public abstract void writeTEST_EAX(int operandSize, int value);

	/**
	 * Write my contents to the given stream.
	 * 
	 * @param os
	 * @throws IOException
	 */
	public abstract void writeTo(OutputStream os) throws IOException;

	/**
	 * Write XCHG dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public abstract void writeXCHG(GPR dstReg, GPR srcReg);

	/**
	 * Write XCHG [dstReg+dstDisp], srcReg
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public abstract void writeXCHG(GPR dstReg, int dstDisp, GPR srcReg);

	/**
	 * Create a XOR dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public abstract void writeXOR(GPR dstReg, GPR srcReg);

	// LS
	/**
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public abstract void writeXOR(GPR dstReg, GPR srcReg, int srcDisp);

	// LS
	/**
	 * @param dstReg
	 * @param imm32
	 */
	public abstract void writeXOR(GPR dstReg, int imm32);

	/**
	 * Create a XOR [dstReg+dstDisp], srcReg
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param srcReg
	 */
	public abstract void writeXOR(GPR dstReg, int dstDisp, GPR srcReg);

	/**
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public abstract void writeXOR(int operandSize, GPR dstReg, int dstDisp, int imm32);

	/**
	 * Test for a valid operand size.
	 * 
	 * @param operandSize The given operand size.
	 * @param allowedMask The allowed operand sizes.
	 */
	protected final void testOperandSize(int operandSize, int allowedMask) {
		if ((operandSize & allowedMask) != operandSize) {
			throw new IllegalArgumentException("Invalid operand size " + operandSize);
		}
	}
}
