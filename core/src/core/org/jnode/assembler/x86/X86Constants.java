/**
 * $Id$
 */
package org.jnode.assembler.x86;


/**
 * <description>
 * 
 * @author epr
 */
public interface X86Constants {

	/* Register definitions */
	public static final int rEAX        = 0;
	public static final int rECX        = 1;
	public static final int rEDX        = 2;
	public static final int rEBX        = 3;
	public static final int rESP        = 4;
	public static final int rEBP        = 5;
	public static final int rESI        = 6;
	public static final int rEDI        = 7;

	public static final int rCS         = 10;
	public static final int rSS         = 11;
	public static final int rDS         = 12;
	public static final int rES         = 13;
	public static final int rFS         = 14;
	public static final int rGS         = 15;
	
	/* Prefixes */
	/** Operand size prefix */
	public static final int OSIZE_PREFIX = 0x66;
	/** Address size prefix */
	public static final int ASIZE_PREFIX = 0x67;
	/** FS prefix */
	public static final int FS_PREFIX = 0x64;
	/** Lock prefix */
	public static final int LOCK_PREFIX = 0xF0;
	
	/* Opcodes */
	public static final int NOP   = 0x90;
	
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
	
	
	public static final String[] REG_NAMES = {
		"EAX", "ECX", "EDX", "EBX", "ESP", "EBP", "ESI", "EDI" 
	};
	
	/* size, and other attributes, of the operand */
	public static final int BITS8         = 0x00000001;
	public static final int BITS16        = 0x00000002;
	public static final int BITS32        = 0x00000004;
	public static final int BITS64        = 0x00000008;	       /* FPU only */
	public static final int BITS80        = 0x00000010;	       /* FPU only */
	public static final int FAR           = 0x00000020;	       /* grotty: this means 16:16 or */
						   /* 16:32, like in CALL/JMP */
	public static final int NEAR          = 0x00000040;
	public static final int SHORT         = 0x00000080;	       /* and this means what it says :) */

	public static final int SIZE_MASK     = 0x000000FF;	       /* all the size attributes */
	public static final int NON_SIZE      = (~SIZE_MASK);
	
	public static final int TO            = 0x00000100;          /* reverse effect in FADD, FSUB &c */

	/* type of operand: memory reference, register, etc. */
	public static final int MEMORY        = 0x00204000;
	public static final int REGISTER      = 0x00001000;	       /* register number in 'basereg' */
	public static final int IMMEDIATE     = 0x00002000;

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
 
 	/** Operand types 
	 */
	public static final int OPT_VOID      = 0;
	public static final int OPT_IMM       = IMMEDIATE;
	public static final int OPT_IMM8      = IMMEDIATE | BITS8;
	public static final int OPT_IMM16     = IMMEDIATE | BITS16;
	public static final int OPT_IMM32     = IMMEDIATE | BITS32;
	public static final int OPT_MEM       = MEMORY;
	public static final int OPT_MEM16     = MEMORY | BITS16;
	public static final int OPT_MEM32     = MEMORY | BITS32;
	public static final int OPT_MEM64     = MEMORY | BITS64;
	public static final int OPT_MEM80     = MEMORY | BITS80;
	public static final int OPT_MEM_OFFS  = 10;
	public static final int OPT_RM8       = REGMEM | BITS8;
	public static final int OPT_RM16      = REGMEM | BITS16;
	public static final int OPT_RM32      = REGMEM | BITS32;
	public static final int OPT_SBYTE     = 31;
	public static final int OPT_UNITY     = 37;
	
	public static final OperandType[] OPTS = {
		new OperandType(OPT_VOID, "void"),
		new OperandType(OPT_IMM, "imm"),
		new OperandType(OPT_IMM|NEAR, "imm|near"),
		new OperandType(OPT_IMM|FAR, "imm|far"),
		new OperandType(OPT_IMM|SHORT, "imm|short"),
		new OperandType(OPT_IMM, OPT_IMM, "imm:imm"),
		new OperandType(OPT_IMM, OPT_IMM16, "imm:imm16"),
		new OperandType(OPT_IMM, OPT_IMM32, "imm:imm32"),
		new OperandType(OPT_IMM8, "imm8"),
		new OperandType(OPT_IMM16, "imm16"),
		new OperandType(OPT_IMM16|NEAR, "imm16|near"),
		new OperandType(OPT_IMM16|FAR, "imm16|far"),
		new OperandType(OPT_IMM16, OPT_IMM, "imm16:imm"),
		//new OperandType(OPT_IMM16, OPT_IMM16, "imm16:imm16"),
		//new OperandType(OPT_IMM16, OPT_IMM32, "imm16:imm32"),
		new OperandType(OPT_IMM32, "imm32"),
		new OperandType(OPT_IMM32|NEAR, "imm32|near"),
		new OperandType(OPT_IMM32|FAR, "imm32|far"),
		new OperandType(OPT_IMM32, OPT_IMM, "imm32:imm"),
		new OperandType(OPT_MEM, "mem"),
		new OperandType(OPT_MEM|NEAR, "mem|near"),
		new OperandType(OPT_MEM|FAR, "mem|far"),
		new OperandType(OPT_MEM16, "mem16"),
		new OperandType(OPT_MEM16|NEAR, "mem16|near"),
		new OperandType(OPT_MEM16|FAR, "mem16|far"),
		new OperandType(OPT_MEM32, "mem32"),
		new OperandType(OPT_MEM32|NEAR, "mem32|near"),
		new OperandType(OPT_MEM32|FAR, "mem32|far"),
		new OperandType(OPT_MEM64, "mem64"),
		new OperandType(OPT_MEM80, "mem80"),
		new OperandType(OPT_MEM_OFFS, "mem_offs"),
		new OperandType(REG8, "reg8"),
		new OperandType(REG16, "reg16"),
		new OperandType(REG32, "reg32"),
		new OperandType(OPT_RM8, "rm8"),
		new OperandType(OPT_RM16, "rm16"),
		new OperandType(OPT_RM32, "rm32"),
		new OperandType(REG_AL, "reg_al"),
		new OperandType(REG_AX, "reg_ax"),
		new OperandType(REG_EAX, "reg_eax"),
		new OperandType(REG_CL, "reg_cl"),
		new OperandType(REG_CX, "reg_cx"),
		new OperandType(REG_ECX, "reg_ecx"),
		new OperandType(REG_DX, "reg_dx"),
		new OperandType(REG_CREG, "reg_creg"),
		new OperandType(REG_DREG, "reg_dreg"),
		new OperandType(REG_SREG, "reg_sreg"),
		new OperandType(REG_TREG, "reg_treg"),
		new OperandType(REG_CS, "reg_cs"),
		new OperandType(REG_DESS, "reg_dess"),
		new OperandType(REG_FSGS, "reg_fsgs"),
		new OperandType(OPT_SBYTE, "sbyte"),
		new OperandType(FPUREG, "fpureg"),
		new OperandType(FPUREG|TO, "fpureg|to"),
		new OperandType(FPU0, "fpu0"),
		new OperandType(MMXREG, "mmxreg"),
		new OperandType(XMMREG, "xmmreg"),
		new OperandType(OPT_UNITY, "unity"),
	};
	
	/** Opcode flags */
	public static final int F_8086      = 0x00000001;
	public static final int F_186       = 0x00000002;
	public static final int F_286       = 0x00000004;
	public static final int F_386       = 0x00000008;
	public static final int F_486       = 0x00000010;
	public static final int F_PENT      = 0x00000020;
	public static final int F_P6        = 0x00000040;
	public static final int F_IA64      = 0x00000080;
	public static final int F_CYRIX     = 0x00000100;
	public static final int F_ND        = 0x00000200;
	public static final int F_SB        = 0x00000400;
	public static final int F_SD        = 0x00000800;
	public static final int F_SM        = 0x00001000;
	public static final int F_SW        = 0x00002000;
	public static final int F_PROT      = 0x00004000;
	public static final int F_PRIV      = 0x00008000;
	public static final int F_UNDOC     = 0x00010000;
	public static final int F_MMX       = 0x00020000;
	public static final int F_FPU       = 0x00040000;
	public static final int F_3DNOW     = 0x00080000;
	public static final int F_SMM       = 0x00100000;
	public static final int F_SM2       = 0x00200000;
	public static final int F_AR2       = 0x00400000;
	public static final int F_AMD       = 0x00800000;
	public static final int F_KATMAI    = 0x01000000;
	public static final int F_SSE       = 0x02000000;
	public static final int F_AR1       = 0x04000000;
	public static final int F_WILLAMETTE= 0x08000000;
	public static final int F_SSE2      = 0x10000000;

	public static class OperandType {
		private int segType = -1;
		private int type;
		private String name;
		private boolean to;
		private boolean zhort;
		
		public OperandType(int type, String name) {
			this.type = type;
			this.name = name;
		}
		
		public OperandType(int segType, int type, String name) {
			this(type, name);
			this.segType = segType;
		}
		
		public OperandType(int type, String name, boolean to, boolean zhort) {
			this(type, name);
			this.to = to;
			this.zhort = zhort;
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
		 * Returns the segType.
		 * @return int
		 */
		public int getSegType() {
			return segType;
		}

		/**
		 * Returns the to.
		 * @return boolean
		 */
		public boolean isTo() {
			return to;
		}

		/**
		 * Returns the short.
		 * @return boolean
		 */
		public boolean isShort() {
			return zhort;
		}

	}
	
	public static class Flag {
		private int flag;
		private String name;
		
		public Flag(int flag, String name) {
			this.flag = flag;
			this.name = name;
		}
		/**
		 * Returns the flag.
		 * @return int
		 */
		public int getFlag() {
			return flag;
		}

		/**
		 * Returns the name of this flag
		 * @return String
		 */
		public String getName() {
			return name;
		}

	}
	
}
