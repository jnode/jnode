/**
 * $Id$
 */
package org.jnode.vm.bytecode;

import java.util.Iterator;

import org.jnode.util.ObjectArrayIterator;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.classmgr.VmByteCode;

/**
 * Determine and maintain the flow of control through a bytecode method.
 * 
 * @author epr
 */
public class ControlFlowGraph extends VmSystemObject {
	
	private final BasicBlock[] bblocks;

	/**
	 * Create a new instance
	 * @param bytecode
	 */
	public ControlFlowGraph(VmByteCode bytecode) {
		
		// First determine the basic blocks
		final BasicBlockFinder bbf = new BasicBlockFinder();
		BytecodeParser.parse(bytecode, bbf);
		this.bblocks = bbf.createBasicBlocks();
		
	}
	
	/**
	 * Create an iterator to iterate over all basic blocks.
	 * @return An iterator that will return instances of BasicBlock.
	 */
	public Iterator basicBlockIterator() {
		return new ObjectArrayIterator(bblocks);
	}
	
	/**
	 * Gets the basic block that contains the given address.
	 * @param pc
	 * @return
	 */
	public BasicBlock getBasicBlock(int pc) {
		final int max = bblocks.length;
		for (int i = 0; i < max; i++) {
			final BasicBlock bb = bblocks[i];
			if (bb.contains(pc)) {
				return bb;
			}
		}
		return null;
	}
}
