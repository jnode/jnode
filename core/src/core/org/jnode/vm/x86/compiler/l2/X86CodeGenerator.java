/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l2;

import org.jnode.util.BootableHashMap;
import org.jnode.vm.compiler.ir.BinaryQuad;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.ConditionalBranchQuad;
import org.jnode.vm.compiler.ir.ConstantRefAssignQuad;
import org.jnode.vm.compiler.ir.UnaryQuad;
import org.jnode.vm.compiler.ir.UnconditionalBranchQuad;
import org.jnode.vm.compiler.ir.VarReturnQuad;
import org.jnode.vm.compiler.ir.VariableRefAssignQuad;
import org.jnode.vm.compiler.ir.VoidReturnQuad;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class X86CodeGenerator extends CodeGenerator {
	private BootableHashMap variableMap;

	/**
	 * @param map
	 */
	public X86CodeGenerator(BootableHashMap variableMap) {
		this.variableMap = variableMap;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.BinaryQuad)
	 */
	public void generateCodeFor(BinaryQuad quad) {
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
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.UnaryQuad)
	 */
	public void generateCodeFor(UnaryQuad quad) {
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
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.VarReturnQuad)
	 */
	public void generateCodeFor(VarReturnQuad quad) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.VoidReturnQuad)
	 */
	public void generateCodeFor(VoidReturnQuad quad) {
		// TODO Auto-generated method stub

	}

}
