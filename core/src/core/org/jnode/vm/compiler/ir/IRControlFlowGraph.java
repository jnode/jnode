/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import java.util.Iterator;
import java.util.List;

import org.jnode.util.BootableArrayList;
import org.jnode.util.ObjectArrayIterator;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.compiler.ir.quad.AssignQuad;
import org.jnode.vm.compiler.ir.quad.PhiAssignQuad;
import org.jnode.vm.compiler.ir.quad.Quad;

/**
 * @author Madhu Siddalingaiah
 * 
 */
//TODO simpify to use existing CFG from l1

public class IRControlFlowGraph {
	private SSAStack[] renumberArray;
	private final IRBasicBlock[] bblocks;
	private BootableArrayList postOrderList;
	private IRBasicBlock startBlock;

	/**
	 * Create a new instance
	 * @param bytecode
	 */
	public IRControlFlowGraph(VmByteCode bytecode) {
		// First determine the basic blocks
		final IRBasicBlockFinder bbf = new IRBasicBlockFinder();
		BytecodeParser.parse(bytecode, bbf);
		this.bblocks = bbf.createBasicBlocks();
		startBlock = bblocks[0];
		renumberArray = new SSAStack[bytecode.getMaxStack() + bytecode.getNoLocals()];
		computeDominance();
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

	public void computeDominance() {
		postOrderList = new BootableArrayList();
		startBlock.computePostOrder(postOrderList);
		doComputeDominance();
		computeDominanceFrontier();
		computeDominatedBlocks();
	}

	/*
		for all nodes, b // initialize the dominators array
			doms[b] = Undefined
		doms[start_node] = start_node
		Changed = true
		while (Changed)
			Changed = false
			for all nodes, b, in reverse postorder (except start node)
				new_idom = first (processed) predecessor of b // (pick one)
				for all other predecessors, p, of b
					if doms[p] != Undefined // i.e., if doms[p] already calculated
						new_idom = intersect(p, new_idom)
			if doms[b] != new_idom
				doms[b] = new_idom
				Changed = true
	*/
	private void doComputeDominance() {
		// This is critical, must be done in reverse postorder
		startBlock.setIDominator(startBlock);
		boolean changed = true;
		while (changed) {
			changed = false;
			int i = postOrderList.size() - 1; // skip startBlock
			while (i >= 0) {
				IRBasicBlock b = (IRBasicBlock) postOrderList.get(i--);
				if (b == startBlock) {
					continue;
				}
				Iterator ip = b.getPredecessors().iterator();
				if (!ip.hasNext()) {
					throw new AssertionError(b + " has no predecessors!");
				}
				IRBasicBlock newIdom = (IRBasicBlock) ip.next();
				while (newIdom.getIDominator() == null && ip.hasNext()) {
					newIdom = (IRBasicBlock) ip.next();
				}
				if (newIdom.getIDominator() == null) {
					throw new AssertionError(newIdom + " has no dominator!");
				}
				while (ip.hasNext()) {
					IRBasicBlock p = (IRBasicBlock) ip.next();
					if (p.getIDominator() != null) {
						newIdom = intersect(p, newIdom);
					}
				}
				if (b.getIDominator() != newIdom) {
					b.setIDominator(newIdom);
					changed = true;
				}
			}
		}
		startBlock.setIDominator(null);
	}

	/**
	 *
	 * @param p
	 * @param newIdom
	 * @return
	 */
	/*
		function intersect(b1, b2) returns node
			finger1 = b1
			finger2 = b2
			while (finger1 != finger2)
				while (finger1 < finger2)
					finger1 = doms[finger1]
				while (finger2 < finger1)
					finger2 = doms[finger2]
			return finger1
	*/
	private IRBasicBlock intersect(IRBasicBlock b1, IRBasicBlock b2) {
		while (b1 != b2) {
			while (b1.getPostOrderNumber() < b2.getPostOrderNumber()) {
				b1 = b1.getIDominator();
			}
			while (b2.getPostOrderNumber() < b1.getPostOrderNumber()) {
				b2 = b2.getIDominator();
			}
		}
		return b1;
	}

	/*
		for all nodes, b
			if the number of predecessors of b >= 2
				for all predecessors, p, of b
					runner = p
					while runner != doms[b]
						add b to runner’s dominance frontier set
						runner = doms[runner]
	*/
	private void computeDominanceFrontier() {
		int n = postOrderList.size();
		for (int i=0; i<n; i+=1) {
			IRBasicBlock b = (IRBasicBlock) postOrderList.get(i);
			List predList = b.getPredecessors();
			if (predList.size() >= 2) {
				Iterator it = predList.iterator();
				while (it.hasNext()) {
					IRBasicBlock runner = (IRBasicBlock) it.next();
					while (runner != b.getIDominator()) {
						runner.addDominanceFrontier(b);
						runner = runner.getIDominator();
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	private void computeDominatedBlocks() {
		int n = postOrderList.size();
		for (int i=0; i<n; i+=1) {
			IRBasicBlock b = (IRBasicBlock) postOrderList.get(i);
			IRBasicBlock idom = b.getIDominator();
			if (idom != null) {
				idom.addDominatedBlock(b);
				idom = idom.getIDominator();
			}
		}
	}

	/**
	 * 
	 */
	public void constructSSA() {
		placePhiFunctions();
		renameVariables(startBlock);
	}

	private void placePhiFunctions() {
		System.out.println("\nplacePhiFunctions");
		for (int i=0; i<bblocks.length; i+=1) {
			IRBasicBlock b = bblocks[i];
			BootableArrayList defList = b.getDefList();
			for (int j=0; j<defList.size(); j+=1) {
				Variable def = (Variable) defList.get(j);
				List df = b.getDominanceFrontier();
				Iterator it = df.iterator();
				while (it.hasNext()) {
					IRBasicBlock dfb = (IRBasicBlock) it.next();
					dfb.add(new PhiAssignQuad(dfb, def.getIndex()));
				}
			}
		}
	}
	
	/**
	 * @param block
	 */
	private void renameVariables(IRBasicBlock block) {
		System.out.println("\nrenameVariables block: " + block);
		doRenameVariables(block);
		Iterator it = block.getSuccessors().iterator();
		while (it.hasNext()) {
			IRBasicBlock b = (IRBasicBlock) it.next();
			rewritePhiParams(b);
		}

		it = block.getDominatedBlocks().iterator();
		while (it.hasNext()) {
			IRBasicBlock b = (IRBasicBlock) it.next();
			if (b != block) {
				renameVariables(b);
			}
		}
		popVariables(block);
	}

	/**
	 * @param block
	 */
	private void doRenameVariables(IRBasicBlock block) {
		System.out.println("\ndoRenameVariables block: " + block);
		Iterator it = block.getQuads().iterator();
		while (it.hasNext()) {
			Quad q = (Quad) it.next();
			Operand[] refs = q.getReferencedOps();
			if (refs != null) {
				int n = refs.length;
				for (int i=0; i<n; i+=1) {
					SSAStack st = getStack(refs[i]);
					if (st != null) {
						refs[i] = st.peek();
					}
				}
			}
			if (q instanceof AssignQuad) {
				AssignQuad aq = (AssignQuad) q;
				SSAStack st = getStack(aq.getLHS());
				aq.setLHS(st.getNewVariable());
			}
		}
	}

	/**
	 * @param block
	 */
	private void rewritePhiParams(IRBasicBlock block) {
		if (block == null) {
			return;
		}
		System.out.println("\nrewritePhiParams block: " + block);
		Iterator it = block.getQuads().iterator();
		while (it.hasNext()) {
			Quad q = (Quad) it.next();
			if (q instanceof PhiAssignQuad) {
				PhiAssignQuad aq = (PhiAssignQuad) q;
				SSAStack st = getStack(aq.getLHS());
				Variable var = st.peek();
				if (var != null) {
					PhiOperand phi = (PhiOperand) aq.getPhiOperand();
					phi.addSource(var);
				}
			}
		}
	}

	/**
	 * @param block
	 */
	private void popVariables(IRBasicBlock block) {
		System.out.println("\npopVariables block: " + block);
		Iterator it = block.getQuads().iterator();
		while (it.hasNext()) {
			Quad q = (Quad) it.next();
			if (q instanceof AssignQuad) {
				AssignQuad aq = (AssignQuad) q;
				SSAStack st = getStack(aq.getLHS());
				st.pop();
			}
		}
	}

	/**
	 * @param operand
	 * @return
	 */
	private SSAStack getStack(Operand operand) {
		if (operand instanceof Variable) {
			return getStack((Variable) operand);
		}
		return null;
	}

	private SSAStack getStack(Variable var) {
		int index = var.getIndex();
		SSAStack st = renumberArray[index];
		if (st == null) {
			System.out.println("new SSAStack for " + var);
			st = new SSAStack(var);
			renumberArray[index] = st;
		}
		return st;
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
			Iterator it = bb.getSuccessors().iterator();
			while (it.hasNext()) {
				IRBasicBlock succ = (IRBasicBlock) it.next();
				sb.append("\n    ");
				sb.append(succ);
			}
			sb.append("\n  idom: ");
			sb.append(bb.getIDominator());
			sb.append("\n  DF:");
			it = bb.getDominanceFrontier().iterator();
			while (it.hasNext()) {
				IRBasicBlock dfb = (IRBasicBlock) it.next();
				sb.append(" ");
				sb.append(dfb);
			}
			sb.append("\n\n");
		}
		return sb.toString();
	}
}
