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


/**
 * <description>
 * 
 * @author epr
 */
public interface X86Constants {

	/* Prefixes */
	/** Operand size prefix */
	public static final int OSIZE_PREFIX = 0x66;
	/** Address size prefix */
	public static final int ASIZE_PREFIX = 0x67;
	/** FS prefix */
	public static final int FS_PREFIX = 0x64;
	/** Lock prefix */
	public static final int LOCK_PREFIX = 0xF0;
	
	/* Jump opcodes (after 0x0f) */
	public static final int JA    = 0x87;
	public static final int JAE   = 0x83;
	public static final int JB    = 0x82;
	public static final int JBE   = 0x86;
	public static final int JC    = 0x82;
	public static final int JE    = 0x84;
	public static final int JZ    = 0x84;
	public static final int JG    = 0x8f;
	public static final int JGE   = 0x8d;
	public static final int JL    = 0x8c;
	public static final int JLE   = 0x8e;
	public static final int JNA   = 0x86;
	public static final int JNAE  = 0x82;
	public static final int JNB   = 0x83;
	public static final int JNBE  = 0x87;
	public static final int JNC   = 0x83;
	public static final int JNE   = 0x85;
	public static final int JNG   = 0x8e;
	public static final int JNGE  = 0x8c;
	public static final int JNL   = 0x8d;
	public static final int JNLE  = 0x8f;
	public static final int JNO   = 0x81;
	public static final int JNP   = 0x8b;
	public static final int JNS   = 0x89;
	public static final int JNZ   = 0x85;
	public static final int JO    = 0x80;
	public static final int JP    = 0x8a;
	
	
	/* size, and other attributes, of the operand */
	public static final int BITS8         = 0x00000001;
	public static final int BITS16        = 0x00000002;
	public static final int BITS32        = 0x00000004;
	public static final int BITS64        = 0x00000008;	       /* FPU only */
	public static final int BITS80        = 0x00000010;	       /* FPU only */
	public static final int SIZE_MASK     = 0x000000FF;	       /* all the size attributes */

	public static final int REGMEM        = 0x00200000;	       /* for r/m, ie EA, operands */
	public static final int REGNORM       = 0x00201000;	       /* 'normal' reg, qualifies as EA */
	public static final int REG8          = 0x00201001;
	public static final int REG16         = 0x00201002;
	public static final int REG32         = 0x00201004;
	public static final int MMXREG        = 0x00201008;	       /* MMX registers */
	public static final int XMMREG        = 0x00201010;          /* XMM Katmai reg */
	public static final int FPUREG        = 0x01000000;	       /* floating point stack registers */
	public static final int FPU0          = 0x01000800;	       /* FPU stack register zero */

	/* special register operands: these may be treated differently */
	public static final int REG_SMASK     = 0x00070000;	       /* a mask for the following */
	public static final int REG_ACCUM     = 0x00211000;	       /* accumulator: AL, AX or EAX */
	public static final int REG_AL        = 0x00211001;	       /* REG_ACCUM | BITSxx */
	public static final int REG_AX        = 0x00211002;	       /* ditto */
	public static final int REG_EAX       = 0x00211004;	       /* and again */
	public static final int REG_COUNT     = 0x00221000;	       /* counter: CL, CX or ECX */
	public static final int REG_CL        = 0x00221001;	       /* REG_COUNT | BITSxx */
	public static final int REG_CX        = 0x00221002;	       /* ditto */
	public static final int REG_ECX       = 0x00221004;	       /* another one */
	public static final int REG_DX        = 0x00241002;
	public static final int REG_SREG      = 0x00081002;	       /* any segment register */
	public static final int REG_CS        = 0x01081002;	       /* CS */
	public static final int REG_DESS      = 0x02081002;	       /* DS, ES, SS (non-CS 86 registers) */
	public static final int REG_FSGS      = 0x04081002;	       /* FS, GS (386 extended registers) */
	public static final int REG_SEG67     = 0x08081002;          /* Non-implemented segment registers */
	public static final int REG_CDT       = 0x00101004;	       /* CRn, DRn and TRn */
	public static final int REG_CREG      = 0x08101004;	       /* CRn */
	public static final int REG_DREG      = 0x10101004;	       /* DRn */
	public static final int REG_TREG      = 0x20101004;	       /* TRn */
}
