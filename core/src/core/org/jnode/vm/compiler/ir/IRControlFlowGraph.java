/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import java.util.Iterator;
import java.util.List;

import org.jnode.util.ObjectArrayIterator;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.classmgr.VmByteCode;

/**
 * @author Madhu Siddalingaiah
 * 
 */
//TODO simpify to use existing CFG from l1

public class IRControlFlowGraph {
	private final IRBasicBlock[] bblocks;

	/**
	 * Create a new instance
	 * @param bytecode
	 */
	public IRControlFlowGraph(VmByteCode bytecode) {
		// First determine the basic blocks
		final IRBasicBlockFinder bbf = new IRBasicBlockFinder();
		BytecodeParser.parse(bytecode, bbf);
		this.bblocks = bbf.createBasicBlocks();
	}
	
	/**
	 * Create an iterator to iterate over all basic blocks.
	 * @return An iterator that will return instances of IRBasicBlock.
	 */
	public Iterator basicBlockIterator() {
		return new ObjectArrayIterator(bblocks);
	}
	
	/**
	 * Gets the number of basic blocks in this graph
	 * @return count of basic blocks
	 */
	public int getBasicBlockCount() {
		return bblocks.length;
	}
	
	/**
	 * Gets the basic block that contains the given address.
	 * @param pc
	 * @return
	 */
	public IRBasicBlock getBasicBlock(int pc) {
		final int max = bblocks.length;
		for (int i = 0; i < max; i++) {
			final IRBasicBlock bb = bblocks[i];
			if (bb.contains(pc)) {
				return bb;
			}
		}
		return null;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		Iterator bbi = basicBlockIterator();
		while (bbi.hasNext()) {
			IRBasicBlock bb = (IRBasicBlock) bbi.next();
			sb.append(bb.toString());
			sb.append(":\n  predecessors:");
			List pred = bb.getPredecessors();
			for (int i=0; i<pred.size(); i+=1) {
				sb.append("\n    ");
				sb.append(pred.get(i).toString());
			}
			sb.append("\n  successors:");
			List succ = bb.getSuccessors();
			for (int i=0; i<succ.size(); i+=1) {
				sb.append("\n    ");
				sb.append(succ.get(i).toString());
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}
