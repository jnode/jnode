/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.test;

import java.io.FileOutputStream;
import org.jnode.assembler.Label;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Operation;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.vm.x86.X86CpuID;

/**
 * @author epr
 */
public class X86StreamTest implements X86Constants {

    public static void main(String[] args)
        throws Exception {
        final Mode mode;
        if ((args.length > 0) && args[0].equals("x86_64")) {
            mode = Mode.CODE64;
        } else {
            mode = Mode.CODE32;
        }

        final X86BinaryAssembler os = new X86BinaryAssembler(X86CpuID.createID("pentium4"), mode, 0);

        if (mode.is32()) {
            testCode32(os);
        } else {
            testCode64(os);
        }

        FileOutputStream fos = new FileOutputStream("test.bin");
        os.writeTo(fos);
        fos.close();
    }

    private static void testCode64(X86Assembler os) throws UnresolvedObjectRefException {
        os.writeCDQE();
        os.writeMOV_Const(X86Register.R15, 0xFFFFFFFFL);
        os.writeAND(X86Register.RAX, X86Register.R15);
        os.writeMOVSXD(X86Register.R14, X86Register.R14d);
        os.writePUSH(X86Register.RAX);
        os.writePUSH(X86Register.R8);
        os.writeMOV_Const(X86Register.R14d, 0x80000000);
        os.writeNOP();
        final Label label = new Label("label");
        os.writeMOV_Const(X86Register.RDI, label);
        os.writeNOP();
        os.setObjectRef(label);
        os.writePUSH(X86Register.RBX);
        os.writePUSH(X86Register.RCX);
        os.writePUSH(X86Register.RDX);
        os.writePUSH(X86Register.RSI);
        os.writePUSH(X86Register.R8);
        os.writePUSH(X86Register.R9);
        os.writePUSH(X86Register.R10);
        os.writePUSH(X86Register.R11);
        os.writePUSH(X86Register.R12);
        os.writePUSH(X86Register.R13);
        os.writePUSH(X86Register.R14);
        os.writeNOP();
        os.writePOP(X86Register.R14);
        os.writePOP(X86Register.R13);
        os.writePOP(X86Register.R12);
        os.writePOP(X86Register.R11);
        os.writePOP(X86Register.R10);
        os.writePOP(X86Register.R9);
        os.writePOP(X86Register.R8);
        os.writePOP(X86Register.RSI);
        os.writePOP(X86Register.RDX);
        os.writePOP(X86Register.RCX);
        os.writePOP(X86Register.RBX);
        os.writePOP(X86Register.RAX);
        os.writeNOP();
        os.writePUSH(X86Register.RBP);
        os.writePOP(X86Register.RBP);
    }

    private static void testCode32(X86Assembler os) throws UnresolvedObjectRefException {
        GPR regs[] = {X86Register.EAX, X86Register.EBX, X86Register.ECX, X86Register.EDX, X86Register.ESI};
        for (GPR reg1 : regs) {
            os.writeIDIV_EAX(reg1);
        }
        if (true) {
            return;
        }

        final Label label = new Label("label");
        os.writeADD(X86Register.EDX, X86Register.EAX);
        os.setObjectRef(label);
        os.writeNOP();
        os.writeLOOP(label);
        os.writeTEST_AL(0xff);
        os.writeTEST(X86Register.EBX, 0xABCD1234);
        os.writeCMPXCHG_EAX(X86Register.EDX, 4, X86Register.ECX, false);
        os.writeLEA(X86Register.ESI, X86Register.ESI, X86Register.EBX, 8, 4);
        os.writeCMPXCHG_EAX(X86Register.EDX, 4, X86Register.ECX, true);
        os.writeJMP(label, 2, false);
        os.writeCALL(label, 4, false);
        os.writeTEST(X86Register.ECX, X86Register.EBX);
        os.writeCMOVcc(X86Constants.JLE, X86Register.EAX, X86Register.EBX);
        os.writeCMOVcc(X86Constants.JE, X86Register.EAX, X86Register.EBX, 5);
        os.writeADD(BITS32, X86Register.EAX, 28, 11);
        os.writeCALL(X86Register.EAX, 28);
        os.writeCMP(X86Register.EAX, X86Register.ECX, 4);
        os.writeCMP(X86Register.EAX, 4, X86Register.ECX);
        os.writePrefix(X86Constants.FS_PREFIX);
        os.writeCMP_MEM(X86Register.ESP, 24);
        os.writeMOV_Const(BITS32, X86Register.ESP, 4, 24);
        os.writeSBB(X86Register.EDX, 5);
        os.writeSBB(X86Register.EDX, 305);

        final Label jt = new Label("Jumptable");
        os.writeSHL(X86Register.ECX, 2);
        os.writeJMP(jt, X86Register.ECX);
        os.setObjectRef(jt);
        os.write32(0x1234ABCD);
        os.write32(0xFFEEDDCC);


        os.writeJMP(X86Register.EDX, 15);
        os.writeADD(X86Register.EDX, X86Register.EBX, 5);
        os.writeSUB(X86Register.EDX, 3);
        os.writeINC(BITS32, X86Register.EBX, 67); // INC [reg+67]
        os.writeCMP_Const(BITS32, X86Register.ECX, 0xF, 0x12);
        os.writeCMP_Const(BITS32, X86Register.ECX, 0x4, 0x1234);
        os.writeMOV_Const(BITS32, X86Register.EDI, X86Register.EAX, 4, 0x09, 0x1234);

        os.writeSETCC(X86Register.EDX, X86Constants.JA);

        os.writeADD(BITS32, X86Register.EAX, 28, 11);
        os.writeADD(BITS32, X86Register.EAX, 28, 255);

        os.writeSUB(X86Register.EAX, 11);
        os.writeSUB(X86Register.EAX, 255);

        os.writeSUB(BITS32, X86Register.EAX, 28, 11);
        os.writeSUB(BITS32, X86Register.EAX, 28, 255);

        os.writeTEST(BITS32, X86Register.EDI, 0x40, 0xFFFFFFFF);

        os.writeFLD32(X86Register.EAX, X86Register.ESI, 4, 15);
        os.writeFLD64(X86Register.EAX, X86Register.ESI, 8, 15);

        os.writeCALL(X86Register.EAX, X86Register.EDX, 1, 0);
        os.writeCALL(X86Register.EAX);
        os.writeCALL(X86Register.ESI);

        os.writeXCHG(X86Register.EAX, X86Register.EDX);
        os.writeXCHG(X86Register.ESI, X86Register.EAX);
        os.writeXCHG(X86Register.ECX, X86Register.EBX);

        os.writeXCHG(X86Register.EAX, 13, X86Register.EDX);
        os.writeXCHG(X86Register.ECX, 13, X86Register.EBX);

        os.writeMOV(X86Constants.BITS8, X86Register.ECX, X86Register.EBX, 1, 4, X86Register.ESI);
        os.writeMOV(X86Constants.BITS8, X86Register.EDX, X86Register.ECX, X86Register.EBX, 1, 4);
        os.writeMOVSX(X86Register.EDX, X86Register.EDX, X86Constants.BITS8);

        os.writeSAR(BITS32, X86Register.EBP, 16, 16);
        os.writeSAR(BITS32, X86Register.EBP, 16, 24);

        os.writeMOVZX(X86Register.EBX, X86Register.EBX, X86Constants.BITS16);
        os.writeAND(X86Register.EBX, 0x0000FFFF);

        // SSE tests
        os.writeArithSSEDOp(X86Operation.SSE_ADD, X86Register.XMM0, X86Register.XMM1);
        os.writeArithSSEDOp(X86Operation.SSE_ADD, X86Register.XMM0, X86Register.EBX, 5);
        os.writeArithSSEDOp(X86Operation.SSE_SUB, X86Register.XMM1, X86Register.XMM2);
        os.writeArithSSEDOp(X86Operation.SSE_SUB, X86Register.XMM1, X86Register.EBX, 5);
        os.writeArithSSEDOp(X86Operation.SSE_MUL, X86Register.XMM2, X86Register.XMM3);
        os.writeArithSSEDOp(X86Operation.SSE_MUL, X86Register.XMM2, X86Register.EBX, 5);
        os.writeArithSSEDOp(X86Operation.SSE_DIV, X86Register.XMM3, X86Register.XMM4);
        os.writeArithSSEDOp(X86Operation.SSE_DIV, X86Register.XMM3, X86Register.EBX, 5);

        os.writeArithSSESOp(X86Operation.SSE_ADD, X86Register.XMM0, X86Register.XMM1);
        os.writeArithSSESOp(X86Operation.SSE_ADD, X86Register.XMM0, X86Register.EBX, 5);
        os.writeArithSSESOp(X86Operation.SSE_SUB, X86Register.XMM1, X86Register.XMM2);
        os.writeArithSSESOp(X86Operation.SSE_SUB, X86Register.XMM1, X86Register.EBX, 5);
        os.writeArithSSESOp(X86Operation.SSE_MUL, X86Register.XMM2, X86Register.XMM3);
        os.writeArithSSESOp(X86Operation.SSE_MUL, X86Register.XMM2, X86Register.EBX, 5);
        os.writeArithSSESOp(X86Operation.SSE_DIV, X86Register.XMM3, X86Register.XMM4);
        os.writeArithSSESOp(X86Operation.SSE_DIV, X86Register.XMM3, X86Register.EBX, 5);

        os.writeMOVSD(X86Register.XMM0, X86Register.XMM1);
        os.writeMOVSD(X86Register.XMM0, X86Register.ESP, 0);
        os.writeMOVSD(X86Register.ESP, 0, X86Register.XMM1);

        os.writeMOVSS(X86Register.XMM0, X86Register.XMM1);
        os.writeMOVSS(X86Register.XMM0, X86Register.ESP, 0);
        os.writeMOVSS(X86Register.ESP, 0, X86Register.XMM1);
    }

}
