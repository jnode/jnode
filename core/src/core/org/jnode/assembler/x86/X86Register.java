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
 * Registers of the x86 architecture.
 * 
 * @author epr
 */
public class X86Register extends VmSystemObject implements X86Constants {

	public static final X86Register.GPR AL = new X86Register.GPR("al", REG_AL,
			REG8, 0);

	public static final X86Register.GPR AH = new X86Register.GPR("ah", REG8,
			REG8, 4);

	public static final X86Register.GPR AX = new X86Register.GPR("ax", REG_AX,
			REG16, 0);

	public static final X86Register.GPR EAX = new X86Register.GPR("eax",
			REG_EAX, REG32, 0, true);

	public static final X86Register.GPR BL = new X86Register.GPR("bl", REG8,
			REG8, 3);

	public static final X86Register.GPR BH = new X86Register.GPR("bh", REG8,
			REG8, 7);

	public static final X86Register.GPR BX = new X86Register.GPR("bx", REG16,
			REG16, 3);

	public static final X86Register.GPR EBX = new X86Register.GPR("ebx", REG32,
			REG32, 3, true);

	public static final X86Register.GPR CL = new X86Register.GPR("cl", REG_CL,
			REG8, 1);

	public static final X86Register.GPR CH = new X86Register.GPR("ch", REG8,
			REG8, 5);

	public static final X86Register.GPR CX = new X86Register.GPR("cx", REG_CX,
			REG16, 1);

	public static final X86Register.GPR ECX = new X86Register.GPR("ecx",
			REG_ECX, REG32, 1, true);

	public static final X86Register.GPR DL = new X86Register.GPR("dl", REG8,
			REG8, 2);

	public static final X86Register.GPR DH = new X86Register.GPR("dh", REG8,
			REG8, 6);

	public static final X86Register.GPR DX = new X86Register.GPR("dx", REG_DX,
			REG16, 2);

	public static final X86Register.GPR EDX = new X86Register.GPR("edx", REG32,
			REG32, 2, true);

	public static final X86Register.GPR SP = new X86Register.GPR("sp", REG16,
			REG16, 4);

	public static final X86Register.GPR ESP = new X86Register.GPR("esp", REG32,
			REG32, 4);

	public static final X86Register.GPR BP = new X86Register.GPR("bp", REG16,
			REG16, 5);

	public static final X86Register.GPR EBP = new X86Register.GPR("ebp", REG32,
			REG32, 5);

	public static final X86Register.GPR SI = new X86Register.GPR("si", REG16,
			REG16, 6);

	public static final X86Register.GPR ESI = new X86Register.GPR("esi", REG32,
			REG32, 6);

	public static final X86Register.GPR DI = new X86Register.GPR("di", REG16,
			REG16, 7);

	public static final X86Register.GPR EDI = new X86Register.GPR("edi", REG32,
			REG32, 7);

	/* Floating-point registers */
	public static final X86Register.FPU ST0 = new X86Register.FPU("st0", 0);

	public static final X86Register.FPU ST1 = new X86Register.FPU("st1", 1);

	public static final X86Register.FPU ST2 = new X86Register.FPU("st2", 2);

	public static final X86Register.FPU ST3 = new X86Register.FPU("st3", 3);

	public static final X86Register.FPU ST4 = new X86Register.FPU("st4", 4);

	public static final X86Register.FPU ST5 = new X86Register.FPU("st5", 5);

	public static final X86Register.FPU ST6 = new X86Register.FPU("st6", 6);

	public static final X86Register.FPU ST7 = new X86Register.FPU("st7", 7);

	/* MMX registers */
	public static final X86Register.MMX MM0 = new X86Register.MMX("mm0", 0);

	public static final X86Register.MMX MM1 = new X86Register.MMX("mm1", 1);

	public static final X86Register.MMX MM2 = new X86Register.MMX("mm2", 2);

	public static final X86Register.MMX MM3 = new X86Register.MMX("mm3", 3);

	public static final X86Register.MMX MM4 = new X86Register.MMX("mm4", 4);

	public static final X86Register.MMX MM5 = new X86Register.MMX("mm5", 5);

	public static final X86Register.MMX MM6 = new X86Register.MMX("mm6", 6);

	public static final X86Register.MMX MM7 = new X86Register.MMX("mm7", 7);

	/* SSE registers */
	public static final X86Register.XMM XMM0 = new X86Register.XMM("xmm0", 0);

	public static final X86Register.XMM XMM1 = new X86Register.XMM("xmm1", 1);

	public static final X86Register.XMM XMM2 = new X86Register.XMM("xmm2", 2);

	public static final X86Register.XMM XMM3 = new X86Register.XMM("xmm3", 3);

	public static final X86Register.XMM XMM4 = new X86Register.XMM("xmm4", 4);

	public static final X86Register.XMM XMM5 = new X86Register.XMM("xmm5", 5);

	public static final X86Register.XMM XMM6 = new X86Register.XMM("xmm6", 6);

	public static final X86Register.XMM XMM7 = new X86Register.XMM("xmm7", 7);

	private final String name;

	private final int type;

	private final int nr;

	private final int size;

	private final boolean suitableFor8Bit;

	public X86Register(String name, int type, int type2, int nr) {
		this(name, type, type2, nr, false);
	}

	public X86Register(String name, int type, int type2, int nr,
			boolean suitableFor8Bit) {
		this.name = name;
		this.type = type;
		this.nr = nr;
		this.size = type & X86Constants.SIZE_MASK;
		this.suitableFor8Bit = suitableFor8Bit;
	}

	/**
	 * Returns the name.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the type.
	 * 
	 * @return int
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns the nr.
	 * 
	 * @return int
	 */
	public int getNr() {
		return nr;
	}

	/**
	 * Returns the size of this register
	 * 
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
	 * 
	 * @return True for EAX, EBX, ECX, EDX, false otherwise.
	 */
	public boolean isSuitableForBits8() {
		return suitableFor8Bit;
	}

	public static final class GPR extends X86Register {

		/**
		 * @param name
		 * @param type
		 * @param type2
		 * @param nr
		 */
		public GPR(String name, int type, int type2, int nr) {
			super(name, type, type2, nr);
		}

		/**
		 * @param name
		 * @param type
		 * @param type2
		 * @param nr
		 * @param suitableFor8Bit
		 */
		public GPR(String name, int type, int type2, int nr,
				boolean suitableFor8Bit) {
			super(name, type, type2, nr, suitableFor8Bit);
		}
	}

	public static final class FPU extends X86Register {

		/**
		 * @param name
		 * @param type
		 * @param type2
		 * @param nr
		 */
		public FPU(String name, int nr) {
			super(name, (nr == 0) ? FPU0 : FPUREG, FPUREG, nr);
		}
	}

	public static final class MMX extends X86Register {

		/**
		 * @param name
		 * @param type
		 * @param type2
		 * @param nr
		 */
		public MMX(String name, int nr) {
			super(name, MMXREG, MMXREG, nr);
		}
	}

	public static final class XMM extends X86Register {

		/**
		 * @param name
		 * @param type
		 * @param type2
		 * @param nr
		 */
		public XMM(String name, int nr) {
			super(name, XMMREG, XMMREG, nr);
		}
	}
}
