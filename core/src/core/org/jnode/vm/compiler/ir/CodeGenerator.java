
/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

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
public abstract class CodeGenerator {
	private static CodeGenerator cgInstance;

	public static void setCodeGenerator(CodeGenerator cg) {
		cgInstance = cg;
	}

	public static CodeGenerator getInstance() {
		return cgInstance;
	}

    public abstract void checkLabel(int address);

	/**
	 * @return
	 */
	public abstract RegisterPool getRegisterPool();

	/**
	 * Returns true of this CPU supports 3 address operands
	 * 
	 * @return
	 */
	public abstract boolean supports3AddrOps();

	/**
	 * @param variables
	 */
	public abstract void setSpilledVariables(Variable[] variables);

	/**
	 * 
	 */
	public abstract void emitHeader();

	/**
	 * @param quad
	 */
	public abstract void generateCodeFor(ConditionalBranchQuad quad);

	/**
	 * @param quad
	 */
	public abstract void generateCodeFor(ConstantRefAssignQuad quad);

	/**
	 * @param quad
	 */
	public abstract void generateCodeFor(UnconditionalBranchQuad quad);

	/**
	 * @param quad
	 */
	public abstract void generateCodeFor(VariableRefAssignQuad quad);

	/**
	 * @param quad
	 */
	public abstract void generateCodeFor(VarReturnQuad quad);

	/**
	 * @param quad
	 */
	public abstract void generateCodeFor(VoidReturnQuad quad);

	/**
	 * @param quad
	 * @param lhsReg
	 * @param operation
	 * @param con
	 */
	public abstract void generateCodeFor(UnaryQuad quad, Object lhsReg,
		int operation, Constant con);

	/**
	 * @param quad
	 * @param lhsReg
	 * @param operation
	 * @param rhsReg
	 */
	public abstract void generateCodeFor(UnaryQuad quad, Object lhsReg,
		int operation, Object rhsReg);

	/**
	 * @param quad
	 * @param lhsReg
	 * @param operation
	 * @param rhsDisp
	 */
	public abstract void generateCodeFor(UnaryQuad quad, Object lhsReg,
		int operation, int rhsDisp);

	/**
	 * @param quad
	 * @param lhsDisp
	 * @param operation
	 * @param rhsReg
	 */
	public abstract void generateCodeFor(UnaryQuad quad, int lhsDisp,
		int operation, Object rhsReg);

	/**
	 * @param quad
	 * @param lhsDisp
	 * @param operation
	 * @param rhsDisp
	 */
	public abstract void generateCodeFor(UnaryQuad quad, int lhsDisp,
		int operation, int rhsDisp);

	/**
	 * @param quad
	 * @param lhsDisp
	 * @param operation
	 * @param con
	 */
	public abstract void generateCodeFor(UnaryQuad quad, int lhsDisp,
		int operation, Constant con);

	/**
	 * @param reg1
	 * @param c2
	 * @param operation
	 * @param c3
	 */
	public abstract void generateBinaryOP(Object reg1, Constant c2,
		int operation, Constant c3);

	/**
	 * @param reg1
	 * @param c2
	 * @param operation
	 * @param reg3
	 */
	public abstract void generateBinaryOP(Object reg1, Constant c2,
		int operation, Object reg3);

	/**
	 * @param reg1
	 * @param c2
	 * @param operation
	 * @param disp3
	 */
	public abstract void generateBinaryOP(Object reg1, Constant c2,
		int operation, int disp3);

	/**
	 * @param reg1
	 * @param reg2
	 * @param operation
	 * @param c3
	 */
	public abstract void generateBinaryOP(Object reg1, Object reg2,
		int operation, Constant c3);

	/**
	 * @param reg1
	 * @param reg2
	 * @param operation
	 * @param reg3
	 */
	public abstract void generateBinaryOP(Object reg1, Object reg2,
		int operation, Object reg3);

	/**
	 * @param reg1
	 * @param reg2
	 * @param operation
	 * @param disp3
	 */
	public abstract void generateBinaryOP(Object reg1, Object reg2,
		int operation, int disp3);

	/**
	 * @param reg1
	 * @param disp2
	 * @param operation
	 * @param c3
	 */
	public abstract void generateBinaryOP(Object reg1, int disp2,
		int operation, Constant c3);

	/**
	 * @param reg1
	 * @param disp2
	 * @param operation
	 * @param reg3
	 */
	public abstract void generateBinaryOP(Object reg1, int disp2,
		int operation, Object reg3);

	/**
	 * @param reg1
	 * @param disp2
	 * @param operation
	 * @param disp3
	 */
	public abstract void generateBinaryOP(Object reg1, int disp2,
		int operation, int disp3);

	/**
	 * @param disp1
	 * @param c2
	 * @param operation
	 * @param c3
	 */
	public abstract void generateBinaryOP(int disp1, Constant c2,
		int operation, Constant c3);

	/**
	 * @param disp1
	 * @param c2
	 * @param operation
	 * @param reg3
	 */
	public abstract void generateBinaryOP(int disp1, Constant c2,
		int operation, Object reg3);

	/**
	 * @param disp1
	 * @param c2
	 * @param operation
	 * @param disp3
	 */
	public abstract void generateBinaryOP(int disp1, Constant c2,
		int operation, int disp3);

	/**
	 * @param disp1
	 * @param reg2
	 * @param operation
	 * @param c3
	 */
	public abstract void generateBinaryOP(int disp1, Object reg2,
		int operation, Constant c3);

	/**
	 * @param disp1
	 * @param reg2
	 * @param operation
	 * @param reg3
	 */
	public abstract void generateBinaryOP(int disp1, Object reg2,
		int operation, Object reg3);

	/**
	 * @param disp1
	 * @param reg2
	 * @param operation
	 * @param disp3
	 */
	public abstract void generateBinaryOP(int disp1, Object reg2,
		int operation, int disp3);

	/**
	 * @param disp1
	 * @param disp2
	 * @param operation
	 * @param c3
	 */
	public abstract void generateBinaryOP(int disp1, int disp2,
		int operation, Constant c3);

	/**
	 * @param disp1
	 * @param disp2
	 * @param operation
	 * @param reg3
	 */
	public abstract void generateBinaryOP(int disp1, int disp2,
		int operation, Object reg3);

	/**
	 * @param disp1
	 * @param disp2
	 * @param operation
	 * @param disp3
	 */
	public abstract void generateBinaryOP(int disp1, int disp2,
		int operation, int disp3);
}
