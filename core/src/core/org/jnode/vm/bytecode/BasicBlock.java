/*
 * $Id$
 */
package org.jnode.vm.bytecode;

import java.util.List;

import org.jnode.util.BootableArrayList;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.compiler.ir.Variable;

/**
 * A Basic block of instructions.
 * 
 * A basic block has 0-n predecessors and 0-m successors. The only exit
 * of a basic block is the last instruction.
 *  
 * @author epr
 * @author Madhu Siddalingaiah
 */
public class BasicBlock extends VmSystemObject {
	private final int startPC;
	private int endPC;
	private boolean startOfExceptionHandler;
	private BootableArrayList predecessors;
	private BootableArrayList successors;
	private Variable[] variables;
	
	/**
	 * Create a new instance
	 * @param startPC The first bytecode address of this block
	 * @param endPC The first bytecode address after this block
	 * @param startOfExceptionHandler
	 */
	public BasicBlock(int startPC, int endPC, boolean startOfExceptionHandler) {
		this.startPC = startPC;
		this.endPC = endPC;
		this.startOfExceptionHandler = startOfExceptionHandler;
		this.predecessors = new BootableArrayList();
		this.successors = new BootableArrayList();
	}

	/**
	 * Create a new instance
	 * @param startPC The first bytecode address of this block
	 */
	public BasicBlock(int startPC) {
		this(startPC, -1, false);
	}

	/**
	 * Gets the first bytecode address after this basic block
	 * @return The end pc
	 */
	public final int getEndPC() {
		return endPC;
	}

	/**
	 * @param endPC The endPC to set.
	 */
	final void setEndPC(int endPC) {
		this.endPC = endPC;
	}

	/**
	 * @param startOfExceptionHandler The startOfExceptionHandler to set.
	 */
	final void setStartOfExceptionHandler(boolean startOfExceptionHandler) {
		this.startOfExceptionHandler = startOfExceptionHandler;
	}

	/**
	 * Gets the first bytecode address of this basic block
	 * @return The start pc
	 */
	public final int getStartPC() {
		return startPC;
	}
	
	/**
	 * Does this block contain a given bytecode address?
	 * @param address
	 * @return boolean
	 */
	public final boolean contains(int address) {
		return ((address >= startPC) && (address < endPC));
	}
	
	/**
	 * @see java.lang.Object#toString()
	 * @return String
	 */
	public String toString() {
		final String str = "" + startPC + "-" + endPC;
		if (startOfExceptionHandler) {
			return str + " (EH)";
		} else {
			return str;
		}
	}
	
	/**
	 * Is this block the start of an exception handler?
	 * @return boolean
	 */
	public final boolean isStartOfExceptionHandler() {
		return startOfExceptionHandler;
	}

	/**
	 * @return an ArrayList containing BasicBlocks that may precede this block
	 */
	public List getPredecessors() {
		return predecessors;
	}

	/**
	 * @return an ArrayList containing BasicBlocks that may succeed this block
	 */
	public List getSuccessors() {
		return successors;
	}

	public void addPredecessor(BasicBlock block) {
		if (!this.predecessors.contains(block)) {
			this.predecessors.add(block);
		}
		// Closure
		List preds = block.getPredecessors();
		int n = preds.size();
		for (int i=0; i<n; i+=1) {
			BasicBlock pred = (BasicBlock) preds.get(i);
			if (!predecessors.contains(pred)) {
				addPredecessor(pred);
			}
			pred.addSuccessor(this);
		}
	}

	// This isn't complete, but it's good enough for now...
	public void addSuccessor(BasicBlock block) {
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
