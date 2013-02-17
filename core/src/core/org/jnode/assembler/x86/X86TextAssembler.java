/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

        public void markEnd() {
            println(";\n; -- End of Object --\n;");
        }

    }

    static class ObjectRefImpl extends NativeStream.ObjectRef {

        public ObjectRefImpl(Object object) {
            super(object);
        }

        public int getOffset() throws UnresolvedObjectRefException {
            return 0;
        }

        public boolean isResolved() {
            return true;
        }

        public void addUnresolvedLink(int offset, int patchSize) {
            // TODO Auto-generated method stub

        }

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

    public int get32(int offset) {
        return 0;
    }

    public int get8(int offset) {
        return 0;
    }

    public long getBaseAddr() {
        return 0;
    }

    public byte[] getBytes() {
        return dummy;
    }

    public int getLength() {
        return idx + buf.length();
    }

    public ObjectRef getObjectRef(Object keyObj) {
        return new ObjectRefImpl(keyObj);
    }

    public Collection<? extends ObjectRef> getObjectRefs() {
        return null;
    }

    public ObjectResolver getResolver() {
        return null;
    }

    public Collection<?> getUnresolvedObjectRefs() {
        return null;
    }

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

    public void set64(int offset, long v64) {
    }

    public void set32(int offset, int v32) {
    }

    public void set16(int offset, int v16) {
    }

    public void set8(int offset, int v8) {
    }
     
    public ObjectRef setObjectRef(Object label) {
        println(label(label) + ':');
        return new ObjectRefImpl(label);
    }

    public void setResolver(ObjectResolver resolver) {
    }

    public ObjectInfo startObject(VmType<?> cls) {
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

    public void write16(int v16) {
        println("\tdw " + v16);
    }

    public void write32(int v32) {
        println("\tdd " + v32);
    }

    public void write64(long v64) {
        println("\tdq " + v64);
    }

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
        println("\tadc " + dstReg + ',' + imm32);
    }

    public void writeADC(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        println("\tadc " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + "]," + imm32);
    }

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
        println("\tadc " + dstReg + ',' + srcReg);
    }

    /**
     * Create a ADC dstReg, [srcReg+srcDisp]
     *
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeADC(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tadc " + dstReg + ",[" + srcReg + disp(srcDisp) + ']');
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
        println("\tadd " + size(operandSize) + '[' + disp(dstDisp)
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
        println("\tadd " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    public void writeADD(int operandSize, SR dstReg, int dstDisp, int imm32) {
        println("\tadd " + size(operandSize) + '[' + dstReg + ":0x"
            + NumberUtils.hex(dstDisp) + "],0x" + NumberUtils.hex(imm32));
    }

    public void writeADD_MEM(X86Register.GPR reg, int memPtr32) {
        println("\tadd " + reg + ",[0x" + NumberUtils.hex(memPtr32) + ']');
    }

    public void writeADD(GPR dstReg, int dstDisp, GPR srcReg) {
        println("\tadd [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    public void writeADD(GPR dstReg, GPR srcReg) {
        println("\tadd " + dstReg + ',' + srcReg);
    }

    /**
     * Create a ADD dstReg, [srcReg+srcDisp]
     *
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeADD(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tadd " + dstReg + ",[" + srcReg + disp(srcDisp) + ']');
    }

    public void writeAND(GPR reg, int imm32) {
        println("\tand " + reg + ",0x" + NumberUtils.hex(imm32));
    }

    public void writeAND(int operandSize, int dstDisp, int imm32) {
        println("\tand " + size(operandSize) + '[' + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param imm32
     */
    public void writeAND(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        println("\tand " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

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
        println("\tand " + size(operandSize) + '[' + dstReg + ":0x" + NumberUtils.hex(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    public void writeAND(GPR dstReg, GPR srcReg) {
        println("\tand " + dstReg + ',' + srcReg);
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeAND(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tand " + dstReg + ",[" + srcReg + disp(srcDisp) + ']');
    }

    public void writeArithSSEDOp(int operation, XMM dst, GPR src, int srcDisp) {
        final String op = getSSEOperationName(operation);
        println('\t' + op + 'D' + dst + ", qword [" + src + disp(srcDisp) + ']');
    }

    public void writeArithSSEDOp(int operation, XMM dst, XMM src) {
        final String op = getSSEOperationName(operation);
        println('\t' + op + 'D' + dst + ", " + src);
    }

    public void writeArithSSESOp(int operation, XMM dst, GPR src, int srcDisp) {
        final String op = getSSEOperationName(operation);
        println('\t' + op + 'S' + dst + ", dword [" + src + disp(srcDisp) + ']');
    }

    public void writeArithSSESOp(int operation, XMM dst, XMM src) {
        final String op = getSSEOperationName(operation);
        println('\t' + op + 'S' + dst + ", " + src);
    }

    public void writeBOUND(GPR lReg, GPR rReg, int rDisp) {
        println("\tbound " + lReg + ",[" + rReg + disp(rDisp) + ']');
    }

    public void writeBreakPoint() {
        println("\tint 3");
    }

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
        println("\tcall [" + tablePtr + disp(offset) + ']');
    }

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
        println("\tcall [" + reg + disp(offset) + ']');
    }

    public void writeCALL(GPR regBase, GPR regIndex, int scale, int disp) {
        println("\tcall [" + regBase + '+' + regIndex + '*' + scale
            + disp(disp) + ']');
    }

    public void writeCALL(GPR regIndex, int scale, int disp) {
        println("\tcall [" + regIndex + '*' + scale + disp(disp) + ']');
    }

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

    public void writeCLD() {
        println("\tCLD");
    }

    public void writeCLI() {
        println("\tCLI");
    }

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
        println("\tCMOV" + ccName(ccOpcode) + ' ' + dst + ',' + src);
    }

    /**
     * Create a CMOVcc dst,[src+srcDisp]
     *
     * @param dst
     * @param src
     * @param srcDisp
     */
    public void writeCMOVcc(int ccOpcode, GPR dst, GPR src, int srcDisp) {
        println("\tCMOV" + ccName(ccOpcode) + ' ' + dst + ",[" + src
            + disp(srcDisp) + ']');
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

    public void writeCMP(GPR reg1, GPR reg2) {
        println("\tcmp " + reg1 + ',' + reg2);
    }

    /**
     * Create a CMP reg1, [reg2:disp]
     *
     * @param reg1
     * @param reg2
     * @param disp
     */
    public void writeCMP(GPR reg1, SR reg2, int disp) {
        println("\tcmp " + reg1 + ",[" + reg2 + ":0x" + NumberUtils.hex(disp) + ']');
    }

    /**
     * Create a CMP reg1, [reg2+disp]
     *
     * @param reg1
     * @param reg2
     * @param disp
     */
    public void writeCMP(GPR reg1, GPR reg2, int disp) {
        println("\tcmp " + reg1 + ",[" + reg2 + disp(disp) + ']');
    }

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
        println("\tcmp " + size(operandSize) + '[' + reg + disp(disp) + "],0x"
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
        println("\tcmp " + size(operandSize) + '[' + dstReg + ":0x" + NumberUtils.hex(dstDisp) + "],0x"
            + NumberUtils.hex(imm32));
    }

    public void writeCMP_EAX(int operandSize, int imm32) {
        testOperandSize(operandSize, BITS32 | BITS64);
        if (operandSize == BITS32) {
            println("\tcmp eax,0x" + NumberUtils.hex(imm32));
        } else {
            println("\tcmp rax,0x" + NumberUtils.hex(imm32));
        }
    }

    public void writeCMP_MEM(int operandSize, int memPtr, int imm32) {
        println("\tcmp " + size(operandSize) + '[' + memPtr + "],0x"
            + NumberUtils.hex(imm32));
    }

    /**
     * Create a CMP reg,[memPtr]
     *
     * @param reg
     * @param memPtr
     */
    public void writeCMP_MEM(GPR reg, int memPtr) {
        println("\tcmp " + reg + ", [" + memPtr + ']');
    }

    public void writeCMPXCHG_EAX(GPR dstReg, int dstDisp, GPR srcReg,
                                 boolean lock) {
        println("\tcmpxchg [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    public void writeCPUID() {
        println("\tcpuid");
    }

    public void writeDEC(GPR dstReg) {
        println("\tdec " + dstReg);
    }

    public void writeDEC(int operandSize, GPR dstReg, int dstDisp) {
        println("\tdec " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + ']');
    }

    public void writeDIV_EAX(GPR srcReg) {

        println("\tdiv " + srcReg);
    }

    public void writeEMMS() {
        println("\temms");
    }

    public void writeFADD32(GPR srcReg, int srcDisp) {
        println("\tfadd dword [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFADD64(GPR srcReg, int srcDisp) {
        println("\tfadd qword [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFADDP(X86Register fpuReg) {
        println("\tfaddp " + fpuReg);
    }

    public void writeFCHS() {
        println("\tfchs");
    }

    public void writeFDIV32(GPR srcReg, int srcDisp) {
        println("\tfdiv dword [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFDIV64(GPR srcReg, int srcDisp) {
        println("\tfdiv qword [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFDIVP(X86Register fpuReg) {
        println("\tfdivp " + fpuReg);
    }

    public void writeFFREE(X86Register fReg) {
        println("\tffree " + fReg);
    }

    public void writeFILD32(GPR dstReg, int dstDisp) {
        println("\tfild dword [" + dstReg + disp(dstDisp) + ']');
    }

    public void writeFILD64(GPR dstReg, int dstDisp) {
        println("\tfild qword [" + dstReg + disp(dstDisp) + ']');
    }

    public void writeFISTP32(GPR dstReg, int dstDisp) {
        println("\tfistp dword [" + dstReg + disp(dstDisp) + ']');
    }

    public void writeFISTP64(GPR dstReg, int dstDisp) {
        println("\tfistp qword [" + dstReg + disp(dstDisp) + ']');
    }

    public void writeFLD32(GPR srcReg, int srcDisp) {
        println("\tfld dword [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFLD32(GPR srcBaseReg, GPR srcIndexReg, int srcScale,
                           int srcDisp) {
        println("\tfld dword [" + srcBaseReg + '+' + srcIndexReg + '*'
            + srcScale + disp(srcDisp) + ']');
    }

    public void writeFLD64(GPR srcReg, int srcDisp) {
        println("\tfld qword [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFLD64(GPR srcBaseReg, GPR srcIndexReg, int srcScale,
                           int srcDisp) {
        println("\tfld qword [" + srcBaseReg + '+' + srcIndexReg + '*'
            + srcScale + disp(srcDisp) + ']');
    }

    public void writeFLDCW(GPR srcReg, int srcDisp) {
        println("\tfldcw word [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFMUL32(GPR srcReg, int srcDisp) {
        println("\tfmul dword [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFMUL64(GPR srcReg, int srcDisp) {
        println("\tfmul qword [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFMULP(X86Register fpuReg) {
        println("\tfmulp " + fpuReg);
    }

    public void writeFNINIT() {
        println("\tfninit");
    }

    public void writeFNSAVE(GPR srcReg, int srcDisp) {
        println("\tfnsave [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFNSTSW_AX() {
        println("\tfnstsw_ax");
    }

    public void writeFPREM() {
        println("\tfprem");
    }

    public void writeFRSTOR(GPR srcReg, int srcDisp) {
        println("\tfrstor [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFSTCW(GPR srcReg, int srcDisp) {
        println("\tfstcw word [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFSTP(X86Register fpuReg) {
        println("\tfstp " + fpuReg);
    }

    public void writeFSTP32(GPR dstReg, int dstDisp) {
        println("\tfstp dword [" + dstReg + disp(dstDisp) + ']');
    }

    public void writeFSTP64(GPR dstReg, int dstDisp) {
        println("\tfstp qword [" + dstReg + disp(dstDisp) + ']');
    }

    public void writeFSUB32(GPR srcReg, int srcDisp) {
        println("\tfsub32 dword [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFSUB64(GPR srcReg, int srcDisp) {
        println("\tfsub64 qword [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFSUBP(X86Register fpuReg) {
        println("\tfsubp " + fpuReg);
    }

    public void writeFUCOMPP() {
        println("\tfucompp");
    }

    public void writeFXCH(X86Register fpuReg) {
        println("\tfxch " + fpuReg);
    }

    public void writeFXRSTOR(GPR srcReg, int srcDisp) {
        println("\tfxrstor [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeFXSAVE(GPR srcReg, int srcDisp) {
        println("\tfxsave [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeHLT() {
        println("\thlt");
    }

    public void writeIDIV_EAX(GPR srcReg) {
        println("\tidiv " + srcReg);
    }

    public void writeIDIV_EAX(int operandSize, GPR srcReg, int srcDisp) {
        println("\tidiv " + size(operandSize) + '[' + srcReg + disp(srcDisp)
            + ']');
    }

    public void writeIMUL(GPR dstReg, GPR srcReg) {
        println("\timul " + dstReg + ',' + srcReg);
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeIMUL(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\timul " + dstReg + ",[" + srcReg + disp(srcDisp) + ']');
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param imm32
     */
    public void writeIMUL_3(GPR dstReg, GPR srcReg, int imm32) {
        println("\timul " + dstReg + ',' + srcReg + ",0x"
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

    public void writeIMUL_EAX(GPR srcReg) {
        println("\timul " + srcReg);
    }

    public void writeIN(int operandSize) {
        if (operandSize == X86Constants.BITS8) {
            println("\tin " + X86Register.AL + ',' + X86Register.DX);
        } else if (operandSize == X86Constants.BITS16) {
            println("\tin " + X86Register.AX + ',' + X86Register.DX);
        } else if (operandSize == X86Constants.BITS32) {
            println("\tin " + X86Register.EAX + ',' + X86Register.DX);
        } else {
            throw new IllegalArgumentException("Invalid operand size for IN: " + operandSize);
        }
    }

    public void writeIN(int operandSize, int imm8) {
        if (operandSize == X86Constants.BITS8) {
            println("\tin " + X86Register.AL + ',' + imm8);
        } else if (operandSize == X86Constants.BITS16) {
            println("\tin " + X86Register.AX + ',' + imm8);
        } else if (operandSize == X86Constants.BITS32) {
            println("\tin " + X86Register.EAX + ',' + imm8);
        } else {
            throw new IllegalArgumentException("Invalid operand size for IN: " + operandSize);
        }
    }

    public void writeINC(GPR dstReg) {
        println("\tinc " + dstReg);
    }

    public void writeINC(int operandSize, X86Register.SR dstReg, int disp) {
        println("\tinc " + size(operandSize) + '[' + dstReg + ":0x" + NumberUtils.hex(disp) + ']');
    }

    /**
     * Create a inc [reg32+disp]
     *
     * @param dstReg
     */
    public void writeINC(int operandSize, GPR dstReg, int disp) {
        println("\tinc " + size(operandSize) + '[' + dstReg + disp(disp) + ']');
    }

    public void writeINC(int operandSize, GPR dstReg, GPR dstIdxReg, int scale, int disp) {
        println("\tinc [" + dstReg + disp(disp) + '+' + dstIdxReg + '*' + scale + ']');
    }

    /**
     * Create a inc [disp]
     *
     * @param dstDisp
     */
    public void writeINC(int operandSize, int dstDisp) {
        println("\tinc " + size(operandSize) + '[' + dstDisp + ']');
    }

    public void writeINT(int vector) {
        println("\tint 0x" + NumberUtils.hex(vector, 2));
    }

    public void writeIRET() {
        println("\tiret");
    }

    public void writeJCC(Label label, int jumpOpcode) {
        println("\tj" + ccName(jumpOpcode) + ' ' + label(label));
    }

    public void writeJECXZ(Label label) {
        println("\tjecxz" + label(label));
    }

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
        println("\tjmp [" + tablePtr + '+' + offsetReg + ']');
    }

    public void writeJMP(GPR reg32) {
        println("\tjmp " + reg32);
    }

    /**
     * Create a absolute jump to [reg32+disp]
     *
     * @param reg32
     */
    public final void writeJMP(GPR reg32, int disp) {
        println("\tjmp [" + reg32 + disp(disp) + ']');
    }

    public void writeLDMXCSR(GPR srcReg, int disp) {
        println("\tldmxcsr dword [" + srcReg + disp(disp) + ']');
    }

    public void writeLEA(GPR dstReg, GPR srcReg, int disp) {
        println("\tlea " + dstReg + ",[" + srcReg + disp(disp) + ']');
    }

    public void writeLEA(X86Register.GPR dstReg, X86Register.GPR srcIdxReg, int scale, int disp) {
        println("\tlea " + dstReg + ",[" + srcIdxReg + '*' + scale + disp(disp) + ']');
    }

    public void writeLGDT(int disp) {
        println("\tlgdt [" + disp(disp) + ']');
    }

    public void writeLIDT(int disp) {
        println("\tlidt [" + disp(disp) + ']');
    }

    public void writeLEA(GPR dstReg, GPR srcReg, GPR srcIdxReg, int scale,
                         int disp) {
        println("\tlea " + dstReg + ",[" + srcReg + disp(disp) + '+'
            + srcIdxReg + '*' + scale + ']');
    }

    public void writeLMSW(GPR srcReg) {
        println("\tlmsw " + srcReg);
    }

    public void writeLODSD() {
        println("\tlodsd");
    }

    public void writeLODSW() {
        println("\tlodsw");
    }

    public void writeLOOP(Label label) throws UnresolvedObjectRefException {
        println("\tloop " + label(label));
    }

    public void writeLTR(GPR srcReg) {
        println("\tltr " + srcReg);
    }

    public void writeMOV(int operandSize, GPR dstReg, int dstDisp, GPR srcReg) {
        println("\tmov " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + "]," + srcReg);
    }

    /**
     * @param dstReg
     * @param dstDisp
     * @param srcReg
     */
    public void writeMOV(X86Register.SR dstReg, int dstDisp, X86Register.GPR srcReg) {
        println("\tmov " + '[' + dstReg + ":0x" + NumberUtils.hex(dstDisp) + "]," + srcReg);
    }

    public void writeMOV(CRX dstReg, GPR srcReg) {
        println("\tmov " + dstReg + ',' + size(BITS32) + ' ' + srcReg);
    }

    public void writeMOV(GPR dstReg, CRX srcReg) {
        println("\tmov " + dstReg + ',' + size(BITS32) + ' ' + srcReg);
    }

    public void writeMOV(SR dstReg, GPR srcReg) {
        println("\tmov " + dstReg + ',' + size(BITS16) + ' ' + srcReg);
    }

    public void writeMOV(GPR dstReg, SR srcReg) {
        println("\tmov " + dstReg + ',' + size(BITS16) + ' ' + srcReg);
    }

    public void writeMOV(int operandSize, GPR dstReg, GPR srcReg) {
        println("\tmov " + dstReg + ',' + size(operandSize) + ' ' + srcReg);
    }

    public void writeMOV(int operandSize, GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tmov " + dstReg + ',' + size(operandSize) + '[' + srcReg
            + disp(srcDisp) + ']');
    }

    public void writeMOV(int operandSize, GPR dstReg, GPR dstIdxReg, int scale,
                         int dstDisp, GPR srcReg) {
        println("\tmov " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + '+' + dstIdxReg + '*' + scale + "]," + srcReg);
    }

    public void writeMOV(int operandSize, GPR dstReg, GPR srcReg,
                         GPR srcIdxReg, int scale, int srcDisp) {
        println("\tmov " + dstReg + ',' + size(operandSize) + '[' + srcReg
            + disp(srcDisp) + '+' + srcIdxReg + '*' + scale + ']');
    }

    /**
     * Create a mov dstReg, [srcReg:srcDisp]
     *
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeMOV(X86Register.GPR dstReg, X86Register.SR srcReg, int srcDisp) {
        println("\tmov " + dstReg + ",[" + srcReg + ":0x" + NumberUtils.hex(srcDisp) + ']');
    }

    public void writeMOV(GPR dstReg, int srcDisp) {
        println("\tmov " + dstReg + ",[" + disp(srcDisp) + ']');
    }

    public void writeMOV(int dstDisp, X86Register.GPR srcReg) {
        println("\tmov [" + disp(dstDisp) + "]," + srcReg);
    }

    public void writeMOV_Const(int operandSize, int dstDisp, int imm32) {
        println("\tmov " + size(operandSize) + '[' + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    public void writeMOV_Const(GPR dstReg, int imm32) {
        if (dstReg.getSize() == BITS32) {
            println("\tmov " + dstReg + ",0x" + NumberUtils.hex(imm32));
        } else {
            println("\tmov " + dstReg + ",0x" + NumberUtils.hex((long) imm32));
        }
    }

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
        println("\tmov " + size(operandSize) + '[' + dstReg + ":0x" + NumberUtils.hex(dstDisp)
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
        println("\tmov " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    public void writeMOV_Const(GPR dstReg, Object label) {
        println("\tmov " + dstReg + ',' + label);
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
        println("\tmov " + size(operandSize) + '[' + dstReg + '+' + dstIdxReg
            + '*' + scale + disp(dstDisp) + "],0x" + NumberUtils.hex(imm32));
    }

    public void writeMOVD(int operandSize, X86Register.MMX mmx, X86Register.GPR reg, int disp) {
        println("\tmovd " + mmx + ',' + size(operandSize) + '[' + reg + disp(disp) + ']');
    }

    public void writeMOVD(int operandSize, X86Register.GPR dstReg, int dstDisp, X86Register.MMX srcMmx) {
        println("\tmovd " + size(operandSize) + '[' + dstReg + disp(dstDisp) + "]," + srcMmx);
    }

    public void writeMOVQ(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tmovd " + dstMmx + ',' + srcMmx);
    }

    public void writeMOVQ(int operandSize, X86Register.MMX dstMmx, X86Register.GPR srcGpr, int srcDisp) {
        println("\tmovq " + dstMmx + ',' + size(operandSize) + '[' + srcGpr + disp(srcDisp) + ']');
    }

    public void writeMOVQ(int operandSize, X86Register.MMX dstMmx, int srcDisp) {
        println("\tmovq " + dstMmx + ',' + size(operandSize) + '[' + disp(srcDisp) + ']');
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
        println("\tmovsd " + dst + ",qword [" + src + disp(srcDisp) + ']');
    }

    public void writeMOVSD(XMM dst, XMM src) {
        println("\tmovsd " + dst + ',' + src);
    }

    public void writeMOVSS(GPR dst, int dstDisp, XMM src) {
        println("\tmovss dword [" + dst + disp(dstDisp) + "]," + src);
    }

    public void writeMOVSS(XMM dst, GPR src, int srcDisp) {
        println("\tmovss " + dst + ",dword [" + src + disp(srcDisp) + ']');
    }

    public void writeMOVSS(XMM dst, XMM src) {
        println("\tmovss " + dst + ',' + src);
    }
    
    public void writeMOVSX(GPR dstReg, GPR srcReg, int srcSize) {
        println("\tmovsx " + dstReg + ',' + size(srcSize) + ' ' + srcReg);
    }

    public void writeMOVSX(GPR dstReg, GPR srcReg, int srcDisp, int srcSize) {
        println("\tmovsx " + dstReg + ',' + size(srcSize) + ' ' + '[' + srcReg
            + disp(srcDisp) + ']');
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
        println("\tmovsxd " + dstReg + ',' + srcReg);
    }

    public void writeMOVSW() {
        println("\tmovsw");
    }

    public void writeMOVZX(GPR dstReg, GPR srcReg, int srcSize) {
        println("\tmovzx " + dstReg + ',' + size(srcSize) + ' ' + srcReg);
    }

    public void writeMOVZX(GPR dstReg, GPR srcReg, int srcDisp, int srcSize) {
        println("\tmovzx " + dstReg + ',' + size(srcSize) + ' ' + '[' + srcReg
            + disp(srcDisp) + ']');
    }

    public void writeMUL_EAX(GPR srcReg) {
        println("\tmul " + srcReg);
    }

    public void writeNEG(GPR dstReg) {
        println("\tneg " + dstReg);
    }

    public void writeNEG(int operandSize, GPR dstReg, int dstDisp) {
        println("\tneg " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + ']');
    }

    public void writeNOP() {
        println("\tnop");
    }

    public void writeNOT(GPR dstReg) {
        println("\tnot " + dstReg);
    }

    public void writeNOT(int operandSize, GPR dstReg, int dstDisp) {
        println("\tnot " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + ']');
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
        println("\tor " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

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
        println("\tor " + size(operandSize) + '[' + dstReg + ":0x" + NumberUtils.hex(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * @param operandSize
     * @param dstDisp
     * @param imm32
     */
    public void writeOR(int operandSize, int dstDisp, int imm32) {
        println("\tor " + size(operandSize) + '[' + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    public void writeOR(GPR dstReg, GPR srcReg) {
        println("\tor " + dstReg + ',' + srcReg);
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeOR(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tor " + dstReg + ",[" + srcReg + disp(srcDisp) + ']');
    }

    public void writeOUT(int operandSize) {
        if (operandSize == X86Constants.BITS8) {
            println("\tout " + X86Register.DX + ',' + X86Register.AL);
        } else if (operandSize == X86Constants.BITS16) {
            println("\tout " + X86Register.DX + ',' + X86Register.AX);
        } else if (operandSize == X86Constants.BITS32) {
            println("\tout " + X86Register.DX + ',' + X86Register.EAX);
        } else {
            throw new IllegalArgumentException("Invalid operand size for OUT: " + operandSize);
        }
    }

    public void writeOUT(int operandSize, int imm8) {
        if (operandSize == X86Constants.BITS8) {
            println("\tout " + imm8 + ',' + X86Register.AL);
        } else if (operandSize == X86Constants.BITS16) {
            println("\tout " + imm8 + ',' + X86Register.AX);
        } else if (operandSize == X86Constants.BITS32) {
            println("\tout " + imm8 + ',' + X86Register.EAX);
        } else {
            throw new IllegalArgumentException("Invalid operand size for OUT: " + operandSize);
        }
    }

    public void writePACKUSWB(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpackuswb " + dstMmx + ',' + srcMmx);
    }

    public void writePADDW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpaddw " + dstMmx + ',' + srcMmx);
    }

    public void writePAND(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpand " + dstMmx + ',' + srcMmx);
    }

    public void writePCMPGTW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpcmpgtw " + dstMmx + ',' + srcMmx);
    }

    public void writePMULLW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpmullw " + dstMmx + ',' + srcMmx);
    }

    public void writePOP(GPR dstReg) {
        println("\tpop " + dstReg);
    }

    public void writePOP(SR dstReg) {
        println("\tpop " + dstReg);
    }

    public void writePOP(GPR dstReg, int dstDisp) {
        println("\tpop [" + dstReg + disp(dstDisp) + ']');
    }

    public void writePOPA() {
        println("\tpopa");
    }

    public void writePOPF() {
        println("\tpopf");
    }

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

    public void writePSHUFW(X86Register.MMX dstMmx, X86Register.MMX srcMmx, int imm8) {
        println("\tpshufw " + dstMmx + ',' + srcMmx + ",0x" + NumberUtils.hex(imm8));
    }

    public void writePSRLW(X86Register.MMX mmx, int imm8) {
        println("\tpsrlw " + mmx + ",0x" + NumberUtils.hex(imm8));
    }

    public void writePSUBW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpsubw " + dstMmx + ',' + srcMmx);
    }

    public void writePUNPCKLBW(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpsunpcklbw " + dstMmx + ',' + srcMmx);
    }

    /**
     * @return The offset of the start of the instruction.
     */
    public int writePUSH(int imm32) {
        return println("\tpush 0x" + NumberUtils.hex(imm32));
    }

    /**
     * @return The offset of the start of the instruction.
     */
    public int writePUSH(GPR srcReg) {
        return println("\tpush " + srcReg);
    }

    /**
     * @return The offset of the start of the instruction.
     */
    public int writePUSH(SR srcReg) {
        return println("\tpush " + srcReg);
    }

    /**
     * @return The offset of the start of the instruction.
     */
    public int writePUSH(GPR srcReg, int srcDisp) {
        return println("\tpush [" + srcReg + disp(srcDisp) + ']');
    }

    /**
     * @return The offset of the start of the instruction.
     */
    public int writePUSH(SR srcReg, int srcDisp) {
        return println("\tpush [" + srcReg + ':' + srcDisp + ']');
    }

    /**
     * @return The offset of the start of the instruction.
     */
    public int writePUSH(GPR srcBaseReg, GPR srcIndexReg, int srcScale,
                         int srcDisp) {
        return println("\tpush [" + srcBaseReg + disp(srcDisp) + '+'
            + srcIndexReg + '*' + srcScale + ']');
    }

    // PR
    /**
     * @return The offset of the start of the instruction.
     */
    public int writePUSH_Const(Object objRef) {
        return println("\tpush " + objRef);
    }

    public void writePUSHA() {
        println("\tpusha");
    }

    public void writePUSHF() {
        println("\tpushf");
    }

    public void writePXOR(X86Register.MMX dstMmx, X86Register.MMX srcMmx) {
        println("\tpxor " + dstMmx + ',' + srcMmx);
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

    public void writeRET() {
        println("\tret");
    }

    public void writeRET(int imm16) {
        println("\tret " + imm16);
    }

    public void writeSAHF() {
        println("\tsahf");
    }

    public void writeSAL(GPR dstReg, int imm8) {
        println("\tsal " + dstReg + ',' + imm8);
    }

    /**
     * @param srcReg
     * @param srcDisp
     * @param imm8
     */
    public void writeSAL(int operandSize, GPR srcReg, int srcDisp, int imm8) {
        println("\tsal " + size(operandSize) + '[' + srcReg + disp(srcDisp)
            + "]," + imm8);
    }

    public void writeSAL_CL(GPR dstReg) {
        println("\tsal " + dstReg + ",cl");
    }

    /**
     * @param srcReg
     * @param srcDisp
     */
    public void writeSAL_CL(int operandSize, GPR srcReg, int srcDisp) {
        println("\tsal " + size(operandSize) + '[' + srcReg + disp(srcDisp)
            + "],cl");
    }

    public void writeSAR(GPR dstReg, int imm8) {
        println("\tsar " + dstReg + ',' + imm8);
    }

    /**
     * @param srcReg
     * @param srcDisp
     * @param imm8
     */
    public void writeSAR(int operandSize, GPR srcReg, int srcDisp, int imm8) {
        println("\tsar " + size(operandSize) + '[' + srcReg + disp(srcDisp)
            + "]," + imm8);
    }

    public void writeSAR_CL(GPR dstReg) {
        println("\tsar " + dstReg + ",cl");
    }

    /**
     * @param srcReg
     * @param srcDisp
     */
    public void writeSAR_CL(int operandSize, GPR srcReg, int srcDisp) {
        println("\tsar " + size(operandSize) + '[' + srcReg + disp(srcDisp)
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

    public void writeSBB(int operandSize, GPR dstReg, int dstDisp, int imm32) {
        println("\tsbb " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    public void writeSBB(GPR dstReg, int dstDisp, GPR srcReg) {
        println("\tsbb [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    public void writeSBB(GPR dstReg, GPR srcReg) {
        println("\tsbb " + dstReg + ',' + srcReg);
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeSBB(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tsbb " + dstReg + ",[" + srcReg + disp(srcDisp) + ']');
    }

    /**
     * Create a SETcc dstReg
     *
     * @param dstReg
     * @param cc
     */
    public void writeSETCC(GPR dstReg, int cc) {
        println("\tset" + ccName(cc) + ' ' + dstReg);
    }

    public void writeSHL(GPR dstReg, int imm8) {
        println("\tshl " + dstReg + ',' + imm8);
    }

    public void writeSHL(int operandSize, GPR dstReg, int dstDisp, int imm8) {
        println("\tshl " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + "]," + imm8);
    }

    public void writeSHL_CL(GPR dstReg) {
        println("\tshl " + dstReg + ",cl");
    }

    public void writeSHL_CL(int operandSize, GPR dstReg, int dstDisp) {
        println("\tshl " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + "],CL");
    }

    public void writeSHLD_CL(GPR dstReg, GPR srcReg) {
        println("\tshld " + dstReg + ',' + srcReg + ",cl");
    }

    public void writeSHR(GPR dstReg, int imm8) {
        println("\tshr " + dstReg + ',' + imm8);
    }

    /**
     * @param srcReg
     * @param srcDisp
     * @param imm8
     */
    public void writeSHR(int operandSize, GPR srcReg, int srcDisp, int imm8) {
        println("\tshr " + size(operandSize) + '[' + srcReg + disp(srcDisp)
            + "]," + imm8);
    }

    public void writeSHR_CL(GPR dstReg) {
        println("\tshr " + dstReg + ",cl");
    }

    /**
     * @param srcReg
     * @param srcDisp
     */
    public void writeSHR_CL(int operandSize, GPR srcReg, int srcDisp) {
        println("\tshr " + size(operandSize) + '[' + srcReg + disp(srcDisp)
            + "],cl");
    }

    public void writeSHRD_CL(GPR dstReg, GPR srcReg) {
        println("\tshrd " + dstReg + ',' + srcReg + ",cl");
    }

    public void writeSTD() {
        println("\tstd");
    }

    public void writeSTI() {
        println("\tsti");
    }

    public void writeSTMXCSR(GPR srcReg, int disp) {
        println("\tstmxcsr dword [" + srcReg + disp(disp) + ']');
    }

    public void writeSTOSB() {
        println("\tstosb");
    }

    public void writeSTOSD() {
        println("\tstosd");
    }

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
        println("\tsub " + reg + ',' + imm32);
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
        println("\tsub " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    public void writeSUB(GPR dstReg, int dstDisp, GPR srcReg) {
        println("\tsub [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    public void writeSUB(GPR dstReg, GPR srcReg) {
        println("\tsub " + dstReg + ',' + srcReg);
    }


    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeSUB(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\tsub " + dstReg + ", [" + srcReg + disp(srcDisp) + ']');
    }

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
        println("\ttest " + size(operandSize) + '[' + reg + disp(disp) + "],0x"
            + NumberUtils.hex(imm32));
    }

    /**
     * @param operandSize
     * @param reg
     * @param disp
     * @param imm32
     */
    public void writeTEST(int operandSize, SR reg, int disp, int imm32) {
        println("\ttest " + size(operandSize) + '[' + reg + ":0x"
            + NumberUtils.hex(disp) + "],0x" + NumberUtils.hex(imm32));
    }

    /**
     * Create a TEST reg1, reg2
     *
     * @param reg1
     * @param reg2
     */
    public void writeTEST(GPR reg1, GPR reg2) {
        println("\ttest " + reg1 + ',' + reg2);
    }

    public void writeTEST_AL(int value) {
        println("\ttest al," + value);
    }

    public void writeTEST_EAX(int operandSize, int value) {
        testOperandSize(operandSize, BITS32 | BITS64);
        if (operandSize == BITS32) {
            println("\ttest eax," + value);
        } else if (operandSize == BITS64) {
            println("\ttest rax," + value);
        }
    }

    public void writeTEST(int operandSize, int destDisp, int imm32) {
        println("\ttest " + size(operandSize) + '[' + disp(destDisp) + "],0x" + NumberUtils.hex(imm32));
    }

    public void writeTo(OutputStream os) throws IOException {
    }

    /**
     * @param dstDisp
     * @param srcReg
     */
    public void writeXCHG(int dstDisp, X86Register.GPR srcReg) {
        println("\txchg [" + disp(dstDisp) + "], " + srcReg);
    }

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
        println("\txor " + size(operandSize) + '[' + dstReg + disp(dstDisp)
            + "],0x" + NumberUtils.hex(imm32));
    }

    public void writeXOR(GPR dstReg, int dstDisp, GPR srcReg) {
        println("\txor [" + dstReg + disp(dstDisp) + "]," + srcReg);
    }

    public void writeXOR(GPR dstReg, GPR srcReg) {
        println("\txor " + dstReg + ',' + srcReg);
    }

    /**
     * @param dstReg
     * @param srcReg
     * @param srcDisp
     */
    public void writeXOR(GPR dstReg, GPR srcReg, int srcDisp) {
        println("\txor " + dstReg + ", [" + srcReg + disp(srcDisp) + ']');
    }

    public void writeObjectRef(Object object) {
    }

    public void writeRDMSR() {
        println("\trdmsr");
    }

    public void writeWRMSR() {
        println("\twrmsr");
    }
}
