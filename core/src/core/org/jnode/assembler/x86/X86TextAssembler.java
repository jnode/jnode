/*
 * $Id$
 *
 * JNode.org
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
import org.jnode.assembler.x86.X86Register.CRX;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.GPR32;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.assembler.x86.X86Register.SR;
import org.jnode.assembler.x86.X86Register.XMM;
import org.jnode.util.NumberUtils;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.X86CpuID;

/**
 * Debug version of AbstractX86Stream.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
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

    private String size(int operandSize) {
        switch (operandSize) {
            case BITS8:
                return "byte";
            case BITS16:
                return "word";
            case BITS32:
                return "dword";
            case BITS64:
                return "qword";
            default:
                throw new IllegalArgumentException("Invalid operand size "
                    + operandSize);
        }
    }

    /**
     * Flush the contents of the used stream.
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
    public Collection<? extends ObjectRef> getObjectRefs() {
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

    protected final int println(String msg) {
        final int rc = idx + buf.length();
        buf.append(msg);
        buf.append('\n');
        return rc;
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#set64(int, long)
     */
    public void set64(int offset, long v64) {
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#set32(int, int)
     */
    public void set32(int offset, int v32) {
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#set16(int, int)
     */
    public void set16(int offset, int v16) {
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
    public void writeADC(GPR dstReg, int imm32) {
        println("\tadc " + dstReg + "," + imm32);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeADC(int, GPR, int, int)
     */
    public void writeADC(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        println("\tadc " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "]," + imm32);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeADC(GPR, int, GPR)
     */
    public void writeADC(GPR dstReg, int dstDisp, GPR srcReg) {
        println("\tadc [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    /**
     * Create a ADC dstReg, srcReg
     *
     * @param dstReg
     * @param srcReg
     */
    public void writeADC(GPR dstReg, GPR srcReg) {

        println("\tadc " + dstReg + "," + srcReg);
    }

    /**
     * Create a ADC dstReg, [srcReg+srcDisp]
     *
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeADC(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tadc " + dstReg + ",[" + srcReg + disp(srcDisp) + "]");
    }

    // LS
    /**
     * @param dstReg
     * @param imm32
     */
    public void writeADD(GPR dstReg, int imm32) {
        println("\tadd " + dstReg + ",0x" + NumberUtils.hex(imm32));
    }

    public void writeADD(int operandSize, int dstDisp, int imm32) {
        println("\tadd " + size(operandSize) + "[" + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * Create a ADD [dstReg+dstDisp], imm32
     *
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeADD(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        println("\tadd " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see @see org.jnode.assembler.x86.X86Assembler#writeADD(int, SR, int, int)
     */
    public void writeADD(int operandSize, SR dstReg, int dstDisp, int imm32) {
        println("\tadd " + size(operandSize) + "[" + dstReg + ":0x"
            + NumberUtils.hex(dstDisp) + "],0x" + NumberUtils.hex(imm32));
    }

    public void writeADD_MEM(X86Register.GPR reg, int memPtr32) {
        println("\tadd " + reg + ",[0x" + NumberUtils.hex(memPtr32) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeADD(GPR, int, GPR)
     */
    public void writeADD(GPR dstReg, int dstDisp, GPR srcReg) {
        println("\tadd [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeADD(GPR, GPR)
     */
    public void writeADD(GPR dstReg, GPR srcReg) {
        println("\tadd " + dstReg + "," + srcReg);
    }

    /**
     * Create a ADD dstReg, [srcReg+srcDisp]
     *
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeADD(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tadd " + dstReg + ",[" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeAND(GPR, int)
     */
    public void writeAND(GPR reg, int imm32) {

        println("\tand " + reg + ",0x" + NumberUtils.hex(imm32));
    }

    public void writeAND(int operandSize, int dstDisp, int imm32) {
        println("\tand " + size(operandSize) + "[" + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeAND(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        println("\tand " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeAND(GPR, int, GPR)
     */
    public void writeAND(GPR dstReg, int dstDisp, GPR srcReg) {

        println("\tand [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    /**
     * @param operandSize
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeAND(int operandSize, X86Register.SR dstReg, int dstDisp, int imm32) {
        println("\tand " + size(operandSize) + "[" + dstReg + ":0x" + NumberUtils.hex(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeAND(GPR, GPR)
     */
    public void writeAND(GPR dstReg, GPR srcReg) {

        println("\tand " + dstReg + "," + srcReg);
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeAND(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tand " + dstReg + ",[" + srcReg + disp(srcDisp) + "]");
    }

    public void writeArithSSEDOp(int operation, XMM dst, GPR src, int srcDisp) {
        final String op = getSSEOperationName(operation);
        println("\t" + op + "D" + dst + ", qword [" + src + disp(srcDisp) + "]");
    }

    public void writeArithSSEDOp(int operation, XMM dst, XMM src) {
        final String op = getSSEOperationName(operation);
        println("\t" + op + "D" + dst + ", " + src);
    }

    public void writeArithSSESOp(int operation, XMM dst, GPR src, int srcDisp) {
        final String op = getSSEOperationName(operation);
        println("\t" + op + "S" + dst + ", dword [" + src + disp(srcDisp) + "]");
    }

    public void writeArithSSESOp(int operation, XMM dst, XMM src) {
        final String op = getSSEOperationName(operation);
        println("\t" + op + "S" + dst + ", " + src);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeBOUND(GPR, GPR, int)
     */
    public void writeBOUND(GPR lReg, GPR rReg, int rDisp) {
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
     * @param rawAddress If true, tablePtr is a raw address
     */
    public void writeCALL(Object tablePtr, int offset, boolean rawAddress) {
        println("\tcall [" + tablePtr + disp(offset) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeCALL(GPR)
     */
    public void writeCALL(GPR reg) {
        println("\tcall " + reg);
    }

    /**
     * Create a call to address stored at the given [reg+offset].
     *
     * @param reg
     * @param offset
     */
    public void writeCALL(GPR reg, int offset) {
        println("\tcall [" + reg + disp(offset) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeCALL(GPR, GPR, int, int)
     */
    public void writeCALL(GPR regBase, GPR regIndex, int scale, int disp) {
        println("\tcall [" + regBase + "+" + regIndex + "*" + scale
            + disp(disp) + "]");
    }

    public void writeCALL(GPR regIndex, int scale, int disp) {
        println("\tcall [" + regIndex + "*" + scale + disp(disp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeCDQ(int)
     */
    public void writeCDQ(int operandSize) {
        testOperandSize(operandSize, BITS32 | BITS64);
        if (operandSize == BITS32) {
            println("\tcdq");
        } else {
            if (!code64) {
                throw new InvalidOpcodeException();
            }
            println("\tcqo");
        }
    }

    /**
     * Create a cdqe.
     * Sign extend EAX to RAX.
     * Only valid in 64-bit mode.
     */
    public void writeCDQE()
        throws InvalidOpcodeException {
        if (!code64) {
            throw new InvalidOpcodeException();
        }
        println("\tcdqe");
    }

    /**
     *
     */
    public void writeCLD() {
        println("\tCLD");
    }

    /**
     *
     */
    public void writeCLI() {
        println("\tCLI");
    }

    /**
     *
     */
    public void writeCLTS() {
        println("\tCLTS");
    }

    /**
     * Create a CMOVcc dst,src
     *
     * @param ccOpcode
     * @param dst
     * @param src
     */
    public void writeCMOVcc(int ccOpcode, GPR dst, GPR src) {
        println("\tCMOV" + ccName(ccOpcode) + " " + dst + "," + src);
    }

    /**
     * Create a CMOVcc dst,[src+srcDisp]
     *
     * @param dst
     * @param src
     * @param srcDisp
     */
    public void writeCMOVcc(int ccOpcode, GPR dst, GPR src, int srcDisp) {
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
    public void writeCMP(GPR reg1, int disp, GPR reg2) {
        println("\tcmp [" + reg1 + disp(disp) + "]," + reg2);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeCMP(GPR, GPR)
     */
    public void writeCMP(GPR reg1, GPR reg2) {
        println("\tcmp " + reg1 + "," + reg2);
    }

    /**
     * Create a CMP reg1, [reg2:disp]
     *
     * @param reg1
     * @param reg2
     * @param disp
     */
    public void writeCMP(GPR reg1, SR reg2, int disp) {
        println("\tcmp " + reg1 + ",[" + reg2 + ":0x" + NumberUtils.hex(disp) + "]");
    }

    /**
     * Create a CMP reg1, [reg2+disp]
     *
     * @param reg1
     * @param reg2
     * @param disp
     */
    public void writeCMP(GPR reg1, GPR reg2, int disp) {
        println("\tcmp " + reg1 + ",[" + reg2 + disp(disp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeCMP_Const(GPR, int)
     */
    public void writeCMP_Const(GPR reg, int imm32) {
        println("\tcmp " + reg + ",0x" + NumberUtils.hex(imm32));
    }

    /**
     * Create a CMP [reg+disp], imm32
     *
     * @param reg
     * @param disp
     * @param imm32
     */
    public void writeCMP_Const(int operandSize, GPR reg, int disp, int imm32) {
        println("\tcmp " + size(operandSize) + "[" + reg + disp(disp) + "],0x"
            + NumberUtils.hex(imm32));
    }

    /**
     * Create a CMP [dstReg:dstDisp], imm32
     *
     * @param operandSize
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeCMP_Const(int operandSize, X86Register.SR dstReg, int dstDisp, int imm32) {
        println("\tcmp " + size(operandSize) + "[" + dstReg + ":0x" + NumberUtils.hex(dstDisp) + "],0x"
            + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeCMP_EAX(int, int)
     */
    public void writeCMP_EAX(int operandSize, int imm32) {
        testOperandSize(operandSize, BITS32 | BITS64);
        if (operandSize == BITS32) {
            println("\tcmp eax,0x" + NumberUtils.hex(imm32));
        } else {
            println("\tcmp rax,0x" + NumberUtils.hex(imm32));
        }
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeCMP_MEM(int, int, int)
     */
    public void writeCMP_MEM(int operandSize, int memPtr, int imm32) {
        println("\tcmp " + size(operandSize) + "[" + memPtr + "],0x"
            + NumberUtils.hex(imm32));
    }

    /**
     * Create a CMP reg,[memPtr]
     *
     * @param reg
     * @param memPtr
     */
    public void writeCMP_MEM(GPR reg, int memPtr) {
        println("\tcmp " + reg + ", [" + memPtr + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeCMPXCHG_EAX(GPR, int, GPR,
     *      boolean)
     */
    public void writeCMPXCHG_EAX(GPR dstReg, int dstDisp, GPR srcReg,
                                 boolean lock) {
        println("\tcmpxchg [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    public void writeCPUID() {
        println("\tcpuid");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeDEC(GPR)
     */
    public void writeDEC(GPR dstReg) {
        println("\tdec " + dstReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeDEC(int, GPR, int)
     */
    public void writeDEC(int operandSize, GPR dstReg, int dstDisp) {
        println("\tdec " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeDIV_EAX(GPR)
     */
    public void writeDIV_EAX(GPR srcReg) {

        println("\tdiv " + srcReg);
    }

    public void writeEMMS() {
        println("\temms");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFADD32(GPR, int)
     */
    public void writeFADD32(GPR srcReg, int srcDisp) {
        println("\tfadd dword [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFADD64(GPR, int)
     */
    public void writeFADD64(GPR srcReg, int srcDisp) {
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
     * @see org.jnode.assembler.x86.X86Assembler#writeFDIV32(GPR, int)
     */
    public void writeFDIV32(GPR srcReg, int srcDisp) {

        println("\tfdiv dword [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFDIV64(GPR, int)
     */
    public void writeFDIV64(GPR srcReg, int srcDisp) {

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
     * @see org.jnode.assembler.x86.X86Assembler#writeFILD32(GPR, int)
     */
    public void writeFILD32(GPR dstReg, int dstDisp) {

        println("\tfild dword [" + dstReg + disp(dstDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFILD64(GPR, int)
     */
    public void writeFILD64(GPR dstReg, int dstDisp) {
        println("\tfild qword [" + dstReg + disp(dstDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFISTP32(GPR, int)
     */
    public void writeFISTP32(GPR dstReg, int dstDisp) {

        println("\tfistp dword [" + dstReg + disp(dstDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFISTP64(GPR, int)
     */
    public void writeFISTP64(GPR dstReg, int dstDisp) {

        println("\tfistp qword [" + dstReg + disp(dstDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFLD32(GPR, int)
     */
    public void writeFLD32(GPR srcReg, int srcDisp) {

        println("\tfld dword [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFLD32(GPR, GPR, int, int)
     */
    public void writeFLD32(GPR srcBaseReg, GPR srcIndexReg, int srcScale,
                           int srcDisp) {
        println("\tfld dword [" + srcBaseReg + '+' + srcIndexReg + '*'
            + srcScale + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFLD64(GPR, int)
     */
    public void writeFLD64(GPR srcReg, int srcDisp) {

        println("\tfld qword [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFLD64(GPR, int)
     */
    public void writeFLD64(GPR srcBaseReg, GPR srcIndexReg, int srcScale,
                           int srcDisp) {
        println("\tfld qword [" + srcBaseReg + '+' + srcIndexReg + '*'
            + srcScale + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFLDCW(GPR, int)
     */
    public void writeFLDCW(GPR srcReg, int srcDisp) {
        println("\tfldcw word [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFMUL32(GPR, int)
     */
    public void writeFMUL32(GPR srcReg, int srcDisp) {
        println("\tfmul dword [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFMUL64(GPR, int)
     */
    public void writeFMUL64(GPR srcReg, int srcDisp) {

        println("\tfmul qword [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFMULP(X86Register)
     */
    public void writeFMULP(X86Register fpuReg) {
        println("\tfmulp " + fpuReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFNINIT()
     */
    public void writeFNINIT() {

        println("\tfninit");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFNSAVE(GPR, int)
     */
    public void writeFNSAVE(GPR srcReg, int srcDisp) {
        println("\tfnsave [" + srcReg + disp(srcDisp) + "]");
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
     * @see org.jnode.assembler.x86.X86Assembler#writeFRSTOR(GPR, int)
     */
    public void writeFRSTOR(GPR srcReg, int srcDisp) {
        println("\tfrstor [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFSTCW(GPR, int)
     */
    public void writeFSTCW(GPR srcReg, int srcDisp) {
        println("\tfstcw word [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFSTP(X86Register)
     */
    public void writeFSTP(X86Register fpuReg) {
        println("\tfstp " + fpuReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFSTP32(GPR, int)
     */
    public void writeFSTP32(GPR dstReg, int dstDisp) {
        println("\tfstp dword [" + dstReg + disp(dstDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFSTP64(GPR, int)
     */
    public void writeFSTP64(GPR dstReg, int dstDisp) {

        println("\tfstp qword [" + dstReg + disp(dstDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFSUB32(GPR, int)
     */
    public void writeFSUB32(GPR srcReg, int srcDisp) {
        println("\tfsub32 dword [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFSUB64(GPR, int)
     */
    public void writeFSUB64(GPR srcReg, int srcDisp) {

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
     * @see org.jnode.assembler.x86.X86Assembler#writeFXRSTOR(GPR, int)
     */
    public void writeFXRSTOR(GPR srcReg, int srcDisp) {
        println("\tfxrstor [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeFXSAVE(GPR, int)
     */
    public void writeFXSAVE(GPR srcReg, int srcDisp) {
        println("\tfxsave [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     *
     */
    public void writeHLT() {
        println("\thlt");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeIDIV_EAX(GPR)
     */
    public void writeIDIV_EAX(GPR srcReg) {

        println("\tidiv " + srcReg);
    }

    /**
     * @param srcReg
     * @param srcDisp
     */
    public void writeIDIV_EAX(int operandSize, GPR srcReg, int srcDisp) {
        println("\tidiv " + size(operandSize) + "[" + srcReg + disp(srcDisp)
            + "]");
    }

    /**
     * @param dstReg
     * @param srcReg
     */
    public void writeIMUL(GPR dstReg, GPR srcReg) {
        println("\timul " + dstReg + "," + srcReg);
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeIMUL(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\timul " + dstReg + ",[" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param imm32
     */
    public void writeIMUL_3(GPR dstReg, GPR srcReg, int imm32) {
        println("\timul " + dstReg + "," + srcReg + ",0x"
            + NumberUtils.hex(imm32));
    }

    // LS
    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     * @param imm32
     */
    public void writeIMUL_3(GPR dstReg, GPR srcReg, int srcDisp, int imm32) {
        println("\timul " + dstReg + ",[" + srcReg + disp(srcDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeIMUL_EAX(GPR)
     */
    public void writeIMUL_EAX(GPR srcReg) {
        println("\timul " + srcReg);
    }

    public void writeIN(int operandSize) {
        if (operandSize == X86Constants.BITS8) {
            println("\tin " + X86Register.AL + "," + X86Register.DX);
        } else if (operandSize == X86Constants.BITS16) {
            println("\tin " + X86Register.AX + "," + X86Register.DX);
        } else if (operandSize == X86Constants.BITS32) {
            println("\tin " + X86Register.EAX + "," + X86Register.DX);
        } else {
            throw new IllegalArgumentException("Invalid operand size for IN: " + operandSize);
        }
    }

    public void writeIN(int operandSize, int imm8) {
        if (operandSize == X86Constants.BITS8) {
            println("\tin " + X86Register.AL + "," + imm8);
        } else if (operandSize == X86Constants.BITS16) {
            println("\tin " + X86Register.AX + "," + imm8);
        } else if (operandSize == X86Constants.BITS32) {
            println("\tin " + X86Register.EAX + "," + imm8);
        } else {
            throw new IllegalArgumentException("Invalid operand size for IN: " + operandSize);
        }
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeINC(GPR)
     */
    public void writeINC(GPR dstReg) {
        println("\tinc " + dstReg);
    }

    public void writeINC(int operandSize, X86Register.SR dstReg, int disp) {
        println("\tinc " + size(operandSize) + "[" + dstReg + ":0x" + NumberUtils.hex(disp) + "]");
    }

    /**
     * Create a inc [reg32+disp]
     *
     * @param dstReg
     */
    public void writeINC(int operandSize, GPR dstReg, int disp) {
        println("\tinc " + size(operandSize) + "[" + dstReg + disp(disp) + "]");
    }

    public void writeINC(int operandSize, GPR dstReg, GPR dstIdxReg, int scale, int disp) {
        println("\tinc [" + dstReg + disp(disp) + "+" + dstIdxReg + "*" + scale + "]");
    }

    /**
     * Create a inc [disp]
     *
     * @param dstDisp
     */
    public void writeINC(int operandSize, int dstDisp) {
        println("\tinc " + size(operandSize) + "[" + dstDisp + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeINT(int)
     */
    public void writeINT(int vector) {
        println("\tint 0x" + NumberUtils.hex(vector, 2));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeIRET()
     */
    public void writeIRET() {
        println("\tiret");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeJCC(Label, int)
     */
    public void writeJCC(Label label, int jumpOpcode) {
        println("\tj" + ccName(jumpOpcode) + " " + label(label));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeJECXZ(Label)
     */
    public void writeJECXZ(Label label) {
        println("\tjecxz" + label(label));
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
     * @param rawAddress If true, tablePtr is a raw address
     */
    public void writeJMP(Object tablePtr, int offset, boolean rawAddress) {
        if (tablePtr == null)
            tablePtr = "null"; // workaround for a peculiar NPE in StringBuffer
        println("\tjmp [ left out in: TextX86Stream.writeJMP(Object tablePtr, int offset, boolean rawAddress)]");
    }

    /**
     * @param operandSize
     * @param seg
     * @param disp
     */
    public void writeJMP(int operandSize, int seg, int disp) {
        println("\tjmp " + size(operandSize) + " 0x" + NumberUtils.hex(seg) + ":0x" + NumberUtils.hex(disp));
    }

    /**
     * Create a absolute jump to address stored at the given offset (in
     * register) in the given table pointer.
     *
     * @param tablePtr
     * @param offsetReg
     */
    public void writeJMP(Object tablePtr, GPR offsetReg) {
        println("\tjmp [" + tablePtr + "+" + offsetReg + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeJMP(GPR)
     */
    public void writeJMP(GPR reg32) {
        println("\tjmp " + reg32);
    }

    /**
     * Create a absolute jump to [reg32+disp]
     *
     * @param reg32
     */
    public final void writeJMP(GPR reg32, int disp) {
        println("\tjmp [" + reg32 + disp(disp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeLDMXCSR(GPR, int)
     */
    public void writeLDMXCSR(GPR srcReg, int disp) {
        println("\tldmxcsr dword [" + srcReg + disp(disp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeLEA(GPR, GPR, int)
     */
    public void writeLEA(GPR dstReg, GPR srcReg, int disp) {
        println("\tlea " + dstReg + ",[" + srcReg + disp(disp) + "]");
    }

    public void writeLEA(X86Register.GPR dstReg, X86Register.GPR srcIdxReg, int scale, int disp) {
        println("\tlea " + dstReg + ",[" + srcIdxReg + "*" + scale + disp(disp) + "]");
    }

    public void writeLGDT(int disp) {
        println("\tlgdt [" + disp(disp) + "]");
    }

    public void writeLIDT(int disp) {
        println("\tlidt [" + disp(disp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeLEA(GPR, GPR, GPR, int,
     *      int)
     */
    public void writeLEA(GPR dstReg, GPR srcReg, GPR srcIdxReg, int scale,
                         int disp) {
        println("\tlea " + dstReg + ",[" + srcReg + disp(disp) + "+"
            + srcIdxReg + "*" + scale + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeLMSW(GPR)
     */
    public void writeLMSW(GPR srcReg) {
        println("\tlmsw " + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeLODSD()
     */
    public void writeLODSD() {

        println("\tlodsd");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeLODSW()
     */
    public void writeLODSW() {

        println("\tlodsw");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeLOOP(Label)
     */
    public void writeLOOP(Label label) throws UnresolvedObjectRefException {

        println("\tloop " + label(label));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeLTR(GPR)
     */
    public void writeLTR(GPR srcReg) {
        println("\tltr " + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOV(int, GPR, int, GPR)
     */
    public void writeMOV(int operandSize, GPR dstReg, int dstDisp, GPR srcReg) {
        println("\tmov " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "]," + srcReg);
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     */
    public void writeMOV(X86Register.SR dstReg, int dstDisp, X86Register.GPR srcReg) {
        println("\tmov " + "[" + dstReg + ":0x" + NumberUtils.hex(dstDisp) + "]," + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOV(CRX, GPR)
     */
    public void writeMOV(CRX dstReg, GPR srcReg) {
        println("\tmov " + dstReg + "," + size(BITS32) + " " + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOV(GPR, CRX)
     */
    public void writeMOV(GPR dstReg, CRX srcReg) {
        println("\tmov " + dstReg + "," + size(BITS32) + " " + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOV(CRX, GPR)
     */
    public void writeMOV(SR dstReg, GPR srcReg) {
        println("\tmov " + dstReg + "," + size(BITS16) + " " + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOV(GPR, CRX)
     */
    public void writeMOV(GPR dstReg, SR srcReg) {
        println("\tmov " + dstReg + "," + size(BITS16) + " " + srcReg);
    }


    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOV(int, GPR, GPR)
     */
    public void writeMOV(int operandSize, GPR dstReg, GPR srcReg) {
        println("\tmov " + dstReg + "," + size(operandSize) + " " + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOV(int, GPR, GPR, int)
     */
    public void writeMOV(int operandSize, GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tmov " + dstReg + "," + size(operandSize) + "[" + srcReg
            + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOV(int, GPR, GPR, int,
     *      int, GPR)
     */
    public void writeMOV(int operandSize, GPR dstReg, GPR dstIdxReg, int scale,
                         int dstDisp, GPR srcReg) {
        println("\tmov " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "+" + dstIdxReg + "*" + scale + "]," + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOV(int, GPR, GPR, GPR,
     *      int, int)
     */
    public void writeMOV(int operandSize, GPR dstReg, GPR srcReg,
                         GPR srcIdxReg, int scale, int srcDisp) {
        println("\tmov " + dstReg + "," + size(operandSize) + "[" + srcReg
            + disp(srcDisp) + "+" + srcIdxReg + "*" + scale + "]");
    }

    /**
     * Create a mov dstReg, [srcReg:srcDisp]
     *
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeMOV(X86Register.GPR dstReg, X86Register.SR srcReg, int srcDisp) {
        println("\tmov " + dstReg + ",[" + srcReg + ":0x" + NumberUtils.hex(srcDisp) + "]");
    }

    public void writeMOV(GPR dstReg, int srcDisp) {
        println("\tmov " + dstReg + ",[" + disp(srcDisp) + "]");
    }

    public void writeMOV(int dstDisp, X86Register.GPR srcReg) {
        println("\tmov [" + disp(dstDisp) + "]," + srcReg);
    }

    public void writeMOV_Const(int operandSize, int dstDisp, int imm32) {
        println("\tmov " + size(operandSize) + "[" + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOV_Const(GPR, int)
     */
    public void writeMOV_Const(GPR dstReg, int imm32) {
        if (dstReg.getSize() == BITS32) {
            println("\tmov " + dstReg + ",0x" + NumberUtils.hex(imm32));
        } else {
            println("\tmov " + dstReg + ",0x" + NumberUtils.hex((long) imm32));
        }
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOV_Const(GPR, int)
     */
    public void writeMOV_Const(GPR dstReg, long imm64) {
        println("\tmov " + dstReg + ",0x" + NumberUtils.hex(imm64));
    }

    /**
     * Create a mov [dstReg:dstDisp], <imm32>
     *
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeMOV_Const(int operandSize, X86Register.SR dstReg, int dstDisp, int imm32) {
        println("\tmov " + size(operandSize) + "[" + dstReg + ":0x" + NumberUtils.hex(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * Create a mov [destReg+destDisp], <imm32>
     *
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeMOV_Const(int operandSize, GPR dstReg, int dstDisp,
                               int imm32) {
        println("\tmov " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOV_Const(GPR,
     *      java.lang.Object)
     */
    public void writeMOV_Const(GPR dstReg, Object label) {
        println("\tmov " + dstReg + "," + label);
    }

    /**
     * Create a mov [destReg+dstIdxReg*scale+destDisp], <imm32>
     *
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeMOV_Const(int operandSize, GPR dstReg, GPR dstIdxReg,
                               int scale, int dstDisp, int imm32) {
        println("\tmov " + size(operandSize) + "[" + dstReg + "+" + dstIdxReg
            + "*" + scale + disp(dstDisp) + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOVD(int, org.jnode.assembler.x86.X86Register.MMX , GPR, int)
     */
    public void writeMOVD(int operandSize, X86Register.MMX mmx, X86Register.GPR reg, int disp) {
        println("\tmovd " + mmx + "," + size(operandSize) + "[" + reg + disp(disp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOVD(int, GPR, int, org.jnode.assembler.x86.X86Register.MMX)
     */
    public void writeMOVD(int operandSize, X86Register.GPR dstReg, int dstDisp, X86Register.MMX srcMmx) {
        println("\tmovd " + size(operandSize) + "[" + dstReg + disp(dstDisp) + "]," + srcMmx);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOVQ(org.jnode.assembler.x86.X86Register.MMX ,
     * org.jnode.assembler.x86.X86Register.MMX)
     */
    public void writeMOVQ(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tmovd " + dstMmx + "," + srcMmx);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOVQ(int, org.jnode.assembler.x86.X86Register.MMX , GPR, int)
     */
    public void writeMOVQ(int operandSize, X86Register.MMX dstMmx, X86Register.GPR srcGpr, int srcDisp) {
        println("\tmovq " + dstMmx + "," + size(operandSize) + "[" + srcGpr + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOVQ(int, org.jnode.assembler.x86.X86Register.MMX , int)
     */
    public void writeMOVQ(int operandSize, X86Register.MMX dstMmx, int srcDisp) {
        println("\tmovq " + dstMmx + "," + size(operandSize) + "[" + disp(srcDisp) + "]");
    }

    public void writeMOVSB() {
        println("\tmovsb");
    }

    public void writeMOVSD() {
        println("\tmovsd");
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
     * @see org.jnode.assembler.x86.X86Assembler#writeMOVSX(GPR, GPR, int)
     */
    public void writeMOVSX(GPR dstReg, GPR srcReg, int srcSize) {
        println("\tmovsx " + dstReg + "," + size(srcSize) + " " + srcReg);
    }

    public void writeMOVSX(GPR dstReg, GPR srcReg, int srcDisp, int srcSize) {
        println("\tmovsx " + dstReg + "," + size(srcSize) + " " + "[" + srcReg
            + disp(srcDisp) + "]");
    }

    /**
     * Create a movsxd dstReg, srcReg. Sign extends the srcReg to dstReg. Only
     * valid in 64-bit mode.
     *
     * @param dstReg
     * @param srcReg
     */
    public void writeMOVSXD(GPR64 dstReg, GPR32 srcReg)
        throws InvalidOpcodeException {
        if (!code64) {
            throw new InvalidOpcodeException();
        }
        println("\tmovsxd " + dstReg + "," + srcReg);
    }

    public void writeMOVSW() {
        println("\tmovsw");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMOVZX(GPR, GPR, int)
     */
    public void writeMOVZX(GPR dstReg, GPR srcReg, int srcSize) {
        println("\tmovzx " + dstReg + "," + size(srcSize) + " " + srcReg);
    }

    public void writeMOVZX(GPR dstReg, GPR srcReg, int srcDisp, int srcSize) {
        println("\tmovzx " + dstReg + "," + size(srcSize) + " " + "[" + srcReg
            + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeMUL_EAX(GPR)
     */
    public void writeMUL_EAX(GPR srcReg) {
        println("\tmul " + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeNEG(GPR)
     */
    public void writeNEG(GPR dstReg) {
        println("\tneg " + dstReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeNEG(int, GPR, int)
     */
    public void writeNEG(int operandSize, GPR dstReg, int dstDisp) {
        println("\tneg " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeNOP()
     */
    public void writeNOP() {
        println("\tnop");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeNOT(GPR)
     */
    public void writeNOT(GPR dstReg) {
        println("\tnot " + dstReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeNOT(int, GPR, int)
     */
    public void writeNOT(int operandSize, GPR dstReg, int dstDisp) {
        println("\tnot " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "]");
    }

    // LS
    /**
     * @param dstReg
     * @param imm32
     */
    public void writeOR(GPR dstReg, int imm32) {
        println("\tor " + dstReg + ",0x" + NumberUtils.hex(imm32));
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeOR(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        println("\tor " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeOR(GPR, int, GPR)
     */
    public void writeOR(GPR dstReg, int dstDisp, GPR srcReg) {
        println("\tor [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    /**
     * @param operandSize
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeOR(int operandSize, X86Register.SR dstReg, int dstDisp, int imm32) {
        println("\tor " + size(operandSize) + "[" + dstReg + ":0x" + NumberUtils.hex(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @param operandSize
     * @param dstDisp
     * @param imm32
     */
    public void writeOR(int operandSize, int dstDisp, int imm32) {
        println("\tor " + size(operandSize) + "[" + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeOR(GPR, GPR)
     */
    public void writeOR(GPR dstReg, GPR srcReg) {
        println("\tor " + dstReg + "," + srcReg);
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeOR(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tor " + dstReg + ",[" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeOUT(int)
     */
    public void writeOUT(int operandSize) {
        if (operandSize == X86Constants.BITS8) {
            println("\tout " + X86Register.DX + "," + X86Register.AL);
        } else if (operandSize == X86Constants.BITS16) {
            println("\tout " + X86Register.DX + "," + X86Register.AX);
        } else if (operandSize == X86Constants.BITS32) {
            println("\tout " + X86Register.DX + "," + X86Register.EAX);
        } else {
            throw new IllegalArgumentException("Invalid operand size for OUT: " + operandSize);
        }
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeOUT(int, int)
     */
    public void writeOUT(int operandSize, int imm8) {
        if (operandSize == X86Constants.BITS8) {
            println("\tout " + imm8 + "," + X86Register.AL);
        } else if (operandSize == X86Constants.BITS16) {
            println("\tout " + imm8 + "," + X86Register.AX);
        } else if (operandSize == X86Constants.BITS32) {
            println("\tout " + imm8 + "," + X86Register.EAX);
        } else {
            throw new IllegalArgumentException("Invalid operand size for OUT: " + operandSize);
        }
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePACKUSWB(org.jnode.assembler.x86.X86Register.MMX ,
     * org.jnode.assembler.x86.X86Register.MMX)
     */
    public void writePACKUSWB(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpackuswb " + dstMmx + "," + srcMmx);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePADDW(org.jnode.assembler.x86.X86Register.MMX ,
     * org.jnode.assembler.x86.X86Register.MMX)
     */
    public void writePADDW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpaddw " + dstMmx + "," + srcMmx);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePAND(org.jnode.assembler.x86.X86Register.MMX ,
     * org.jnode.assembler.x86.X86Register.MMX)
     */
    public void writePAND(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpand " + dstMmx + "," + srcMmx);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePCMPGTW(org.jnode.assembler.x86.X86Register.MMX ,
     * org.jnode.assembler.x86.X86Register.MMX)
     */
    public void writePCMPGTW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpcmpgtw " + dstMmx + "," + srcMmx);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePMULLW(org.jnode.assembler.x86.X86Register.MMX ,
     * org.jnode.assembler.x86.X86Register.MMX)
     */
    public void writePMULLW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpmullw " + dstMmx + "," + srcMmx);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePOP(GPR)
     */
    public void writePOP(GPR dstReg) {
        println("\tpop " + dstReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePOP(SR)
     */
    public void writePOP(SR dstReg) {
        println("\tpop " + dstReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePOP(GPR, int)
     */
    public void writePOP(GPR dstReg, int dstDisp) {
        println("\tpop [" + dstReg + disp(dstDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePOPA()
     */
    public void writePOPA() {
        println("\tpopa");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePOPF()
     */
    public void writePOPF() {
        println("\tpopf");
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
            case LOCK_PREFIX:
                str = "lock";
                break;
            case REP_PREFIX:
                str = "rep";
                break;
            default:
                throw new IllegalArgumentException("Unknown prefix " + prefix);
        }
        println("\tprefix " + str);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePSHUFW(org.jnode.assembler.x86.X86Register.MMX ,
     *  org.jnode.assembler.x86.X86Register.MMX ,int)
     */
    public void writePSHUFW(X86Register.MMX dstMmx, X86Register.MMX srcMmx, int imm8) {
        println("\tpshufw " + dstMmx + "," + srcMmx + ",0x" + NumberUtils.hex(imm8));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePSRLW(org.jnode.assembler.x86.X86Register.MMX ,int)
     */
    public void writePSRLW(X86Register.MMX mmx, int imm8) {
        println("\tpsrlw " + mmx + ",0x" + NumberUtils.hex(imm8));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePSUBW(org.jnode.assembler.x86.X86Register.MMX ,
     *  org.jnode.assembler.x86.X86Register.MMX)
     */
    public void writePSUBW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpsubw " + dstMmx + "," + srcMmx);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePUNPCKLBW(org.jnode.assembler.x86.X86Register.MMX ,
     *  org.jnode.assembler.x86.X86Register.MMX)
     */
    public void writePUNPCKLBW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpsunpcklbw " + dstMmx + "," + srcMmx);
    }

    /**
     * @return The ofset of the start of the instruction.
     * @see org.jnode.assembler.x86.X86Assembler#writePUSH(int)
     */
    public int writePUSH(int imm32) {
        return println("\tpush 0x" + NumberUtils.hex(imm32));
    }

    /**
     * @return The ofset of the start of the instruction.
     * @see org.jnode.assembler.x86.X86Assembler#writePUSH(GPR)
     */
    public int writePUSH(GPR srcReg) {
        return println("\tpush " + srcReg);
    }

    /**
     * @return The ofset of the start of the instruction.
     * @see org.jnode.assembler.x86.X86Assembler#writePUSH(SR)
     */
    public int writePUSH(SR srcReg) {
        return println("\tpush " + srcReg);
    }

    /**
     * @return The ofset of the start of the instruction.
     * @see org.jnode.assembler.x86.X86Assembler#writePUSH(GPR, int)
     */
    public int writePUSH(GPR srcReg, int srcDisp) {
        return println("\tpush [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @return The ofset of the start of the instruction.
     * @see org.jnode.assembler.x86.X86Assembler#writePUSH(GPR, int)
     */
    public int writePUSH(SR srcReg, int srcDisp) {
        return println("\tpush [" + srcReg + ":" + srcDisp + "]");
    }

    /**
     * @return The ofset of the start of the instruction.
     * @see org.jnode.assembler.x86.X86Assembler#writePUSH(GPR, GPR, int, int)
     */
    public int writePUSH(GPR srcBaseReg, GPR srcIndexReg, int srcScale,
                         int srcDisp) {
        return println("\tpush [" + srcBaseReg + disp(srcDisp) + "+"
            + srcIndexReg + "*" + srcScale + "]");
    }

    // PR
    /**
     * @return The offset of the start of the instruction.
     * @see org.jnode.assembler.x86.X86Assembler#writePUSH_Const(Object)
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

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePUSHF()
     */
    public void writePUSHF() {
        println("\tpushf");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writePXOR(org.jnode.assembler.x86.X86Register.MMX ,
     *  org.jnode.assembler.x86.X86Register.MMX)
     */
    public void writePXOR(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpxor " + dstMmx + "," + srcMmx);
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
     * @see org.jnode.assembler.x86.X86Assembler#writeSAL(GPR, int)
     */
    public void writeSAL(GPR dstReg, int imm8) {
        println("\tsal " + dstReg + "," + imm8);
    }

    /**
     * @param srcReg
     * @param srcDisp
     * @param imm8
     */
    public void writeSAL(int operandSize, GPR srcReg, int srcDisp, int imm8) {
        println("\tsal " + size(operandSize) + "[" + srcReg + disp(srcDisp)
            + "]," + imm8);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSAL_CL(GPR)
     */
    public void writeSAL_CL(GPR dstReg) {
        println("\tsal " + dstReg + ",cl");
    }

    /**
     * @param srcReg
     * @param srcDisp
     */
    public void writeSAL_CL(int operandSize, GPR srcReg, int srcDisp) {
        println("\tsal " + size(operandSize) + "[" + srcReg + disp(srcDisp)
            + "],cl");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSAR(GPR, int)
     */
    public void writeSAR(GPR dstReg, int imm8) {
        println("\tsar " + dstReg + "," + imm8);
    }

    /**
     * @param srcReg
     * @param srcDisp
     * @param imm8
     */
    public void writeSAR(int operandSize, GPR srcReg, int srcDisp, int imm8) {
        println("\tsar " + size(operandSize) + "[" + srcReg + disp(srcDisp)
            + "]," + imm8);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSAR_CL(GPR)
     */
    public void writeSAR_CL(GPR dstReg) {
        println("\tsar " + dstReg + ",cl");
    }

    /**
     * @param srcReg
     * @param srcDisp
     */
    public void writeSAR_CL(int operandSize, GPR srcReg, int srcDisp) {
        println("\tsar " + size(operandSize) + "[" + srcReg + disp(srcDisp)
            + "],cl");
    }

    /**
     * Create a SBB dstReg, imm32
     *
     * @param dstReg
     * @param imm32
     */
    public void writeSBB(GPR dstReg, int imm32) {
        println("\tsbb " + dstReg + ",0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSBB(int, GPR, int, int)
     */
    public void writeSBB(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        println("\tsbb " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSBB(GPR, int, GPR)
     */
    public void writeSBB(GPR dstReg, int dstDisp, GPR srcReg) {
        println("\tsbb [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSBB(GPR, GPR)
     */
    public void writeSBB(GPR dstReg, GPR srcReg) {
        println("\tsbb " + dstReg + "," + srcReg);
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeSBB(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tsbb " + dstReg + ",[" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * Create a SETcc dstReg
     *
     * @param dstReg
     * @param cc
     */
    public void writeSETCC(GPR dstReg, int cc) {
        println("\tset" + ccName(cc) + " " + dstReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSHL(GPR, int)
     */
    public void writeSHL(GPR dstReg, int imm8) {
        println("\tshl " + dstReg + "," + imm8);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSHL(int, GPR, int, int)
     */
    public void writeSHL(int operandSize, GPR dstReg, int dstDisp, int imm8) {
        println("\tshl " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "]," + imm8);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSHL_CL(GPR)
     */
    public void writeSHL_CL(GPR dstReg) {
        println("\tshl " + dstReg + ",cl");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSHL_CL(int, GPR, int)
     */
    public void writeSHL_CL(int operandSize, GPR dstReg, int dstDisp) {
        println("\tshl " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "],CL");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSHLD_CL(GPR, GPR)
     */
    public void writeSHLD_CL(GPR dstReg, GPR srcReg) {
        println("\tshld " + dstReg + "," + srcReg + ",cl");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSHR(GPR, int)
     */
    public void writeSHR(GPR dstReg, int imm8) {
        println("\tshr " + dstReg + "," + imm8);
    }

    /**
     * @param srcReg
     * @param srcDisp
     * @param imm8
     */
    public void writeSHR(int operandSize, GPR srcReg, int srcDisp, int imm8) {
        println("\tshr " + size(operandSize) + "[" + srcReg + disp(srcDisp)
            + "]," + imm8);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSHR_CL(GPR)
     */
    public void writeSHR_CL(GPR dstReg) {
        println("\tshr " + dstReg + ",cl");
    }

    /**
     * @param srcReg
     * @param srcDisp
     */
    public void writeSHR_CL(int operandSize, GPR srcReg, int srcDisp) {
        println("\tshr " + size(operandSize) + "[" + srcReg + disp(srcDisp)
            + "],cl");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSHRD_CL(GPR, GPR)
     */
    public void writeSHRD_CL(GPR dstReg, GPR srcReg) {
        println("\tshrd " + dstReg + "," + srcReg + ",cl");
    }

    /**
     *
     */
    public void writeSTD() {
        println("\tstd");
    }

    /**
     *
     */
    public void writeSTI() {
        println("\tsti");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSTMXCSR(GPR, int)
     */
    public void writeSTMXCSR(GPR srcReg, int disp) {
        println("\tstmxcsr dword [" + srcReg + disp(disp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSTOSB()
     */
    public void writeSTOSB() {
        println("\tstosb");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSTOSD()
     */
    public void writeSTOSD() {
        println("\tstosd");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSTOSW()
     */
    public void writeSTOSW() {
        println("\tstosw");
    }

    /**
     * Create a SUB reg, imm32
     *
     * @param reg
     * @param imm32
     */
    public final void writeSUB(GPR reg, int imm32) {
        println("\tsub " + reg + "," + imm32);
    }

    /**
     * @param dstDisp
     * @param srcReg
     */
    public void writeSUB(int dstDisp, GPR srcReg) {
        println("\tsub [" + disp(dstDisp) + "]," + srcReg);
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeSUB(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        println("\tsub " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSUB(GPR, int, GPR)
     */
    public void writeSUB(GPR dstReg, int dstDisp, GPR srcReg) {
        println("\tsub [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeSUB(GPR, GPR)
     */
    public void writeSUB(GPR dstReg, GPR srcReg) {
        println("\tsub " + dstReg + "," + srcReg);
    }


    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeSUB(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tsub " + dstReg + ", [" + srcReg + disp(srcDisp) + "]");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeTEST(GPR, int)
     */
    public void writeTEST(GPR reg, int imm32) {
        println("\ttest " + reg + ",0x" + NumberUtils.hex(imm32));
    }

    /**
     * Create a TEST [reg+disp], imm32
     *
     * @param reg
     * @param disp
     * @param imm32
     */
    public void writeTEST(int operandSize, GPR reg, int disp, int imm32) {
        println("\ttest " + size(operandSize) + "[" + reg + disp(disp) + "],0x"
            + NumberUtils.hex(imm32));
    }

    /**
     * @param operandSize
     * @param reg
     * @param disp
     * @param imm32
     */
    public void writeTEST(int operandSize, SR reg, int disp, int imm32) {
        println("\ttest " + size(operandSize) + "[" + reg + ":0x"
            + NumberUtils.hex(disp) + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * Create a TEST reg1, reg2
     *
     * @param reg1
     * @param reg2
     */
    public void writeTEST(GPR reg1, GPR reg2) {
        println("\ttest " + reg1 + "," + reg2);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeTEST_AL(int)
     */
    public void writeTEST_AL(int value) {
        println("\ttest al," + value);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeTEST_EAX(int, int)
     */
    public void writeTEST_EAX(int operandSize, int value) {
        testOperandSize(operandSize, BITS32 | BITS64);
        if (operandSize == BITS32) {
            println("\ttest eax," + value);
        } else if (operandSize == BITS64) {
            println("\ttest rax," + value);
        }
    }

    public void writeTEST(int operandSize, int destDisp, int imm32) {
        println("\ttest " + size(operandSize) + "[" + disp(destDisp) + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.NativeStream#writeTo(java.io.OutputStream)
     */
    public void writeTo(OutputStream os) throws IOException {

    }

    /**
     * @param dstDisp
     * @param srcReg
     */
    public void writeXCHG(int dstDisp, X86Register.GPR srcReg) {
        println("\txchg [" + disp(dstDisp) + "], " + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeXCHG(GPR, int, GPR)
     */
    public void writeXCHG(GPR dstReg, int dstDisp, GPR srcReg) {
        println("\txchg [" + dstReg + disp(dstDisp) + "], " + srcReg);
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     */
    public void writeXCHG(X86Register.SR dstReg, int dstDisp, X86Register.GPR srcReg) {
        println("\txchg [" + dstReg + ":0x" + NumberUtils.hex(dstDisp) + "], " + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeXCHG(GPR, GPR)
     */
    public void writeXCHG(GPR dstReg, GPR srcReg) {
        println("\txchg " + dstReg + ", " + srcReg);
    }

    /**
     * @param dstReg
     * @param imm32
     */
    public void writeXOR(GPR dstReg, int imm32) {
        println("\txor " + dstReg + ",0x" + NumberUtils.hex(imm32));
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeXOR(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        println("\txor " + size(operandSize) + "[" + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeXOR(GPR, int, GPR)
     */
    public void writeXOR(GPR dstReg, int dstDisp, GPR srcReg) {
        println("\txor [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeXOR(GPR, GPR)
     */
    public void writeXOR(GPR dstReg, GPR srcReg) {
        println("\txor " + dstReg + "," + srcReg);
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeXOR(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\txor " + dstReg + ", [" + srcReg + disp(srcDisp) + "]");
    }

    public void writeObjectRef(Object object) {

    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeRDMSR()
     */
    public void writeRDMSR() {
        println("\trdmsr");
    }

    /**
     * @see org.jnode.assembler.x86.X86Assembler#writeWRMSR()
     */
    public void writeWRMSR() {
        println("\twrmsr");
    }
}
