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
		
		FileOutputStream fos = new FileOutputStream("test.bin");
		os.writeTo(fos);
		fos.close();
	}

}
