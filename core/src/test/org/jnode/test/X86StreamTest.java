/*
 * $Id$
 */
package org.jnode.test;

import java.io.FileOutputStream;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Stream;

/**
 * @author epr
 */
public class X86StreamTest {

	public static void main(String[] args) 
	throws Exception {
		
		final X86Stream os = new X86Stream(0);
		
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
		
		FileOutputStream fos = new FileOutputStream("test.bin");
		os.writeTo(fos);
		fos.close();
	}

}
