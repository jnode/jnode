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

import org.jnode.vm.VmSystemObject;

/**
 * <description>
 * 
 * @author epr
 */
public class X86Register extends VmSystemObject
implements X86Constants {

	public static final X86Register AL = new X86Register("al", REG_AL, REG8, 0);
	public static final X86Register AH = new X86Register("ah", REG8, REG8, 4);
	public static final X86Register AX = new X86Register("ax", REG_AX, REG16, 0);
	public static final X86Register EAX = new X86Register("eax", REG_EAX, REG32, 0, true);

	public static final X86Register BL = new X86Register("bl", REG8, REG8, 3);
	public static final X86Register BH = new X86Register("bh", REG8, REG8, 7);
	public static final X86Register BX = new X86Register("bx", REG16, REG16, 3);
	public static final X86Register EBX = new X86Register("ebx", REG32, REG32, 3, true);

	public static final X86Register CL = new X86Register("cl", REG_CL, REG8, 1);
	public static final X86Register CH = new X86Register("ch", REG8, REG8, 5);
	public static final X86Register CX = new X86Register("cx", REG_CX, REG16, 1);
	public static final X86Register ECX = new X86Register("ecx", REG_ECX, REG32, 1, true);

	public static final X86Register DL = new X86Register("dl", REG8, REG8, 2);
	public static final X86Register DH = new X86Register("dh", REG8, REG8, 6);
	public static final X86Register DX = new X86Register("dx", REG_DX, REG16, 2);
	public static final X86Register EDX = new X86Register("edx", REG32, REG32, 2, true);

	public static final X86Register SP = new X86Register("sp", REG16, REG16, 4);
	public static final X86Register ESP = new X86Register("esp", REG32, REG32, 4);

	public static final X86Register BP = new X86Register("bp", REG16, REG16, 5);
	public static final X86Register EBP = new X86Register("ebp", REG32, REG32, 5);

	public static final X86Register SI = new X86Register("si", REG16, REG16, 6);
	public static final X86Register ESI = new X86Register("esi", REG32, REG32, 6);

	public static final X86Register DI = new X86Register("di", REG16, REG16, 7);
	public static final X86Register EDI = new X86Register("edi", REG32, REG32, 7);
		
	/* Segment registers */
	public static final X86Register CS = new X86Register("cs", REG_CS, REG_SREG, 1);
	public static final X86Register DS = new X86Register("ds", REG_DESS, REG_SREG, 3);
	public static final X86Register ES = new X86Register("es", REG_DESS, REG_SREG, 0);
	public static final X86Register SS = new X86Register("ss", REG_DESS, REG_SREG, 2);
	public static final X86Register FS = new X86Register("fs", REG_FSGS, REG_SREG, 4);
	public static final X86Register GS = new X86Register("gs", REG_FSGS, REG_SREG, 5);
	public static final X86Register SEGR6 = new X86Register("segr6", REG_SEG67, REG_SREG, 6);
	public static final X86Register SEGR7 = new X86Register("segr7", REG_SEG67, REG_SREG, 7);

	/* Control registers */
	public static final X86Register CR0 = new X86Register("cr0", REG_CREG, REG_CREG, 0);
	public static final X86Register CR1 = new X86Register("cr1", REG_CREG, REG_CREG, 1);
	public static final X86Register CR2 = new X86Register("cr2", REG_CREG, REG_CREG, 2);
	public static final X86Register CR3 = new X86Register("cr3", REG_CREG, REG_CREG, 3);
	public static final X86Register CR4 = new X86Register("cr4", REG_CREG, REG_CREG, 4);
	public static final X86Register CR5 = new X86Register("cr5", REG_CREG, REG_CREG, 5);
	public static final X86Register CR6 = new X86Register("cr6", REG_CREG, REG_CREG, 6);
	public static final X86Register CR7 = new X86Register("cr7", REG_CREG, REG_CREG, 7);

	/* Debug registers */
	public static final X86Register DR0 = new X86Register("dr0", REG_DREG, REG_DREG, 0);
	public static final X86Register DR1 = new X86Register("dr1", REG_DREG, REG_DREG, 1);
	public static final X86Register DR2 = new X86Register("dr2", REG_DREG, REG_DREG, 2);
	public static final X86Register DR3 = new X86Register("dr3", REG_DREG, REG_DREG, 3);
	public static final X86Register DR4 = new X86Register("dr4", REG_DREG, REG_DREG, 4);
	public static final X86Register DR5 = new X86Register("dr5", REG_DREG, REG_DREG, 5);
	public static final X86Register DR6 = new X86Register("dr6", REG_DREG, REG_DREG, 6);
	public static final X86Register DR7 = new X86Register("dr7", REG_DREG, REG_DREG, 7);

	/* Test registers */
	public static final X86Register TR0 = new X86Register("tr0", REG_TREG, REG_TREG, 0);
	public static final X86Register TR1 = new X86Register("tr1", REG_TREG, REG_TREG, 1);
	public static final X86Register TR2 = new X86Register("tr2", REG_TREG, REG_TREG, 2);
	public static final X86Register TR3 = new X86Register("tr3", REG_TREG, REG_TREG, 3);
	public static final X86Register TR4 = new X86Register("tr4", REG_TREG, REG_TREG, 4);
	public static final X86Register TR5 = new X86Register("tr5", REG_TREG, REG_TREG, 5);
	public static final X86Register TR6 = new X86Register("tr6", REG_TREG, REG_TREG, 6);
	public static final X86Register TR7 = new X86Register("tr7", REG_TREG, REG_TREG, 7);

	/* Floating-point registers */
	public static final X86Register ST0 = new X86Register("st0", FPU0, FPUREG, 0);
	public static final X86Register ST1 = new X86Register("st1", FPUREG, FPUREG, 1);
	public static final X86Register ST2 = new X86Register("st2", FPUREG, FPUREG, 2);
	public static final X86Register ST3 = new X86Register("st3", FPUREG, FPUREG, 3);
	public static final X86Register ST4 = new X86Register("st4", FPUREG, FPUREG, 4);
	public static final X86Register ST5 = new X86Register("st5", FPUREG, FPUREG, 5);
	public static final X86Register ST6 = new X86Register("st6", FPUREG, FPUREG, 6);
	public static final X86Register ST7 = new X86Register("st7", FPUREG, FPUREG, 7);
		
	/* MMX registers */
	public static final X86Register MM0 = new X86Register("mm0", MMXREG, MMXREG, 0);
	public static final X86Register MM1 = new X86Register("mm1", MMXREG, MMXREG, 1);
	public static final X86Register MM2 = new X86Register("mm2", MMXREG, MMXREG, 2);
	public static final X86Register MM3 = new X86Register("mm3", MMXREG, MMXREG, 3);
	public static final X86Register MM4 = new X86Register("mm4", MMXREG, MMXREG, 4);
	public static final X86Register MM5 = new X86Register("mm5", MMXREG, MMXREG, 5);
	public static final X86Register MM6 = new X86Register("mm6", MMXREG, MMXREG, 6);
	public static final X86Register MM7 = new X86Register("mm7", MMXREG, MMXREG, 7);

	/* SSE registers */
	public static final X86Register XMM0 = new X86Register("xmm0", XMMREG, XMMREG, 0);
	public static final X86Register XMM1 = new X86Register("xmm1", XMMREG, XMMREG, 1);
	public static final X86Register XMM2 = new X86Register("xmm2", XMMREG, XMMREG, 2);
	public static final X86Register XMM3 = new X86Register("xmm3", XMMREG, XMMREG, 3);
	public static final X86Register XMM4 = new X86Register("xmm4", XMMREG, XMMREG, 4);
	public static final X86Register XMM5 = new X86Register("xmm5", XMMREG, XMMREG, 5);
	public static final X86Register XMM6 = new X86Register("xmm6", XMMREG, XMMREG, 6);
	public static final X86Register XMM7 = new X86Register("xmm7", XMMREG, XMMREG, 7);

	private final String name;
	private final int type;
	private final int nr;
	private final int size;
	private final boolean suitableFor8Bit;
		
	public X86Register(String name, int type, int type2, int nr) {
		this(name, type, type2, nr, false);
	}

	public X86Register(String name, int type, int type2, int nr, boolean suitableFor8Bit) {
		this.name = name;
		this.type = type;
		this.nr = nr;
		this.size = type & X86Constants.SIZE_MASK;
		this.suitableFor8Bit = suitableFor8Bit;
	}
	
	/**
	 * Returns the name.
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the type.
	 * @return int
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns the nr.
	 * @return int
	 */
	public int getNr() {
		return nr;
	}
	
	/**
	 * Returns the size of this register 
	 * @return int
	 * @see X86Constants#BITS8
	 * @see X86Constants#BITS16
	 * @see X86Constants#BITS32
	 * @see X86Constants#BITS64
	 * @see X86Constants#BITS80
	 */
	public int getSize() {
		return size;
	}
	
	public String toString() {
		return name;
	}
	
	/**
	 * Does this register have an 8-bit part.
	 * @return True for EAX, EBX, ECX, EDX, false otherwise.
	 */
	public boolean isSuitableForBits8() {
		return suitableFor8Bit;
	}
}
