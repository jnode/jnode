/*
 * $Id$
 */
package org.jnode.test;

import java.io.FileOutputStream;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Stream;
import org.jnode.vm.x86.X86CpuID;

/**
 * @author epr
 */
public class X86StreamTest {

	public static void main(String[] args) 
	throws Exception {
		
		final X86Stream os = new X86Stream(X86CpuID.createID("pentium4"), 0);
		
		final Label label = new Label("label");
		os.writeADD(Register.EDX, Register.EAX);
		os.setObjectRef(label);
		os.writeNOP();
		os.writeLOOP(label);
		os.writeTEST_AL(0xff);
		os.writeTEST(Register.EBX, 0xABCD1234);
		os.writeCMPXCHG_EAX(Register.EDX, 4, Register.ECX, false);
		os.writeLEA(Register.ESI, Register.ESI, Register.EBX, 8, 4);
		os.writeCMPXCHG_EAX(Register.EDX, 4, Register.ECX, true);
		os.writeJMP(label, 2, false);
		os.writeCALL(label, 4, false);
		os.writeTEST(Register.ECX, Register.EBX);
		os.writeCMOVcc(X86Constants.JLE, Register.EAX, Register.EBX);
		os.writeCMOVcc(X86Constants.JE, Register.EAX, Register.EBX, 5);
		os.writeADD(Register.EAX, 28, 11);
		os.writeCALL(Register.EAX, 28);
		os.writeCMP(Register.EAX, Register.ECX, 4);
		os.writeCMP(Register.EAX, 4, Register.ECX);
		os.writePrefix(X86Constants.FS_PREFIX);
		os.writeCMP_MEM(Register.ESP, 24);
		os.writeMOV_Const(Register.ESP, 4, 24);
		os.writeSBB(Register.EDX, 5);
		os.writeSBB(Register.EDX, 305);
		
		final Label jt = new Label("Jumptable");
		os.writeSHL(Register.ECX, 2);
		os.writeJMP(jt, Register.ECX);
		os.setObjectRef(jt);
		os.write32(0x1234ABCD);
		os.write32(0xFFEEDDCC);
		
		
		os.writeJMP(Register.EDX, 15);
		os.writeADD(Register.EDX, Register.EBX, 5);
		os.writeSUB(Register.EDX, 3);
		os.writeINC(Register.EBX, 67); // INC [reg+67]
		os.writeCMP_Const(Register.ECX, 0xF, 0x12);
		os.writeCMP_Const(Register.ECX, 0x4, 0x1234);
		os.writeMOV_Const(Register.EDI, Register.EAX, 4, 0x09, 0x1234);

		os.writeSETCC(Register.EDX, X86Constants.JA);

		os.writeADD(Register.EAX, 28, 11);
		os.writeADD(Register.EAX, 28, 255);
		
		os.writeSUB(Register.EAX, 11);
		os.writeSUB(Register.EAX, 255);

		os.writeSUB(Register.EAX, 28, 11);
		os.writeSUB(Register.EAX, 28, 255);

		os.writeTEST(Register.EDI, 0x40, 0xFFFFFFFF);
		
		os.writeFLD32(Register.EAX, Register.ESI, 4, 15);
		os.writeFLD64(Register.EAX, Register.ESI, 8, 15);
		
		os.writeCALL(Register.EAX, Register.EDX, 1, 0);
		os.writeCALL(Register.EAX);
		os.writeCALL(Register.ESI);
		
		os.writeXCHG(Register.EAX, Register.EDX);
		os.writeXCHG(Register.ESI, Register.EAX);
		os.writeXCHG(Register.ECX, Register.EBX);

		os.writeXCHG(Register.EAX, 13, Register.EDX);
		os.writeXCHG(Register.ECX, 13, Register.EBX);

		os.writeMOV(X86Constants.BITS8, Register.ECX, Register.EBX, 1, 4, Register.ESI);
		os.writeMOV(X86Constants.BITS8, Register.EDX, Register.ECX, Register.EBX, 1, 4);
		os.writeMOVSX(Register.EDX, Register.EDX, X86Constants.BITS8);

		os.writeSAR(Register.EBP, 16, 16);
		os.writeSAR(Register.EBP, 16, 24);
		
		os.writeMOVZX(Register.EBX, Register.EBX, X86Constants.BITS16);
		os.writeAND(Register.EBX, 0x0000FFFF);
		
		FileOutputStream fos = new FileOutputStream("test.bin");
		os.writeTo(fos);
		fos.close();
	}

}
