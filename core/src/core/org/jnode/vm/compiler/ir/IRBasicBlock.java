/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import java.util.List;

import org.jnode.util.BootableArrayList;
import org.jnode.vm.bytecode.BasicBlock;
import org.jnode.vm.compiler.ir.quad.AssignQuad;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class IRBasicBlock extends BasicBlock {
	private BootableArrayList predecessors;
	private BootableArrayList successors;
	private Variable[] variables;
	private BootableArrayList phiReferences;

	// This is useful for finding the bottom of a loop.
	private IRBasicBlock lastPredecessor;

	/**
	 * @param startPC
	 * @param endPC
	 * @param startOfExceptionHandler
	 */
	public IRBasicBlock(
		int startPC,
		int endPC,
		boolean startOfExceptionHandler) {
		super(startPC, endPC, startOfExceptionHandler);
		this.predecessors = new BootableArrayList();
		this.successors = new BootableArrayList();
		this.phiReferences = new BootableArrayList();
	}

	/**
	 * @param address
	 */
	public IRBasicBlock(int address) {
		this(address, -1, false);
	}

	/**
	 * @return a List containing BasicBlocks that may precede this block
	 */
	public List getPredecessors() {
		return predecessors;
	}

	/**
	 * @return a List containing BasicBlocks that may succeed this block
	 */
	public List getSuccessors() {
		return successors;
	}

	final void addPredecessor(IRBasicBlock block) {
		if (!this.predecessors.contains(block)) {
			this.predecessors.add(block);
			if (lastPredecessor == null ||
				block.getEndPC() > lastPredecessor.getEndPC()) {
				lastPredecessor = block;
			}
		}
		// Closure
		List preds = block.getPredecessors();
		int n = preds.size();
		for (int i=0; i<n; i+=1) {
			IRBasicBlock pred = (IRBasicBlock) preds.get(i);
			if (!predecessors.contains(pred)) {
				addPredecessor(pred);
			}
			pred.addSuccessor(this);
		}
	}

	// TODO This isn't complete, but it's good enough for now...
	final void addSuccessor(IRBasicBlock block) {
		if (!this.successors.contains(block)) {
			this.successors.add(block);
		}
	}

	/**
	 * @return
	 */
	public Variable[] getVariables() {
		return variables;
	}

	/**
	 * @param variables
	 */
	public void setVariables(Variable[] variables) {
		this.variables = variables;
	}

	/**
	 * @param phi
	 */
	public void addPhiReference(PhiOperand phi) {
		phiReferences.add(phi);
	}

	/**
	 * 
	 */
	public void resolvePhiReferences() {
		int n = phiReferences.size();
		for (int i=0; i<n; i+=1) {
			PhiOperand phi = (PhiOperand) phiReferences.get(i);
			Variable op = variables[phi.getIndex()];
			AssignQuad assignQuad = op.getAssignQuad();
			IRBasicBlock block = assignQuad.getBasicBlock();
			if (block == this) {
				assignQuad.setDeadCode(false);
				phi.addSource(op);
			}
		}
	}

	/**
	 * @return
	 */
	public IRBasicBlock getLastPredecessor() {
		return lastPredecessor;
	}
}
