
/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

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
	 * @param quad
	 */
	public abstract void generateCodeFor(BinaryQuad quad);

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
	public abstract void generateCodeFor(UnaryQuad quad);

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
}
