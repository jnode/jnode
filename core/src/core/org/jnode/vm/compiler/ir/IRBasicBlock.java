/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import java.util.List;

import org.jnode.util.BootableArrayList;
import org.jnode.vm.bytecode.BasicBlock;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class IRBasicBlock extends BasicBlock {
	private BootableArrayList predecessors;
	private BootableArrayList successors;
	private Variable[] variables;

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
	}

	/**
	 * @param address
	 */
	public IRBasicBlock(int address) {
		this(address, -1, false);
	}

	/**
	 * @return an ArrayList containing BasicBlocks that may precede this block
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

	// This isn't complete, but it's good enough for now...
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
}
