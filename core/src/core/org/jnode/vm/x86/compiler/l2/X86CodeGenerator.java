/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l2;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.Constant;
import org.jnode.vm.compiler.ir.IntConstant;
import org.jnode.vm.compiler.ir.Location;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.RegisterLocation;
import org.jnode.vm.compiler.ir.RegisterPool;
import org.jnode.vm.compiler.ir.StackLocation;
import org.jnode.vm.compiler.ir.Variable;
import org.jnode.vm.compiler.ir.quad.BinaryQuad;
import org.jnode.vm.compiler.ir.quad.ConditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.ConstantRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.UnaryQuad;
import org.jnode.vm.compiler.ir.quad.UnconditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.VarReturnQuad;
import org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad;
import org.jnode.vm.compiler.ir.quad.VoidReturnQuad;

/**
 * @author Madhu Siddalingaiah
 * @author Levente Sántha
 *
 */
public class X86CodeGenerator extends CodeGenerator {
	private Variable[] spilledVariables;
	private AbstractX86Stream os;
	private int displacement;

	private final RegisterPool registerPool;

	/**
	 * Initialize this instance
	 */
	public X86CodeGenerator(AbstractX86Stream x86Stream) {
		CodeGenerator.setCodeGenerator(this);
		this.registerPool = new X86RegisterPool();
		this.os = x86Stream;
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#getRegisterPool()
	 */
	public RegisterPool getRegisterPool() {
		return registerPool;
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#supports3AddrOps()
	 */
	public boolean supports3AddrOps() {
		return false;
	}

	/**
	 * @param vars
	 * @param nArgs
	 */
	public void setArgumentVariables(Variable[] vars, int nArgs) {
		displacement = 0;
		for (int i=0; i<nArgs; i+=1) {
			// TODO this might not be right, check with Ewout
			displacement = vars[i].getIndex() * 4;
			vars[i].setLocation(new StackLocation(displacement));
		}
		// not sure how big the last arg is...
		displacement += 8;
	}

	/**
	 * @param variables
	 */
	public void setSpilledVariables(Variable[] variables) {
		this.spilledVariables = variables;
		int n = spilledVariables.length;
		for (int i=0; i<n; i+=1) {
			StackLocation loc = (StackLocation) spilledVariables[i].getLocation();
			loc.setDisplacement(displacement);
			switch (spilledVariables[i].getType()) {
				case Operand.BYTE:
				case Operand.CHAR:
				case Operand.SHORT:
				case Operand.INT:
				case Operand.FLOAT:
				case Operand.REFERENCE:
					displacement += 4;
					break;
				case Operand.LONG:
				case Operand.DOUBLE:
					displacement += 8;
					break;
			}
		}
	}

	/**
	 *
	 */
	public void emitHeader() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.ConditionalBranchQuad)
	 */
	public void generateCodeFor(ConditionalBranchQuad quad) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.ConstantRefAssignQuad)
	 */
	public void generateCodeFor(ConstantRefAssignQuad quad) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnconditionalBranchQuad)
	 */
	public void generateCodeFor(UnconditionalBranchQuad quad) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad)
	 */
	public void generateCodeFor(VariableRefAssignQuad quad) {
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.VarReturnQuad)
	 */
	public void generateCodeFor(VarReturnQuad quad) {
		Operand op = quad.getOperand();
		// TODO must deal with other types, see else case also
		if (op instanceof IntConstant) {
			IntConstant iconst = (IntConstant) op;
			os.writeMOV_Const(Register.EAX, iconst.getValue());
		} else if (op instanceof Variable) {
			Variable var = (Variable) op;
			Location loc = var.getLocation();
			if (loc instanceof RegisterLocation) {
				RegisterLocation regLoc = (RegisterLocation) loc;
				Register src = (Register) regLoc.getRegister();
				if (!src.equals(Register.EAX)) {
					os.writeMOV(X86Constants.BITS32, Register.EAX, src);
				}
			} else {
				StackLocation stackLoc = (StackLocation) loc;
				os.writeMOV(X86Constants.BITS32, Register.EAX, Register.EBP,
					stackLoc.getDisplacement());
			}
		}
		os.writeRET();
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.VoidReturnQuad)
	 */
	public void generateCodeFor(VoidReturnQuad quad) {
		os.writeRET();
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, java.lang.Object, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateCodeFor(UnaryQuad quad, Object lhsReg, int operation,
		Constant con) {
		switch(operation) {
			case UnaryQuad.INEG:
				IntConstant iconst = (IntConstant) con;
				os.writeMOV_Const((Register) lhsReg, iconst.getValue());
				os.writeNEG((Register) lhsReg);
				break;
			// TODO finish operations
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, java.lang.Object, int, java.lang.Object)
	 */
	public void generateCodeFor(UnaryQuad quad, Object lhsReg, int operation,
		Object rhsReg) {
		switch(operation) {
            case UnaryQuad.I2L:
            case UnaryQuad.I2F:
            case UnaryQuad.I2D:
            case UnaryQuad.L2I:
            case UnaryQuad.L2F:
            case UnaryQuad.L2D:
            case UnaryQuad.F2I:
            case UnaryQuad.F2L:
            case UnaryQuad.F2D:
            case UnaryQuad.D2I:
            case UnaryQuad.D2L:
            case UnaryQuad.D2F:
            case UnaryQuad.I2B:
            case UnaryQuad.I2C:
            case UnaryQuad.I2S:
                throw new IllegalArgumentException("Unknown operation");
			case UnaryQuad.INEG:
				if (lhsReg != rhsReg) {
					os.writeMOV(X86Constants.BITS32, (Register) lhsReg, (Register) rhsReg);
				}
				os.writeNEG((Register) lhsReg);
				break;
            case UnaryQuad.LNEG:
            case UnaryQuad.FNEG:
            case UnaryQuad.DNEG:
			// TODO finish operations
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, java.lang.Object, int, int)
	 */
	public void generateCodeFor(UnaryQuad quad, Object lhsReg, int operation,
		int rhsDisp) {
		switch(operation) {
            case UnaryQuad.I2L:
            case UnaryQuad.I2F:
            case UnaryQuad.I2D:
            case UnaryQuad.L2I:
            case UnaryQuad.L2F:
            case UnaryQuad.L2D:
            case UnaryQuad.F2I:
            case UnaryQuad.F2L:
            case UnaryQuad.F2D:
            case UnaryQuad.D2I:
            case UnaryQuad.D2L:
            case UnaryQuad.D2F:
            case UnaryQuad.I2B:
            case UnaryQuad.I2C:
            case UnaryQuad.I2S:
                throw new IllegalArgumentException("Unknown operation");
			case UnaryQuad.INEG:
				os.writeMOV(X86Constants.BITS32, (Register) lhsReg, Register.EBP,
					rhsDisp);
				os.writeNEG((Register) lhsReg);
				break;
            case UnaryQuad.LNEG:
            case UnaryQuad.FNEG:
            case UnaryQuad.DNEG:
			// TODO finish operations
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, int, int, java.lang.Object)
	 */
	public void generateCodeFor(UnaryQuad quad, int lhsDisp, int operation,
		Object rhsReg) {
		switch(operation) {
            case UnaryQuad.I2L:
            case UnaryQuad.I2F:
            case UnaryQuad.I2D:
            case UnaryQuad.L2I:
            case UnaryQuad.L2F:
            case UnaryQuad.L2D:
            case UnaryQuad.F2I:
            case UnaryQuad.F2L:
            case UnaryQuad.F2D:
            case UnaryQuad.D2I:
            case UnaryQuad.D2L:
            case UnaryQuad.D2F:
            case UnaryQuad.I2B:
            case UnaryQuad.I2C:
            case UnaryQuad.I2S:
                throw new IllegalArgumentException("Unknown operation");
			case UnaryQuad.INEG:
				os.writeMOV(X86Constants.BITS32, Register.EBP,
					lhsDisp, (Register) rhsReg);
				os.writeNEG(Register.EBP, lhsDisp);
				break;
            case UnaryQuad.LNEG:
            case UnaryQuad.FNEG:
            case UnaryQuad.DNEG:
			// TODO finish operations
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, int, int, int)
	 */
	public void generateCodeFor(UnaryQuad quad, int lhsDisp, int operation, int rhsDisp) {
		throw new IllegalArgumentException("ineg memory-memory not done");
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, int, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateCodeFor(UnaryQuad quad, int lhsDisp, int operation, Constant con) {
		switch(operation) {
			case UnaryQuad.INEG:
				IntConstant iconst = (IntConstant) con;
				os.writeMOV_Const(Register.EBP, lhsDisp, iconst.getValue());
				os.writeNEG(Register.EBP, lhsDisp);
				break;
			// TODO finish operations
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

    /// WE should not get to this method
	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, org.jnode.vm.compiler.ir.Constant, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateBinaryOP(Object reg1, Constant c2, int operation, Constant c3) {
		switch(operation) {
			case BinaryQuad.IADD:
//				IntConstant iconst1 = (IntConstant) c2;
				//IntConstant iconst2 = (IntConstant) c3;
//				os.writeMOV_Const((Register) reg1, iconst1.getValue());
				// TODO STOPPED HERE...
//				break;
			// TODO finish operations
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, org.jnode.vm.compiler.ir.Constant, int, java.lang.Object)
	 */
	public void generateBinaryOP(Object reg1, Constant c2, int operation, Object reg3) {
        IntConstant iconst2 = (IntConstant) c2;
		switch(operation) {
			case BinaryQuad.IADD:
				os.writeMOV_Const((Register) reg1, iconst2.getValue());
				os.writeADD((Register)reg1, (Register)reg3);
				break;

            case BinaryQuad.IAND:
                os.writeMOV_Const((Register) reg1, iconst2.getValue());
                os.writeAND((Register)reg1, (Register)reg3);
                break;

            case BinaryQuad.IDIV:   //needs EAX
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IMUL:
                //os.writeIMUL_3(reg1, reg3, iconst2.getValue()); TODO
				break;

            case BinaryQuad.IOR:
                os.writeMOV_Const((Register) reg1, iconst2.getValue());
                os.writeOR((Register)reg1, (Register)reg3);
                break;

            case BinaryQuad.IREM:   //needs EAX
            case BinaryQuad.ISHL:   //needs CL
            case BinaryQuad.ISHR:   //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.ISUB:
                os.writeMOV_Const((Register) reg1, iconst2.getValue());
                os.writeSUB((Register)reg1, (Register)reg3);
                break;

            case BinaryQuad.IUSHR:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IXOR:
                os.writeMOV_Const((Register) reg1, iconst2.getValue());
                os.writeXOR((Register)reg1, (Register)reg3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
            case BinaryQuad.FADD:
            case BinaryQuad.FDIV:
            case BinaryQuad.FMUL:
            case BinaryQuad.FREM:
            case BinaryQuad.FSUB:
            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, org.jnode.vm.compiler.ir.Constant, int, int)
	 */
	public void generateBinaryOP(Object reg1, Constant c2, int operation, int disp3) {
        IntConstant iconst2 = (IntConstant) c2;
		switch(operation) {
			case BinaryQuad.IADD:
				os.writeMOV_Const((Register) reg1, iconst2.getValue());
				os.writeADD((Register)reg1, Register.EBP, disp3);
				break;
            case BinaryQuad.IAND:   //not supported
            case BinaryQuad.IDIV:   //not supported
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IMUL:
                //os.writeIMUL_3(reg1, Register.EBP, disp3, iconst2.getValue()); TODO
                break;

            case BinaryQuad.IOR:    //not supported
            case BinaryQuad.IREM:   //not supported
            case BinaryQuad.ISHL:   //not supported
            case BinaryQuad.ISHR:   //not supported
            case BinaryQuad.ISUB:   //not supported
            case BinaryQuad.IUSHR:  //not supported
            case BinaryQuad.IXOR:   //not supported
            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
            case BinaryQuad.FADD:
            case BinaryQuad.FDIV:
            case BinaryQuad.FMUL:
            case BinaryQuad.FREM:
            case BinaryQuad.FSUB:
            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
			default:
				throw new IllegalArgumentException("Unknown operation");
        }
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, java.lang.Object, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateBinaryOP(Object reg1, Object reg2, int operation, Constant c3) {
		IntConstant iconst3 = (IntConstant) c3;
		switch(operation) {

			case BinaryQuad.IADD:   //not supported
				throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IAND:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
                }
                os.writeAND((Register)reg1, iconst3.getValue());
                break;

            case BinaryQuad.IDIV:   //needs EAX
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IMUL:
                //os.writeIMUL_3(reg1, reg2, iconst3.getValue()); TODO
                break;

            case BinaryQuad.IOR:    //not supported
            case BinaryQuad.IREM:   //needs EAX
            case BinaryQuad.ISHL:   //needs CL
            case BinaryQuad.ISHR:   //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.ISUB:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
                }
                os.writeSUB((Register)reg1, iconst3.getValue());
                break;

            case BinaryQuad.IUSHR:  //needs CL
            case BinaryQuad.IXOR:   //not supported
            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
            case BinaryQuad.FADD:
            case BinaryQuad.FDIV:
            case BinaryQuad.FMUL:
            case BinaryQuad.FREM:
            case BinaryQuad.FSUB:
            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, java.lang.Object, int, java.lang.Object)
	 */
	public void generateBinaryOP(Object reg1, Object reg2, int operation, Object reg3) {

        switch(operation) {

            case BinaryQuad.IADD:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
                }
                os.writeADD((Register) reg1, (Register) reg3);
                break;

            case BinaryQuad.IAND:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
                }
                os.writeAND((Register) reg1, (Register) reg3);
                break;

            case BinaryQuad.IDIV:   //needs EAX
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IMUL:   //needs EAX
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
                }
                //os.writeIMUL(reg1, reg3); TODO

            case BinaryQuad.IOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
                }
                os.writeOR((Register) reg1, (Register) reg3);
                break;

            case BinaryQuad.IREM:   //needs EAX
            case BinaryQuad.ISHL:   //needs CL
            case BinaryQuad.ISHR:   //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.ISUB:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
                }
                os.writeSUB((Register) reg1, (Register) reg3);
                break;

            case BinaryQuad.IUSHR:  //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IXOR:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
                }
                os.writeXOR((Register) reg1, (Register) reg3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
            case BinaryQuad.FADD:
            case BinaryQuad.FDIV:
            case BinaryQuad.FMUL:
            case BinaryQuad.FREM:
            case BinaryQuad.FSUB:
            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, java.lang.Object, int, int)
	 */
	public void generateBinaryOP(Object reg1, Object reg2, int operation, int disp3) {
        switch(operation) {

            case BinaryQuad.IADD:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
                }
                os.writeADD((Register) reg1, Register.EBP, disp3);
                break;

            case BinaryQuad.IAND:   //not supported
//                if (reg1 != reg2) {
//                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
//                }
//                os.writeAND((Register) reg1, Register.EBP, disp3);
//                break;

            case BinaryQuad.IDIV:   //needs EAX
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IMUL:
                if (reg1 != reg2) {
                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
                }
                //os.writeIMUL(reg1, Register.EBP, disp3); TODO
                break;

            case BinaryQuad.IOR:    //not supported
//                if (reg1 != reg2) {
//                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
//                }
//                os.writeOR((Register) reg1, Register.EBP, disp3);
//                break;

            case BinaryQuad.IREM:   //needs EAX
            case BinaryQuad.ISHL:   //needs CL
            case BinaryQuad.ISHR:   //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.ISUB:   //not supported
//                if (reg1 != reg2) {
//                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
//                }
//                os.writeSUB((Register) reg1, Register.EBP, disp3);
//                break;

            case BinaryQuad.IUSHR:  //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IXOR:   //not supported
//                if (reg1 != reg2) {
//                    os.writeMOV(X86Constants.BITS32, (Register) reg1, (Register) reg2);
//                }
//                os.writeXOR((Register) reg1, Register.EBP, disp3);
//                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
            case BinaryQuad.FADD:
            case BinaryQuad.FDIV:
            case BinaryQuad.FMUL:
            case BinaryQuad.FREM:
            case BinaryQuad.FSUB:
            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, int, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateBinaryOP(Object reg1, int disp2, int operation, Constant c3) {
        IntConstant iconst3 = (IntConstant) c3;
        switch(operation) {

            case BinaryQuad.IADD:   //not supported
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IAND:
                os.writeMOV(X86Constants.BITS32, (Register) reg1, Register.EBP, disp2);
                os.writeAND((Register)reg1, iconst3.getValue());
                break;

            case BinaryQuad.IDIV:   //needs EAX
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IMUL:   //needs EAX
                //os.writeIMUL_3(reg1, Register.EBP, disp2, iconst3.getValue());
                break;

            case BinaryQuad.IOR:    //not supported
            case BinaryQuad.IREM:   //needs EAX
            case BinaryQuad.ISHL:   //needs CL
            case BinaryQuad.ISHR:   //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.ISUB:
                os.writeMOV(X86Constants.BITS32, (Register) reg1, Register.EBP, disp2);
                os.writeSUB((Register)reg1, iconst3.getValue());
                break;

            case BinaryQuad.IUSHR:  //needs CL
            case BinaryQuad.IXOR:   //not supported
            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
            case BinaryQuad.FADD:
            case BinaryQuad.FDIV:
            case BinaryQuad.FMUL:
            case BinaryQuad.FREM:
            case BinaryQuad.FSUB:
            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }

	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, int, int, java.lang.Object)
	 */
	public void generateBinaryOP(Object reg1, int disp2, int operation, Object reg3) {
		switch(operation) {

            case BinaryQuad.IADD:
                os.writeMOV(X86Constants.BITS32, (Register) reg1, Register.EBP, disp2);
                os.writeADD((Register) reg1, (Register) reg3);
                break;

            case BinaryQuad.IAND:
                os.writeMOV(X86Constants.BITS32, (Register) reg1, Register.EBP, disp2);
                os.writeAND((Register) reg1, (Register) reg3);
                break;

            case BinaryQuad.IDIV:   //needs EAX
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IMUL:   //needs EAX
                os.writeMOV(X86Constants.BITS32, (Register) reg1, Register.EBP, disp2);
                //os.writeIMUL((Register) reg1, (Register) reg3); TODO
                break;

            case BinaryQuad.IOR:
                os.writeMOV(X86Constants.BITS32, (Register) reg1, Register.EBP, disp2);
                os.writeOR((Register) reg1, (Register) reg3);
                break;

            case BinaryQuad.IREM:   //needs EAX
            case BinaryQuad.ISHL:   //needs CL
            case BinaryQuad.ISHR:   //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.ISUB:
                os.writeMOV(X86Constants.BITS32, (Register) reg1, Register.EBP, disp2);
                os.writeSUB((Register) reg1, (Register) reg3);
                break;

            case BinaryQuad.IUSHR:  //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IXOR:
                os.writeMOV(X86Constants.BITS32, (Register) reg1, Register.EBP, disp2);
                os.writeXOR((Register) reg1, (Register) reg3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
            case BinaryQuad.FADD:
            case BinaryQuad.FDIV:
            case BinaryQuad.FMUL:
            case BinaryQuad.FREM:
            case BinaryQuad.FSUB:
            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, int, int, int)
	 */
	public void generateBinaryOP(Object reg1, int disp2, int operation, int disp3) {
        switch(operation) {
            case BinaryQuad.IADD:
                os.writeMOV(X86Constants.BITS32, (Register) reg1, Register.EBP, disp2);
                os.writeADD((Register) reg1, Register.EBP, disp3);
                break;

            case BinaryQuad.IAND:   //not supported
            case BinaryQuad.IDIV:   //needs EAX
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IMUL:   //needs EAX
                os.writeMOV(X86Constants.BITS32, (Register) reg1, Register.EBP, disp2);
                //os.writeIMUL((Register) reg1, Register.EBP, disp3);
                break;

            case BinaryQuad.IOR:    //not supported
            case BinaryQuad.IREM:   //needs EAX
            case BinaryQuad.ISHL:   //needs CL
            case BinaryQuad.ISHR:   //needs CL
            case BinaryQuad.ISUB:   //not supported
            case BinaryQuad.IUSHR:  //needs CL
            case BinaryQuad.IXOR:   //not supported
            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
            case BinaryQuad.FADD:
            case BinaryQuad.FDIV:
            case BinaryQuad.FMUL:
            case BinaryQuad.FREM:
            case BinaryQuad.FSUB:
            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
	}


    /// WE should not get to this method
	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, org.jnode.vm.compiler.ir.Constant, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateBinaryOP(int disp1, Constant c2, int operation, Constant c3) {
		// not supported, see next method

	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, org.jnode.vm.compiler.ir.Constant, int, java.lang.Object)
	 */
	public void generateBinaryOP(int disp1, Constant c2, int operation, Object reg3) {
        IntConstant iconst2 = (IntConstant) c2;
		switch(operation) {
            case BinaryQuad.IADD:
                os.writeMOV_Const(Register.EBP, disp1, iconst2.getValue());
                os.writeADD(Register.EBP, disp1, (Register) reg3);
                break;

            case BinaryQuad.IAND:
                os.writeMOV_Const(Register.EBP, disp1, iconst2.getValue());
                os.writeAND(Register.EBP, disp1, (Register) reg3);
                break;

            case BinaryQuad.IDIV:   //needs EAX
            case BinaryQuad.IMUL:   //needs EAX
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IOR:
                os.writeMOV_Const(Register.EBP, disp1, iconst2.getValue());
                os.writeOR(Register.EBP, disp1, (Register) reg3);
                break;

            case BinaryQuad.IREM:   //needs EAX
            case BinaryQuad.ISHL:   //needs CL
            case BinaryQuad.ISHR:   //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.ISUB:   //not supported
            case BinaryQuad.IUSHR:  //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IXOR:   //not supported
                os.writeMOV_Const(Register.EBP, disp1, iconst2.getValue());
                os.writeXOR(Register.EBP, disp1, (Register) reg3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
            case BinaryQuad.FADD:
            case BinaryQuad.FDIV:
            case BinaryQuad.FMUL:
            case BinaryQuad.FREM:
            case BinaryQuad.FSUB:
            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, org.jnode.vm.compiler.ir.Constant, int, int)
	 */
	public void generateBinaryOP(int disp1, Constant c2, int operation, int disp3) {
        IntConstant iconst2 = (IntConstant) c2;
		switch(operation) {
            case BinaryQuad.IADD:
            case BinaryQuad.IAND:
            case BinaryQuad.IDIV:
            case BinaryQuad.IMUL:
            case BinaryQuad.IOR:
            case BinaryQuad.IREM:
            case BinaryQuad.ISHL:
            case BinaryQuad.ISHR:
            case BinaryQuad.ISUB:
            case BinaryQuad.IUSHR:
            case BinaryQuad.IXOR:
            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.FADD:
                os.writeMOV_Const(Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(Register.EBP, disp1);
                os.writeFADD32(Register.EBP, disp3);
                os.writeFSTP32(Register.EBP, disp1);
                break;

            case BinaryQuad.FDIV:
                os.writeMOV_Const(Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(Register.EBP, disp1);
                os.writeFDIV32(Register.EBP, disp3);
                os.writeFSTP32(Register.EBP, disp1);
                break;

            case BinaryQuad.FMUL:
                os.writeMOV_Const(Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(Register.EBP, disp1);
                os.writeFMUL32(Register.EBP, disp3);
                os.writeFSTP32(Register.EBP, disp1);
                break;

            case BinaryQuad.FREM:
                os.writeMOV_Const(Register.EBP, disp1, iconst2.getValue());
                os.writeFSUB32(Register.EBP, disp3);
                os.writeFLD32(Register.EBP, disp1);
                os.writeFPREM();
                os.writeFSTP32(Register.EBP, disp1);
                os.writeFFREE(Register.ST0);
                break;

            case BinaryQuad.FSUB:
                os.writeMOV_Const(Register.EBP, disp1, iconst2.getValue());
                os.writeFLD32(Register.EBP, disp1);
                os.writeFSUB32(Register.EBP, disp3);
                os.writeFSTP32(Register.EBP, disp1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, java.lang.Object, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateBinaryOP(int disp1, Object reg2, int operation, Constant c3) {
		IntConstant iconst3 = (IntConstant) c3;
		switch(operation) {

			case BinaryQuad.IADD:
                os.writeMOV(X86Constants.BITS32, Register.EBP, disp1, (Register) reg2);
                os.writeADD(Register.EBP, disp1, iconst3.getValue());
                break;

            case BinaryQuad.IAND:   //not supported
            case BinaryQuad.IDIV:   //needs EAX
            case BinaryQuad.IMUL:   //needs EAX
            case BinaryQuad.IOR:    //not supported
            case BinaryQuad.IREM:   //needs EAX
            case BinaryQuad.ISHL:   //needs CL
            case BinaryQuad.ISHR:   //needs CL
            case BinaryQuad.ISUB:   //not supported
            case BinaryQuad.IUSHR:  //needs CL
            case BinaryQuad.IXOR:   //not supported
            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
            case BinaryQuad.FADD:
            case BinaryQuad.FDIV:
            case BinaryQuad.FMUL:
            case BinaryQuad.FREM:
            case BinaryQuad.FSUB:
            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, java.lang.Object, int, java.lang.Object)
	 */
	public void generateBinaryOP(int disp1, Object reg2, int operation, Object reg3) {
        switch(operation) {
            case BinaryQuad.IADD:
                os.writeMOV(X86Constants.BITS32, Register.EBP, disp1, (Register) reg2);
                os.writeADD(Register.EBP, disp1, (Register) reg3);
                break;

            case BinaryQuad.IAND:
                os.writeMOV(X86Constants.BITS32, Register.EBP, disp1, (Register) reg2);
                os.writeAND(Register.EBP, disp1, (Register) reg3);
                break;

            case BinaryQuad.IDIV:   //needs EAX
            case BinaryQuad.IMUL:   //needs EAX
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IOR:
                os.writeMOV(X86Constants.BITS32, Register.EBP, disp1, (Register) reg2);
                os.writeOR(Register.EBP, disp1, (Register) reg3);
                break;

            case BinaryQuad.IREM:   //needs EAX
            case BinaryQuad.ISHL:   //needs CL
            case BinaryQuad.ISHR:   //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.ISUB:
                os.writeMOV(X86Constants.BITS32, Register.EBP, disp1, (Register) reg2);
                os.writeSUB(Register.EBP, disp1, (Register) reg3);
                break;

            case BinaryQuad.IUSHR:  //needs CL
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.IXOR:
                os.writeMOV(X86Constants.BITS32, Register.EBP, disp1, (Register) reg2);
                os.writeXOR(Register.EBP, disp1, (Register) reg3);
                break;

            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
            case BinaryQuad.FADD:
            case BinaryQuad.FDIV:
            case BinaryQuad.FMUL:
            case BinaryQuad.FREM:
            case BinaryQuad.FSUB:
            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, java.lang.Object, int, int)
	 */
	public void generateBinaryOP(int disp1, Object reg2, int operation, int disp3) {
		// not supported

	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, int, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateBinaryOP(int disp1, int disp2, int operation, Constant c3) {
        IntConstant iconst3 = (IntConstant) c3;
        switch(operation) {

            case BinaryQuad.IADD:   //not supported due to the move bellow
//                if(disp1 != disp2){
//                    os.writeMOV(X86Constants.BITS32, Register.EBP, disp1, Register.EBP, disp2);
//                }
//                os.writeADD(Register.EBP, disp1, iconst3.getValue());
//                break;

            case BinaryQuad.IAND:   //not supported
            case BinaryQuad.IDIV:   //needs EAX
            case BinaryQuad.IMUL:   //needs EAX
            case BinaryQuad.IOR:    //not supported
            case BinaryQuad.IREM:   //needs EAX
            case BinaryQuad.ISHL:   //needs CL
            case BinaryQuad.ISHR:   //needs CL
            case BinaryQuad.ISUB:   //not supported
            case BinaryQuad.IUSHR:  //needs CL
            case BinaryQuad.IXOR:   //not supported
            case BinaryQuad.DADD:
            case BinaryQuad.DDIV:
            case BinaryQuad.DMUL:
            case BinaryQuad.DREM:
            case BinaryQuad.DSUB:
            case BinaryQuad.FADD:
            case BinaryQuad.FDIV:
            case BinaryQuad.FMUL:
            case BinaryQuad.FREM:
            case BinaryQuad.FSUB:
            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, int, int, java.lang.Object)
	 */
	public void generateBinaryOP(int disp1, int disp2, int operation, Object reg3) {
		//not supported for the reason in the previous method
	}

	/**
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, int, int, int)
	 */
	public void generateBinaryOP(int disp1, int disp2, int operation, int disp3) {
        switch(operation) {
            case BinaryQuad.IADD:   //not supported due to the move bellow
            case BinaryQuad.IAND:   //not supported
            case BinaryQuad.IDIV:   //needs EAX
            case BinaryQuad.IMUL:   //needs EAX
            case BinaryQuad.IOR:    //not supported
            case BinaryQuad.IREM:   //needs EAX
            case BinaryQuad.ISHL:   //needs CL
            case BinaryQuad.ISHR:   //needs CL
            case BinaryQuad.ISUB:   //not supported
            case BinaryQuad.IUSHR:  //needs CL
            case BinaryQuad.IXOR:   //not supported
                throw new IllegalArgumentException("Unknown operation");

            case BinaryQuad.DADD:
                os.writeFLD64(Register.EBP, disp2);
                os.writeFADD64(Register.EBP, disp3);
                os.writeFSTP64(Register.EBP, disp1);
                break;

            case BinaryQuad.DDIV:
                os.writeFLD64(Register.EBP, disp2);
                os.writeFDIV64(Register.EBP, disp3);
                os.writeFSTP64(Register.EBP, disp1);
                break;

            case BinaryQuad.DMUL:
                os.writeFLD64(Register.EBP, disp2);
                os.writeFMUL64(Register.EBP, disp3);
                os.writeFSTP64(Register.EBP, disp1);
                break;

            case BinaryQuad.DREM:
                os.writeFLD64(Register.EBP, disp3);
                os.writeFLD64(Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP64(Register.EBP, disp1);
                os.writeFFREE(Register.ST0);
                break;

            case BinaryQuad.DSUB:
                os.writeFLD64(Register.EBP, disp2);
                os.writeFSUB64(Register.EBP, disp3);
                os.writeFSTP64(Register.EBP, disp1);
                break;

            case BinaryQuad.FADD:
                os.writeFLD32(Register.EBP, disp2);
                os.writeFADD32(Register.EBP, disp3);
                os.writeFSTP32(Register.EBP, disp1);
                break;

            case BinaryQuad.FDIV:
                os.writeFLD32(Register.EBP, disp2);
                os.writeFADD32(Register.EBP, disp3);
                os.writeFSTP32(Register.EBP, disp1);
                break;

            case BinaryQuad.FMUL:
                os.writeFLD32(Register.EBP, disp2);
                os.writeFMUL32(Register.EBP, disp3);
                os.writeFSTP32(Register.EBP, disp1);
                break;

            case BinaryQuad.FREM:
                os.writeFLD32(Register.EBP, disp3);
                os.writeFLD32(Register.EBP, disp2);
                os.writeFPREM();
                os.writeFSTP32(Register.EBP, disp1);
                os.writeFFREE(Register.ST0);
                break;

            case BinaryQuad.FSUB:
                os.writeFLD32(Register.EBP, disp2);
                os.writeFSUB32(Register.EBP, disp3);
                os.writeFSTP32(Register.EBP, disp1);
                break;

            case BinaryQuad.LADD:
            case BinaryQuad.LAND:
            case BinaryQuad.LDIV:
            case BinaryQuad.LMUL:
            case BinaryQuad.LOR:
            case BinaryQuad.LREM:
            case BinaryQuad.LSHL:
            case BinaryQuad.LSHR:
            case BinaryQuad.LSUB:
            case BinaryQuad.LUSHR:
            case BinaryQuad.LXOR:
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
	}
}
