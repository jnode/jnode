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

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#getRegisterPool()
	 */
	public RegisterPool getRegisterPool() {
		return registerPool;
	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.ConditionalBranchQuad)
	 */
	public void generateCodeFor(ConditionalBranchQuad quad) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.ConstantRefAssignQuad)
	 */
	public void generateCodeFor(ConstantRefAssignQuad quad) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.UnconditionalBranchQuad)
	 */
	public void generateCodeFor(UnconditionalBranchQuad quad) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.VariableRefAssignQuad)
	 */
	public void generateCodeFor(VariableRefAssignQuad quad) {
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.VarReturnQuad)
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

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.VoidReturnQuad)
	 */
	public void generateCodeFor(VoidReturnQuad quad) {
		os.writeRET();
	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, java.lang.Object, int, java.lang.Object)
	 */
	public void generateCodeFor(UnaryQuad quad, Object lhsReg, int operation,
		Object rhsReg) {
		switch(operation) {
			case UnaryQuad.INEG:
				if (lhsReg != rhsReg) {
					os.writeMOV(X86Constants.BITS32, (Register) lhsReg, (Register) rhsReg);
				}
				os.writeNEG((Register) lhsReg);
				break;
			// TODO finish operations
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, java.lang.Object, int, int)
	 */
	public void generateCodeFor(UnaryQuad quad, Object lhsReg, int operation,
		int rhsDisp) {
		switch(operation) {
			case UnaryQuad.INEG:
				os.writeMOV(X86Constants.BITS32, (Register) lhsReg, Register.EBP,
					rhsDisp);
				os.writeNEG((Register) lhsReg);
				break;
			// TODO finish operations
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, int, int, java.lang.Object)
	 */
	public void generateCodeFor(UnaryQuad quad, int lhsDisp, int operation,
		Object rhsReg) {
		switch(operation) {
			case UnaryQuad.INEG:
				os.writeMOV(X86Constants.BITS32, Register.EBP,
					lhsDisp, (Register) rhsReg);
				os.writeNEG(Register.EBP, lhsDisp);
				break;
			// TODO finish operations
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.UnaryQuad, int, int, int)
	 */
	public void generateCodeFor(UnaryQuad quad, int lhsDisp, int operation, int rhsDisp) {
		throw new IllegalArgumentException("ineg memory-memory not done");
	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, org.jnode.vm.compiler.ir.Constant, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateBinaryOP(Object reg1, Constant c2, int operation, Constant c3) {
		switch(operation) {
			case BinaryQuad.IADD:
				IntConstant iconst1 = (IntConstant) c2;
				IntConstant iconst2 = (IntConstant) c3;
				os.writeMOV_Const((Register) reg1, iconst1.getValue());
				// TODO STOPPED HERE...
				break;
			// TODO finish operations
			default:
				throw new IllegalArgumentException("Unknown operation");
		}
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, org.jnode.vm.compiler.ir.Constant, int, java.lang.Object)
	 */
	public void generateBinaryOP(Object reg1, Constant c2, int operation, Object reg3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, org.jnode.vm.compiler.ir.Constant, int, int)
	 */
	public void generateBinaryOP(Object reg1, Constant c2, int operation, int disp3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, java.lang.Object, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateBinaryOP(Object reg1, Object reg2, int operation, Constant c3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, java.lang.Object, int, java.lang.Object)
	 */
	public void generateBinaryOP(Object reg1, Object reg2, int operation, Object reg3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, java.lang.Object, int, int)
	 */
	public void generateBinaryOP(Object reg1, Object reg2, int operation, int disp3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, int, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateBinaryOP(Object reg1, int disp2, int operation, Constant c3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, int, int, java.lang.Object)
	 */
	public void generateBinaryOP(Object reg1, int disp2, int operation, Object reg3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(java.lang.Object, int, int, int)
	 */
	public void generateBinaryOP(Object reg1, int disp2, int operation, int disp3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, org.jnode.vm.compiler.ir.Constant, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateBinaryOP(int disp1, Constant c2, int operation, Constant c3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, org.jnode.vm.compiler.ir.Constant, int, java.lang.Object)
	 */
	public void generateBinaryOP(int disp1, Constant c2, int operation, Object reg3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, org.jnode.vm.compiler.ir.Constant, int, int)
	 */
	public void generateBinaryOP(int disp1, Constant c2, int operation, int disp3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, java.lang.Object, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateBinaryOP(int disp1, Object reg2, int operation, Constant c3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, java.lang.Object, int, java.lang.Object)
	 */
	public void generateBinaryOP(int disp1, Object reg2, int operation, Object reg3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, java.lang.Object, int, int)
	 */
	public void generateBinaryOP(int disp1, Object reg2, int operation, int disp3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, int, int, org.jnode.vm.compiler.ir.Constant)
	 */
	public void generateBinaryOP(int disp1, int disp2, int operation, Constant c3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, int, int, java.lang.Object)
	 */
	public void generateBinaryOP(int disp1, int disp2, int operation, Object reg3) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateBinaryOP(int, int, int, int)
	 */
	public void generateBinaryOP(int disp1, int disp2, int operation, int disp3) {
		// TODO Auto-generated method stub
		
	}
}
