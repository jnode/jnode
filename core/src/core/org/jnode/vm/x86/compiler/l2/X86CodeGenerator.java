/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l2;

import org.jnode.util.BootableHashMap;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.RegisterPool;
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
	private BootableHashMap variableMap;
	private final RegisterPool registerPool;

	/**
	 * @param variableMap
	 */
	public X86CodeGenerator() {
		CodeGenerator.setCodeGenerator(this);
		this.registerPool = new X86RegisterPool();
	}

	public void setVariableMap(BootableHashMap variableMap) {
		this.variableMap = variableMap;
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

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.CodeGenerator#generateCodeFor(org.jnode.vm.compiler.ir.quad.BinaryQuad)
	 */
	public void generateCodeFor(BinaryQuad quad) {
		// TODO Auto-generated method stub
		
	}
}
