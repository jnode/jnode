/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir.quad;

import java.util.List;

import org.jnode.util.BootableHashMap;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.PhiOperand;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Madhu Siddalingaiah
 *
 * Represents an intermediate intruction, commonly called a Quad
 * in the literature.
 * 
 */
public abstract class Quad {
	private int address;
	private boolean deadCode;
	private IRBasicBlock basicBlock;

	public Quad(int address, IRBasicBlock block) {
		this.address = address;
		this.basicBlock = block;
		this.deadCode = false;
	}

	public Operand getOperand(int varIndex) {
		Variable operand = basicBlock.getVariables()[varIndex];
		AssignQuad assignQuad = operand.getAssignQuad();
		if (assignQuad == null) {
			// This was probably a method argument
			return operand;
		}
		List predList = basicBlock.getPredecessors();
		int n = predList.size();

		// if this block has 0 or 1 predecessors or assignment was in this block

		if (n == 0 || n == 1 || assignQuad.getBasicBlock() == basicBlock) {
			return assignQuad.propagate(operand);
		}
		
		// Operand was assigned in more than one preceding blocks, need to merge.
		// This assumes predecessors is an exhaustive list, e.g. includes direct
		// and indirect predecessors. Maybe this should be split into immediate
		// predecessors and all predecessors?

		PhiOperand phi = new PhiOperand();
		int startPC = basicBlock.getStartPC();
		for (int i=0; i<n; i+=1) {
			IRBasicBlock bb = (IRBasicBlock) predList.get(i);
			if (bb.getStartPC() >= startPC) {
				// forward reference, backpatch later
				bb.addPhiReference(phi);
			} else {
				Variable[] variables = bb.getVariables();
				Variable op = variables[varIndex];
				assignQuad = op.getAssignQuad();
				if (assignQuad.getBasicBlock() == bb) {
					assignQuad.setDeadCode(false);
					phi.addSource(op);
				}
			}
		}
		return phi;
	}

	/**
	 * Gets the bytecode address for this operation
	 * 
	 * @return bytecode address for this operation
	 */
	public int getAddress() {
		return address;
	}

	/**
	 * Gets the operand defined by this operation (left side of assignment)
	 * 
	 * @return defined operand or null if none
	 */
	public abstract Operand getDefinedOp();

	/**
	 * Gets all operands used by this operation (right side of assignment)
	 * 
	 * @return array of referenced operands or null if none
	 */
	public abstract Operand[] getReferencedOps();

	/**
	 * Gets all operands that interfere in this operation
	 * This useful in graph coloring register allocators
	 * 
	 * @return array of operands that interfere with each other or null if none
	 */
	public Operand[] getIFOperands() {
		return null;
	}

	/**
	 * @return
	 */
	public boolean isDeadCode() {
		return deadCode;
	}

	/**
	 * @param dead
	 */
	public void setDeadCode(boolean dead) {
		deadCode = dead;
	}

	/**
	 * @return
	 */
	public IRBasicBlock getBasicBlock() {
		return basicBlock;
	}

	public abstract void doPass2(BootableHashMap liveVariables);
	public abstract void generateCode(CodeGenerator cg);
}
