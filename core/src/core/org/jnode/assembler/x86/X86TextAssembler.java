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
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.XMM;
import org.jnode.util.NumberUtils;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.X86CpuID;

/**
 * Debug version of AbstractX86Stream.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
public class X86TextAssembler extends X86Assembler implements X86Operation {
	class ObjectInfoImpl extends NativeStream.ObjectInfo {

		/**
		 * @see org.jnode.assembler.NativeStream.ObjectInfo#markEnd()
		 */
		public void markEnd() {
			println(";\n; -- End of Object --\n;");
		}

	}

	class ObjectRefImpl extends NativeStream.ObjectRef {

		public ObjectRefImpl(Object object) {
			super(object);
		}

		/**
		 * @see org.jnode.assembler.NativeStream.ObjectRef#getOffset()
		 */
		public int getOffset() throws UnresolvedObjectRefException {
			return 0;
		}

		/**
		 * @see org.jnode.assembler.NativeStream.ObjectRef#isResolved()
		 */
		public boolean isResolved() {
			return true;
		}

		public void addUnresolvedLink(int offset, int patchSize) {
			// TODO Auto-generated method stub

		}

		/**
		 * @see org.jnode.assembler.NativeStream.ObjectRef#link(org.jnode.assembler.NativeStream.ObjectRef)
		 */
		public void link(ObjectRef objectRef)
				throws UnresolvedObjectRefException {
		}

	}

	private static final String ccName(int jumpOpcode) {
		final String opc;
		switch (jumpOpcode) {
		case JA:
			opc = "a";
			break;
		case JAE:
			opc = "ae";
			break;
		case JB:
			opc = "b";
			break;
		case JBE:
			opc = "be";
			break;
		case JE:
			opc = "e";
			break;
		case JNE:
			opc = "ne";
			break;
		case JLE:
			opc = "le";
			break;
		case JL:
			opc = "l";
			break;
		case JGE:
			opc = "ge";
			break;
		case JG:
			opc = "g";
			break;
		default:
			throw new RuntimeException("Unknown jump opcode " + jumpOpcode);
		}
		return opc;
	}

	private static String getSSEOperationName(int operation) {
		switch (operation) {
		case SSE_ADD:
			return "ADDS";
		case SSE_SUB:
			return "SUBS";
		case SSE_MUL:
			return "MULS";
		case SSE_DIV:
			return "DIVS";
		default:
			throw new IllegalArgumentException("Unknown SSE operation "
					+ operation);
		}
	}

	private final StringBuffer buf = new StringBuffer();

	private final byte[] dummy = new byte[0];

	private int idx = 0;

	final PrintWriter out;

	private final String stripPrefix;

	/**
	 * Initialize this instance
	 * 
	 * @param out
	 */
	public X86TextAssembler(Writer out, X86CpuID cpuId, Mode mode) {
		this(out, cpuId, mode, null);
	}

	/**
	 * Initialize this instance
	 * 
	 * @param out
	 */
	public X86TextAssembler(Writer out, X86CpuID cpuId, Mode mode,
			String stripPrefix) {
		super(cpuId, mode);
		this.out = new PrintWriter(out);
		this.stripPrefix = stripPrefix;
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#align(int)
	 */
	public int align(int value) {
		return 0;
	}

	/**
	 * Remove all data and references.
	 */
	public void clear() {
		buf.setLength(0);
		idx = 0;
	}

	private String disp(int v) {
		if (v > 0) {
			return "+" + v;
		} else if (v < 0) {
			return "" + v;
		} else {
			return "";
		}
	}

	/**
	 * Flush the contents of the used stream.
	 * 
	 */
	public void flush() throws IOException {
		out.print(buf.toString());
		idx += buf.length();
		buf.setLength(0);
		out.flush();
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#get32(int)
	 */
	public int get32(int offset) {
		return 0;
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#get8(int)
	 */
	public int get8(int offset) {
		return 0;
	}

	/**
	 * @see org.jnode.assembler.NativeStream#getBaseAddr()
	 */
	public long getBaseAddr() {
		return 0;
	}

	/**
	 * @see org.jnode.assembler.NativeStream#getBytes()
	 */
	public byte[] getBytes() {
		return dummy;
	}

	/**
	 * @see org.jnode.assembler.NativeStream#getLength()
	 */
	public int getLength() {
		return idx + buf.length();
	}

	/**
	 * @see org.jnode.assembler.NativeStream#getObjectRef(java.lang.Object)
	 */
	public ObjectRef getObjectRef(Object keyObj) {
		return new ObjectRefImpl(keyObj);
	}

	/**
	 * @see org.jnode.assembler.NativeStream#getObjectRefs()
	 */
	public Collection getObjectRefs() {
		return null;
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#getResolver()
	 */
	public ObjectResolver getResolver() {
		return null;
	}

	/**
	 * @see org.jnode.assembler.NativeStream#getUnresolvedObjectRefs()
	 */
	public Collection getUnresolvedObjectRefs() {
		return null;
	}

	/**
	 * @see org.jnode.assembler.NativeStream#hasUnresolvedObjectRefs()
	 */
	public boolean hasUnresolvedObjectRefs() {
		return false;
	}

	/**
	 * Is logging enabled. This method will only return true on on debug like
	 * implementations.
	 * 
	 * @return boolean
	 */
	public boolean isLogEnabled() {
		return true;
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#isTextStream()
	 */
	public boolean isTextStream() {
		return true;
	}

	private String label(Object label) {
		String s = label.toString();
		if (stripPrefix != null) {
			if (s.startsWith(stripPrefix)) {
				s = s.substring(stripPrefix.length());
			}
		}
		return s;
	}

	/**
	 * Write a log message. This method is only implemented on debug like
	 * implementations.
	 * 
	 * @param msg
	 */
	public void log(Object msg) {
		println(";\n; " + msg + "\n;");
	}

	private final String operandSize(int opSize) {
		switch (opSize) {
		case BITS8:
			return "byte";
		case BITS16:
			return "word";
		case BITS32:
			return "dword";
		default:
			throw new IllegalArgumentException("Unknown operand size " + opSize);
		}
	}

	protected final int println(String msg) {
		final int rc = idx + buf.length();
		buf.append(msg);
		buf.append('\n');
		return rc;
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#set32(int, int)
	 */
	public void set32(int offset, int v32) {
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#set8(int, int)
	 */
	public void set8(int offset, int v8) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.jnode.assembler.NativeStream#setObjectRef(java.lang.Object)
	 */
	public ObjectRef setObjectRef(Object label) {
		println(label(label) + ":");
		return new ObjectRefImpl(label);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#setResolver(org.jnode.assembler.ObjectResolver)
	 */
	public void setResolver(ObjectResolver resolver) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.jnode.assembler.NativeStream#startObject(org.jnode.vm.classmgr.VmType)
	 */
	public ObjectInfo startObject(VmType cls) {
		println(";\n; -- Start of object --\n;");
		return new ObjectInfoImpl();
	}

	/**
	 * Remove count bytes from the end of the generated stream.
	 * 
	 * @param count
	 */
	public void trim(int count) {
		buf.setLength(buf.length() - count);
		// println("\t; TRIM " + count + " bytes");
	}

	/**
	 * @see org.jnode.assembler.NativeStream#write(byte[], int, int)
	 */
	public void write(byte[] data, int ofs, int len) {
		buf.append("\tdb ");
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				buf.append(',');
			}
			buf.append(data[i]);
		}
		buf.append('\n');
	}

	/**
	 * @see org.jnode.assembler.NativeStream#write16(int)
	 */
	public void write16(int v16) {
		println("\tdw " + v16);
	}

	/**
	 * @see org.jnode.assembler.NativeStream#write32(int)
	 */
	public void write32(int v32) {
		println("\tdd " + v32);
	}

	/**
	 * @see org.jnode.assembler.NativeStream#write64(long)
	 */
	public void write64(long v64) {
		println("\tdq " + v64);
	}

	/**
	 * @see org.jnode.assembler.NativeStream#write8(int)
	 */
	public void write8(int v8) {
		println("\tdb " + v8);
	}

	/**
	 * Create a ADC dstReg, imm32
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public void writeADC(X86Register dstReg, int imm32) {
		println("\tadc " + dstReg + "," + imm32);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeADC(X86Register, int, int)
	 */
	public void writeADC(X86Register dstReg, int dstDisp, int imm32) {
		println("\tadc [" + dstReg + disp(dstDisp) + "]," + imm32);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeADC(X86Register, int,
	 *      X86Register)
	 */
	public void writeADC(X86Register dstReg, int dstDisp, X86Register srcReg) {
		println("\tadc [" + dstReg + disp(dstDisp) + "]," + srcReg);
	}

	/**
	 * Create a ADC dstReg, srcReg
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public void writeADC(X86Register dstReg, X86Register srcReg) {

		println("\tadc " + dstReg + "," + srcReg);
	}

	/**
	 * Create a ADC dstReg, [srcReg+srcDisp]
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeADC(X86Register dstReg, X86Register srcReg, int srcDisp) {
		println("\tadc " + dstReg + ",[" + srcReg + disp(srcDisp) + "]");
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public void writeADD(X86Register dstReg, int imm32) {
		println("\tadd " + dstReg + ",0x" + NumberUtils.hex(imm32));
	}

	/**
	 * Create a ADD [dstReg+dstDisp], imm32
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeADD(X86Register dstReg, int dstDisp, int imm32) {
		println("\tadd dword[" + dstReg + disp(dstDisp) + "]," + imm32);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeADD(X86Register, int,
	 *      X86Register)
	 */
	public void writeADD(X86Register dstReg, int dstDisp, X86Register srcReg) {

		println("\tadd [" + dstReg + disp(dstDisp) + "]," + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeADD(X86Register,
	 *      X86Register)
	 */
	public void writeADD(X86Register dstReg, X86Register srcReg) {

		println("\tadd " + dstReg + "," + srcReg);
	}

	/**
	 * Create a ADD dstReg, [srcReg+srcDisp]
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeADD(X86Register dstReg, X86Register srcReg, int srcDisp) {
		println("\tadd " + dstReg + ",[" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeAND(X86Register, int)
	 */
	public void writeAND(X86Register reg, int imm32) {

		println("\tand " + reg + ",0x" + NumberUtils.hex(imm32));
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeAND(X86Register dstReg, int dstDisp, int imm32) {
		println("\tand dword[" + dstReg + disp(dstDisp) + "],0x"
				+ NumberUtils.hex(imm32));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeAND(X86Register, int,
	 *      X86Register)
	 */
	public void writeAND(X86Register dstReg, int dstDisp, X86Register srcReg) {

		println("\tand [" + dstReg + disp(dstDisp) + "]," + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeAND(X86Register,
	 *      X86Register)
	 */
	public void writeAND(X86Register dstReg, X86Register srcReg) {

		println("\tand " + dstReg + "," + srcReg);
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeAND(X86Register dstReg, X86Register srcReg, int srcDisp) {
		println("\tand " + dstReg + ",[" + srcReg + disp(srcDisp) + "]");
	}

	public void writeArithSSEDOp(int operation, X86Register.XMM dst,
			X86Register.GPR src, int srcDisp) {
		final String op = getSSEOperationName(operation);
		println("\t" + op + "D" + dst + ", qword [" + src + disp(srcDisp) + "]");
	}

	public void writeArithSSEDOp(int operation, X86Register.XMM dst,
			X86Register.XMM src) {
		final String op = getSSEOperationName(operation);
		println("\t" + op + "D" + dst + ", " + src);
	}

	public void writeArithSSESOp(int operation, X86Register.XMM dst,
			X86Register.GPR src, int srcDisp) {
		final String op = getSSEOperationName(operation);
		println("\t" + op + "S" + dst + ", dword [" + src + disp(srcDisp) + "]");
	}

	public void writeArithSSESOp(int operation, X86Register.XMM dst,
			X86Register.XMM src) {
		final String op = getSSEOperationName(operation);
		println("\t" + op + "S" + dst + ", " + src);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeBOUND(X86Register,
	 *      X86Register, int)
	 */
	public void writeBOUND(X86Register lReg, X86Register rReg, int rDisp) {
		println("\tbound " + lReg + ",[" + rReg + disp(rDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeBreakPoint()
	 */
	public void writeBreakPoint() {
		println("\tint 3");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCALL(Label)
	 */
	public void writeCALL(Label label) {

		println("\tcall " + label(label));
	}

	/**
	 * Create a call to address stored at the given offset in the given table
	 * pointer.
	 * 
	 * @param tablePtr
	 * @param offset
	 * @param rawAddress
	 *            If true, tablePtr is a raw address
	 */
	public void writeCALL(Object tablePtr, int offset, boolean rawAddress) {

		println("\tcall dword [" + tablePtr + disp(offset) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCALL(X86Register)
	 */
	public void writeCALL(X86Register reg) {
		println("\tcall " + reg);
	}

	/**
	 * Create a call to address stored at the given [reg+offset].
	 * 
	 * @param reg
	 * @param offset
	 */
	public void writeCALL(X86Register reg, int offset) {

		println("\tcall dword [" + reg + disp(offset) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCALL(X86Register,
	 *      X86Register, int, int)
	 */
	public void writeCALL(X86Register regBase, X86Register regIndex, int scale,
			int disp) {
		println("\tcall dword [" + regBase + "+" + regIndex + "*" + scale
				+ disp(disp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCDQ()
	 */
	public void writeCDQ() {

		println("\tcdq");
	}

	/**
	 * Create a CMOVcc dst,src
	 * 
	 * @param ccOpcode
	 * @param dst
	 * @param src
	 */
	public void writeCMOVcc(int ccOpcode, X86Register dst, X86Register src) {

		println("\tCMOV" + ccName(ccOpcode) + " " + dst + "," + src);
	}

	/**
	 * Create a CMOVcc dst,[src+srcDisp]
	 * 
	 * @param dst
	 * @param src
	 * @param srcDisp
	 */
	public void writeCMOVcc(int ccOpcode, X86Register dst, X86Register src,
			int srcDisp) {

		println("\tCMOV" + ccName(ccOpcode) + " " + dst + ",[" + src
				+ disp(srcDisp) + "]");
	}

	/**
	 * Create a CMP [reg1+disp], reg2
	 * 
	 * @param reg1
	 * @param disp
	 * @param reg2
	 */
	public void writeCMP(X86Register reg1, int disp, X86Register reg2) {

		println("\tcmp [" + reg1 + disp(disp) + "]," + reg2);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCMP(X86Register,
	 *      X86Register)
	 */
	public void writeCMP(X86Register reg1, X86Register reg2) {

		println("\tcmp " + reg1 + "," + reg2);
	}

	/**
	 * Create a CMP reg1, [reg2+disp]
	 * 
	 * @param reg1
	 * @param reg2
	 * @param disp
	 */
	public void writeCMP(X86Register reg1, X86Register reg2, int disp) {

		println("\tcmp " + reg1 + ",[" + reg2 + disp(disp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCMP_Const(X86Register,
	 *      int)
	 */
	public void writeCMP_Const(X86Register reg, int imm32) {

		println("\tcmp " + reg + ",0x" + NumberUtils.hex(imm32));
	}

	/**
	 * Create a CMP [reg+disp], imm32
	 * 
	 * @param reg
	 * @param disp
	 * @param imm32
	 */
	public void writeCMP_Const(X86Register reg, int disp, int imm32) {
		println("\tcmp dword [" + reg + disp(disp) + "],0x"
				+ NumberUtils.hex(imm32));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCMP_EAX(int)
	 */
	public void writeCMP_EAX(int imm32) {

		println("\tcmp eax,0x" + NumberUtils.hex(imm32));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCMP_MEM(int, int)
	 */
	public void writeCMP_MEM(int memPtr, int imm32) {

		println("\tcmp dword [" + memPtr + "],0x" + NumberUtils.hex(imm32));
	}

	/**
	 * Create a CMP reg,[memPtr]
	 * 
	 * @param reg
	 * @param memPtr
	 */
	public void writeCMP_MEM(X86Register reg, int memPtr) {

		println("\tcmp " + reg + ",dword [" + memPtr + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeCMPXCHG_EAX(X86Register,
	 *      int, X86Register, boolean)
	 */
	public void writeCMPXCHG_EAX(X86Register dstReg, int dstDisp,
			X86Register srcReg, boolean lock) {

		println("\tcmpxchg [" + dstReg + disp(dstDisp) + "]," + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeDEC(X86Register)
	 */
	public void writeDEC(X86Register dstReg) {

		println("\tdec " + dstReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeDEC(X86Register, int)
	 */
	public void writeDEC(X86Register dstReg, int dstDisp) {

		println("\tdec dword [" + dstReg + disp(dstDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFADD32(X86Register, int)
	 */
	public void writeFADD32(X86Register srcReg, int srcDisp) {

		println("\tfadd dword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFADD64(X86Register, int)
	 */
	public void writeFADD64(X86Register srcReg, int srcDisp) {

		println("\tfadd qword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFADDP(X86Register)
	 */
	public void writeFADDP(X86Register fpuReg) {
		println("\tfaddp " + fpuReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFCHS()
	 */
	public void writeFCHS() {
		println("\tfchs");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFDIV32(X86Register, int)
	 */
	public void writeFDIV32(X86Register srcReg, int srcDisp) {

		println("\tfdiv dword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFDIV64(X86Register, int)
	 */
	public void writeFDIV64(X86Register srcReg, int srcDisp) {

		println("\tfdiv qword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFDIVP(X86Register)
	 */
	public void writeFDIVP(X86Register fpuReg) {
		println("\tfdivp " + fpuReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFFREE(X86Register)
	 */
	public void writeFFREE(X86Register fReg) {

		println("\tffree " + fReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFILD32(X86Register, int)
	 */
	public void writeFILD32(X86Register dstReg, int dstDisp) {

		println("\tfild dword [" + dstReg + disp(dstDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFILD64(X86Register, int)
	 */
	public void writeFILD64(X86Register dstReg, int dstDisp) {
		println("\tfild qword [" + dstReg + disp(dstDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFISTP32(X86Register, int)
	 */
	public void writeFISTP32(X86Register dstReg, int dstDisp) {

		println("\tfistp dword [" + dstReg + disp(dstDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFISTP64(X86Register, int)
	 */
	public void writeFISTP64(X86Register dstReg, int dstDisp) {

		println("\tfistp qword [" + dstReg + disp(dstDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFLD32(X86Register, int)
	 */
	public void writeFLD32(X86Register srcReg, int srcDisp) {

		println("\tfld dword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFLD32(X86Register,
	 *      X86Register, int, int)
	 */
	public void writeFLD32(X86Register srcBaseReg, X86Register srcIndexReg,
			int srcScale, int srcDisp) {
		println("\tfld dword [" + srcBaseReg + '+' + srcIndexReg + '*'
				+ srcScale + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFLD64(X86Register, int)
	 */
	public void writeFLD64(X86Register srcReg, int srcDisp) {

		println("\tfld qword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFLD64(X86Register, int)
	 */
	public void writeFLD64(X86Register srcBaseReg, X86Register srcIndexReg,
			int srcScale, int srcDisp) {
		println("\tfld qword [" + srcBaseReg + '+' + srcIndexReg + '*'
				+ srcScale + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFMUL32(X86Register, int)
	 */
	public void writeFMUL32(X86Register srcReg, int srcDisp) {
		println("\tfmul dword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFMUL64(X86Register, int)
	 */
	public void writeFMUL64(X86Register srcReg, int srcDisp) {

		println("\tfmul qword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFMULP(X86Register)
	 */
	public void writeFMULP(X86Register fpuReg) {
		println("\tfmulp " + fpuReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFNSTSW_AX()
	 */
	public void writeFNSTSW_AX() {

		println("\tfnstsw_ax");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFPREM()
	 */
	public void writeFPREM() {
		println("\tfprem");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFSTP(X86Register)
	 */
	public void writeFSTP(X86Register fpuReg) {
		println("\tfstp " + fpuReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFSTP32(X86Register, int)
	 */
	public void writeFSTP32(X86Register dstReg, int dstDisp) {
		println("\tfstp dword [" + dstReg + disp(dstDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFSTP64(X86Register, int)
	 */
	public void writeFSTP64(X86Register dstReg, int dstDisp) {

		println("\tfstp qword [" + dstReg + disp(dstDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFSUB32(X86Register, int)
	 */
	public void writeFSUB32(X86Register srcReg, int srcDisp) {
		println("\tfsub32 dword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFSUB64(X86Register, int)
	 */
	public void writeFSUB64(X86Register srcReg, int srcDisp) {

		println("\tfsub64 qword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFSUBP(X86Register)
	 */
	public void writeFSUBP(X86Register fpuReg) {
		println("\tfsubp " + fpuReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFUCOMPP()
	 */
	public void writeFUCOMPP() {

		println("\tfucompp");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeFXCH(X86Register)
	 */
	public void writeFXCH(X86Register fpuReg) {
		println("\tfxch " + fpuReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeIDIV_EAX(X86Register)
	 */
	public void writeIDIV_EAX(X86Register srcReg) {

		println("\tidiv " + srcReg);
	}

	// LS
	/**
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeIDIV_EAX(X86Register srcReg, int srcDisp) {
		println("\tidiv dword [" + srcReg + disp(srcDisp) + "]");
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 */
	public void writeIMUL(X86Register dstReg, X86Register srcReg) {
		println("\timul " + dstReg + "," + srcReg);
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeIMUL(X86Register dstReg, X86Register srcReg, int srcDisp) {
		println("\timul " + dstReg + ",dword[" + srcReg + disp(srcDisp) + "]");
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param imm32
	 */
	public void writeIMUL_3(X86Register dstReg, X86Register srcReg, int imm32) {
		println("\timul " + dstReg + "," + srcReg + ",0x"
				+ NumberUtils.hex(imm32));
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 * @param imm32
	 */
	public void writeIMUL_3(X86Register dstReg, X86Register srcReg,
			int srcDisp, int imm32) {
		println("\timul " + dstReg + ",dword[" + srcReg + disp(srcDisp)
				+ "],0x" + NumberUtils.hex(imm32));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeIMUL_EAX(X86Register)
	 */
	public void writeIMUL_EAX(X86Register srcReg) {

		println("\timul " + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeINC(X86Register)
	 */
	public void writeINC(X86Register dstReg) {

		println("\tinc " + dstReg);
	}

	/**
	 * Create a inc [reg32+disp]
	 * 
	 * @param dstReg
	 */
	public void writeINC(X86Register dstReg, int disp) {
		println("\tinc [" + dstReg + disp(disp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeINT(int)
	 */
	public void writeINT(int vector) {

		println("\tint 0x" + NumberUtils.hex(vector, 2));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeJCC(Label, int)
	 */
	public void writeJCC(Label label, int jumpOpcode) {

		println("\tj" + ccName(jumpOpcode) + " " + label(label));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeJMP(Label)
	 */
	public void writeJMP(Label label) {

		println("\tjmp " + label(label));
	}

	/**
	 * Create a absolute jump to address stored at the given offset in the given
	 * table pointer.
	 * 
	 * @param tablePtr
	 * @param offset
	 * @param rawAddress
	 *            If true, tablePtr is a raw address
	 */
	public void writeJMP(Object tablePtr, int offset, boolean rawAddress) {
		if (tablePtr == null)
			tablePtr = "null"; // workaround for a peculiar NPE in StringBuffer
		// 298
		// println("\tjmp dword [" + tablePtr + disp(offset) + "]");
		println("\tjmp dword [ left out in: TextX86Stream.writeJMP(Object tablePtr, int offset, boolean rawAddress)]");
	}

	/**
	 * Create a absolute jump to address stored at the given offset (in
	 * register) in the given table pointer.
	 * 
	 * @param tablePtr
	 * @param offsetReg
	 */
	public void writeJMP(Object tablePtr, X86Register offsetReg) {
		println("\tjmp dword [" + tablePtr + "+" + offsetReg + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeJMP(X86Register)
	 */
	public void writeJMP(X86Register reg32) {

		println("\tjmp " + reg32);
	}

	/**
	 * Create a absolute jump to [reg32+disp]
	 * 
	 * @param reg32
	 */
	public final void writeJMP(X86Register reg32, int disp) {
		println("\tjmp [" + reg32 + disp(disp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeLEA(X86Register,
	 *      X86Register, int)
	 */
	public void writeLEA(X86Register dstReg, X86Register srcReg, int disp) {

		println("\tlea " + dstReg + ",[" + srcReg + disp(disp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeLEA(X86Register,
	 *      X86Register, X86Register, int, int)
	 */
	public void writeLEA(X86Register dstReg, X86Register srcReg,
			X86Register srcIdxReg, int scale, int disp) {

		println("\tlea " + dstReg + ",[" + srcReg + disp(disp) + "+"
				+ srcIdxReg + "*" + scale + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeLODSD()
	 */
	public void writeLODSD() {

		println("\tlodsd");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeLOOP(Label)
	 */
	public void writeLOOP(Label label) throws UnresolvedObjectRefException {

		println("\tloop " + label(label));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeMOV(int, X86Register, int,
	 *      X86Register)
	 */
	public void writeMOV(int operandSize, X86Register dstReg, int dstDisp,
			X86Register srcReg) {

		println("\tmov " + operandSize(operandSize) + "[" + dstReg
				+ disp(dstDisp) + "]," + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeMOV(int, X86Register,
	 *      X86Register)
	 */
	public void writeMOV(int operandSize, X86Register dstReg, X86Register srcReg) {

		println("\tmov " + dstReg + "," + operandSize(operandSize) + " "
				+ srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeMOV(int, X86Register,
	 *      X86Register, int)
	 */
	public void writeMOV(int operandSize, X86Register dstReg,
			X86Register srcReg, int srcDisp) {

		println("\tmov " + dstReg + "," + operandSize(operandSize) + "["
				+ srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeMOV(int, X86Register,
	 *      X86Register, int, int, X86Register)
	 */
	public void writeMOV(int operandSize, X86Register dstReg,
			X86Register dstIdxReg, int scale, int dstDisp, X86Register srcReg) {

		println("\tmov " + operandSize(operandSize) + "[" + dstReg
				+ disp(dstDisp) + "+" + dstIdxReg + "*" + scale + "]," + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeMOV(int, X86Register,
	 *      X86Register, X86Register, int, int)
	 */
	public void writeMOV(int operandSize, X86Register dstReg,
			X86Register srcReg, X86Register srcIdxReg, int scale, int srcDisp) {

		println("\tmov " + dstReg + "," + operandSize(operandSize) + "["
				+ srcReg + disp(srcDisp) + "+" + srcIdxReg + "*" + scale + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeMOV_Const(X86Register,
	 *      int)
	 */
	public void writeMOV_Const(X86Register dstReg, int imm32) {

		println("\tmov " + dstReg + ",0x" + NumberUtils.hex(imm32));
	}

	/**
	 * Create a mov [destReg+destDisp], <imm32>
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeMOV_Const(X86Register dstReg, int dstDisp, int imm32) {

		println("\tmov dword [" + dstReg + disp(dstDisp) + "],0x"
				+ NumberUtils.hex(imm32));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeMOV_Const(X86Register,
	 *      java.lang.Object)
	 */
	public void writeMOV_Const(X86Register dstReg, Object label) {

		println("\tmov " + dstReg + "," + label);
	}

	/**
	 * Create a mov [destReg+dstIdxReg*scale+destDisp], <imm32>
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeMOV_Const(X86Register dstReg, X86Register dstIdxReg,
			int scale, int dstDisp, int imm32) {
		println("\tmov dword [" + dstReg + "+" + dstIdxReg + "*" + scale
				+ disp(dstDisp) + "],0x" + NumberUtils.hex(imm32));
	}

	public void writeMOVSD(GPR dst, int dstDisp, XMM src) {
		println("\tmovsd qword [" + dst + disp(dstDisp) + "]," + src);
	}

	public void writeMOVSD(XMM dst, GPR src, int srcDisp) {
		println("\tmovsd " + dst + ",qword [" + src + disp(srcDisp) + "]");
	}

	public void writeMOVSD(XMM dst, XMM src) {
		println("\tmovsd " + dst + "," + src);
	}

	public void writeMOVSS(GPR dst, int dstDisp, XMM src) {
		println("\tmovss dword [" + dst + disp(dstDisp) + "]," + src);

	}

	public void writeMOVSS(XMM dst, GPR src, int srcDisp) {
		println("\tmovss " + dst + ",dword [" + src + disp(srcDisp) + "]");
	}

	public void writeMOVSS(XMM dst, XMM src) {
		println("\tmovss " + dst + "," + src);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeMOVSX(X86Register,
	 *      X86Register, int)
	 */
	public void writeMOVSX(X86Register dstReg, X86Register srcReg, int srcSize) {

		println("\tmovsx " + dstReg + "," + operandSize(srcSize) + " " + srcReg);
	}

	public void writeMOVSX(X86Register dstReg, X86Register srcReg, int srcDisp,
			int srcSize) {
		println("\tmovsx " + dstReg + "," + operandSize(srcSize) + " " + "["
				+ srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeMOVZX(X86Register,
	 *      X86Register, int)
	 */
	public void writeMOVZX(X86Register dstReg, X86Register srcReg, int srcSize) {

		println("\tmovzx " + dstReg + "," + operandSize(srcSize) + " " + srcReg);
	}

	public void writeMOVZX(X86Register dstReg, X86Register srcReg, int srcDisp,
			int srcSize) {
		println("\tmovzx " + dstReg + "," + operandSize(srcSize) + " " + "["
				+ srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeMUL_EAX(X86Register)
	 */
	public void writeMUL_EAX(X86Register srcReg) {

		println("\tmul " + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeNEG(X86Register)
	 */
	public void writeNEG(X86Register dstReg) {

		println("\tneg " + dstReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeNEG(X86Register, int)
	 */
	public void writeNEG(X86Register dstReg, int dstDisp) {

		println("\tneg dword [" + dstReg + disp(dstDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeNOP()
	 */
	public void writeNOP() {

		println("\tnop");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeNOT(X86Register)
	 */
	public void writeNOT(X86Register dstReg) {

		println("\tnot " + dstReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeNOT(X86Register, int)
	 */
	public void writeNOT(X86Register dstReg, int dstDisp) {

		println("\tnot dword [" + dstReg + disp(dstDisp) + "]");
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public void writeOR(X86Register dstReg, int imm32) {
		println("\tor " + dstReg + ",0x" + NumberUtils.hex(imm32));
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeOR(X86Register dstReg, int dstDisp, int imm32) {
		println("\tor dword[" + dstReg + disp(dstDisp) + "],0x"
				+ NumberUtils.hex(imm32));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeOR(X86Register, int,
	 *      X86Register)
	 */
	public void writeOR(X86Register dstReg, int dstDisp, X86Register srcReg) {

		println("\tor dword [" + dstReg + disp(dstDisp) + "]," + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeOR(X86Register,
	 *      X86Register)
	 */
	public void writeOR(X86Register dstReg, X86Register srcReg) {

		println("\tor " + dstReg + "," + srcReg);
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeOR(X86Register dstReg, X86Register srcReg, int srcDisp) {
		println("\tor " + dstReg + ",dword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePOP(X86Register)
	 */
	public void writePOP(X86Register dstReg) {

		println("\tpop " + dstReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePOP(X86Register, int)
	 */
	public void writePOP(X86Register dstReg, int dstDisp) {

		println("\tpop dword [" + dstReg + disp(dstDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePOPA()
	 */
	public void writePOPA() {
		println("\tpopa");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePrefix(int)
	 */
	public void writePrefix(int prefix) {

		final String str;
		switch (prefix) {
		case FS_PREFIX:
			str = "fs";
			break;
		default:
			throw new IllegalArgumentException("Unknown prefix " + prefix);
		}
		println("\tprefix " + str);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePUSH(int)
	 * @return The ofset of the start of the instruction.
	 */
	public int writePUSH(int imm32) {
		return println("\tpush 0x" + NumberUtils.hex(imm32));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePUSH(X86Register)
	 * @return The ofset of the start of the instruction.
	 */
	public int writePUSH(X86Register srcReg) {
		return println("\tpush " + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePUSH(X86Register, int)
	 * @return The ofset of the start of the instruction.
	 */
	public int writePUSH(X86Register srcReg, int srcDisp) {
		return println("\tpush dword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePUSH(X86Register,
	 *      X86Register, int, int)
	 * @return The ofset of the start of the instruction.
	 */
	public int writePUSH(X86Register srcBaseReg, X86Register srcIndexReg,
			int srcScale, int srcDisp) {
		return println("\tpush dword [" + srcBaseReg + disp(srcDisp) + "+"
				+ srcIndexReg + "*" + srcScale + "]");
	}

	// PR
	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePUSH_Const(Object)
	 * @return The offset of the start of the instruction.
	 */
	public int writePUSH_Const(Object objRef) {
		return println("\tpush " + objRef);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writePUSHA()
	 */
	public void writePUSHA() {
		println("\tpusha");
	}

	public void writeRDTSC() {
		println("\trdtsc");
	}

	/**
	 * Create 32-bit offset relative to the current (after this offset) offset.
	 * 
	 * @param label
	 */
	public void writeRelativeObjectRef(Label label) {
		println("\tdd relative " + label(label));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeRET()
	 */
	public void writeRET() {

		println("\tret");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeRET(int)
	 */
	public void writeRET(int imm16) {

		println("\tret " + imm16);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSAHF()
	 */
	public void writeSAHF() {

		println("\tsahf");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSAL(X86Register, int)
	 */
	public void writeSAL(X86Register dstReg, int imm8) {

		println("\tsal " + dstReg + "," + imm8);
	}

	/**
	 * @param srcReg
	 * @param srcDisp
	 * @param imm8
	 */
	public void writeSAL(X86Register srcReg, int srcDisp, int imm8) {
		println("\tsal dword [" + srcReg + disp(srcDisp) + "]," + imm8);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSAL_CL(X86Register)
	 */
	public void writeSAL_CL(X86Register dstReg) {

		println("\tsal " + dstReg + ",cl");
	}

	/**
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeSAL_CL(X86Register srcReg, int srcDisp) {
		println("\tsal dword [" + srcReg + disp(srcDisp) + "],cl");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSAR(X86Register, int)
	 */
	public void writeSAR(X86Register dstReg, int imm8) {

		println("\tsar " + dstReg + "," + imm8);
	}

	/**
	 * @param srcReg
	 * @param srcDisp
	 * @param imm8
	 */
	public void writeSAR(X86Register srcReg, int srcDisp, int imm8) {
		println("\tsar dword [" + srcReg + disp(srcDisp) + "]," + imm8);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSAR_CL(X86Register)
	 */
	public void writeSAR_CL(X86Register dstReg) {

		println("\tsar " + dstReg + ",cl");
	}

	/**
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeSAR_CL(X86Register srcReg, int srcDisp) {
		println("\tsar dword [" + srcReg + disp(srcDisp) + "],cl");
	}

	/**
	 * Create a SBB dstReg, imm32
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public void writeSBB(X86Register dstReg, int imm32) {

		println("\tsbb " + dstReg + ",0x" + NumberUtils.hex(imm32));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSBB(X86Register, int, int)
	 */
	public void writeSBB(X86Register dstReg, int dstDisp, int imm32) {

		println("\tsbb dword [" + dstReg + disp(dstDisp) + "],0x"
				+ NumberUtils.hex(imm32));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSBB(X86Register, int,
	 *      X86Register)
	 */
	public void writeSBB(X86Register dstReg, int dstDisp, X86Register srcReg) {

		println("\tsbb dword [" + dstReg + disp(dstDisp) + "]," + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSBB(X86Register,
	 *      X86Register)
	 */
	public void writeSBB(X86Register dstReg, X86Register srcReg) {

		println("\tsbb " + dstReg + "," + srcReg);
	}

	/**
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeSBB(X86Register dstReg, X86Register srcReg, int srcDisp) {
		println("\tsbb " + dstReg + ",dword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * Create a SETcc dstReg
	 * 
	 * @param dstReg
	 * @param cc
	 */
	public void writeSETCC(X86Register dstReg, int cc) {
		println("\tset" + ccName(cc) + " " + dstReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSHL(X86Register, int)
	 */
	public void writeSHL(X86Register dstReg, int imm8) {

		println("\tshl " + dstReg + "," + imm8);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSHL(X86Register, int, int)
	 */
	public void writeSHL(X86Register dstReg, int dstDisp, int imm8) {
		println("\tshl dword [" + dstReg + disp(dstDisp) + "]," + imm8);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSHL_CL(X86Register)
	 */
	public void writeSHL_CL(X86Register dstReg) {

		println("\tshl " + dstReg + ",cl");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSHL_CL(X86Register, int)
	 */
	public void writeSHL_CL(X86Register dstReg, int dstDisp) {
		println("\tshl dword [" + dstReg + disp(dstDisp) + "],CL");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSHLD_CL(X86Register,
	 *      X86Register)
	 */
	public void writeSHLD_CL(X86Register dstReg, X86Register srcReg) {

		println("\tshld " + dstReg + "," + srcReg + ",cl");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSHR(X86Register, int)
	 */
	public void writeSHR(X86Register dstReg, int imm8) {

		println("\tshr " + dstReg + "," + imm8);
	}

	/**
	 * @param srcReg
	 * @param srcDisp
	 * @param imm8
	 */
	public void writeSHR(X86Register srcReg, int srcDisp, int imm8) {
		println("\tshr dword [" + srcReg + disp(srcDisp) + "]," + imm8);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSHR_CL(X86Register)
	 */
	public void writeSHR_CL(X86Register dstReg) {

		println("\tshr " + dstReg + ",cl");
	}

	/**
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeSHR_CL(X86Register srcReg, int srcDisp) {
		println("\tshr dword [" + srcReg + disp(srcDisp) + "],cl");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSHRD_CL(X86Register,
	 *      X86Register)
	 */
	public void writeSHRD_CL(X86Register dstReg, X86Register srcReg) {

		println("\tshrd " + dstReg + "," + srcReg + ",cl");
	}

	/**
	 * Create a SUB reg, imm32
	 * 
	 * @param reg
	 * @param imm32
	 */
	public final void writeSUB(X86Register reg, int imm32) {
		println("\tsub " + reg + "," + imm32);
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeSUB(X86Register dstReg, int dstDisp, int imm32) {
		println("\tsub dword[" + dstReg + disp(dstDisp) + "],0x"
				+ NumberUtils.hex(imm32));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSUB(X86Register, int,
	 *      X86Register)
	 */
	public void writeSUB(X86Register dstReg, int dstDisp, X86Register srcReg) {

		println("\tsub dword [" + dstReg + disp(dstDisp) + "]," + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeSUB(X86Register,
	 *      X86Register)
	 */
	public void writeSUB(X86Register dstReg, X86Register srcReg) {
		println("\tsub " + dstReg + "," + srcReg);
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeSUB(X86Register dstReg, X86Register srcReg, int srcDisp) {
		println("\tsub " + dstReg + ",dword [" + srcReg + disp(srcDisp) + "]");
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeTEST(X86Register, int)
	 */
	public void writeTEST(X86Register reg, int imm32) {

		println("\ttest " + reg + ",0x" + NumberUtils.hex(imm32));
	}

	/**
	 * Create a TEST [reg+disp], imm32
	 * 
	 * @param reg
	 * @param disp
	 * @param imm32
	 */
	public void writeTEST(X86Register reg, int disp, int imm32) {
		println("\ttest [" + reg + disp(disp) + "],0x" + NumberUtils.hex(imm32));
	}

	/**
	 * Create a TEST reg1, reg2
	 * 
	 * @param reg1
	 * @param reg2
	 */
	public void writeTEST(X86Register reg1, X86Register reg2) {

		println("\ttest " + reg1 + "," + reg2);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeTEST_AL(int)
	 */
	public void writeTEST_AL(int value) {

		println("\ttest al," + value);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeTEST_EAX(int)
	 */
	public void writeTEST_EAX(int value) {

		println("\ttest eax," + value);
	}

	/**
	 * @see org.jnode.assembler.NativeStream#writeTo(java.io.OutputStream)
	 */
	public void writeTo(OutputStream os) throws IOException {
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeXCHG(X86Register, int,
	 *      X86Register)
	 */
	public void writeXCHG(X86Register dstReg, int dstDisp, X86Register srcReg) {
		println("\txchg dword [" + dstReg + disp(dstDisp) + "], " + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeXCHG(X86Register,
	 *      X86Register)
	 */
	public void writeXCHG(X86Register dstReg, X86Register srcReg) {
		println("\txchg " + dstReg + ", " + srcReg);
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param imm32
	 */
	public void writeXOR(X86Register dstReg, int imm32) {
		println("\txor " + dstReg + ",0x" + NumberUtils.hex(imm32));
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param dstDisp
	 * @param imm32
	 */
	public void writeXOR(X86Register dstReg, int dstDisp, int imm32) {
		println("\txor dword[" + dstReg + disp(dstDisp) + "],0x"
				+ NumberUtils.hex(imm32));
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeXOR(X86Register, int,
	 *      X86Register)
	 */
	public void writeXOR(X86Register dstReg, int dstDisp, X86Register srcReg) {
		println("\txor dword [" + dstReg + disp(dstDisp) + "]," + srcReg);
	}

	/**
	 * @see org.jnode.assembler.x86.X86Assembler#writeXOR(X86Register,
	 *      X86Register)
	 */
	public void writeXOR(X86Register dstReg, X86Register srcReg) {
		println("\txor " + dstReg + "," + srcReg);
	}

	// LS
	/**
	 * 
	 * @param dstReg
	 * @param srcReg
	 * @param srcDisp
	 */
	public void writeXOR(X86Register dstReg, X86Register srcReg, int srcDisp) {
		println("\txor " + dstReg + ",dword [" + srcReg + disp(srcDisp) + "]");
	}
}
