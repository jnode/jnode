
/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import org.jnode.vm.compiler.ir.quad.*;
import org.jnode.vm.compiler.ir.quad.ConditionalBranchQuad;
import org.jnode.vm.compiler.ir.quad.ConstantRefAssignQuad;
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
	 */
	public abstract void generateCodeFor(BinaryQuad quad);

	/**
	 * @param quad
	 * @param object
	 * @param operation
	 * @param con
	 */
	public abstract void generateCodeFor(UnaryQuad quad, Object lhsReg,
		int operation, Constant con);

	/**
	 * @param quad
	 * @param object
	 * @param operation
	 * @param object2
	 */
	public abstract void generateCodeFor(UnaryQuad quad, Object lhsReg,
		int operation, Object rhsReg);

	/**
	 * @param quad
	 * @param object
	 * @param operation
	 * @param i
	 */
	public abstract void generateCodeFor(UnaryQuad quad, Object lhsReg,
		int operation, int rhsDisp);

	/**
	 * @param quad
	 * @param lhsDisp
	 * @param operation
	 * @param object
	 */
	public abstract void generateCodeFor(UnaryQuad quad, int lhsDisp,
		int operation, Object rhsReg);

	/**
	 * @param quad
	 * @param lhsDisp
	 * @param operation
	 * @param i
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
}
