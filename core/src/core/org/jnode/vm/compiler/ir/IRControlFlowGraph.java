/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
import org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad;

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
		Variable[] vars = startBlock.getVariables();
		int nvars = vars.length;
		renumberArray = new SSAStack[nvars];
		// Push method arguments on the stack since they are not assigned
		for (int i=0; i<nvars; i+=1) {
			Variable vi = vars[i];
			SSAStack st = getStack(vi);
			if (vi instanceof MethodArgument) {
				st.getNewVariable();
			}
		}
		placePhiFunctions();
		renameVariables(startBlock);
	}

	/**
	 * 
	 */
	public void optimize() {
		for (int i=0; i<bblocks.length; i+=1) {
			IRBasicBlock b = bblocks[i];
			Iterator qi = b.getQuads().iterator();
			while (qi.hasNext()) {
				Quad q = (Quad) qi.next();
				q.doPass2();
			}
		}
	}

	public void deconstrucSSA() {
		BootableArrayList phiQuads = new BootableArrayList();
		for (int i=0; i<bblocks.length; i+=1) {
			IRBasicBlock b = bblocks[i];
			Iterator it = b.getQuads().iterator();
			while (it.hasNext()) {
				Quad q = (Quad) it.next();
				if (q instanceof PhiAssignQuad) {
					phiQuads.add(q);
				} else {
					break;
				}
			}
		}
		int n = phiQuads.size();
		for (int i=0; i<n; i+=1) {
			PhiAssignQuad paq = (PhiAssignQuad) phiQuads.get(i);
			Variable lhs = paq.getLHS();
			IRBasicBlock firstBlock = null;
			VariableRefAssignQuad firstPhiMove = null;
			Iterator pit = paq.getPhiOperand().getSources().iterator();
			while (pit.hasNext()) {
				Variable rhs = (Variable) pit.next();
				IRBasicBlock ab = rhs.getAssignQuad().getBasicBlock();
				VariableRefAssignQuad phiMove;
				phiMove = new VariableRefAssignQuad(0, ab, lhs, rhs);
				ab.add(phiMove);
				if (firstBlock == null || ab.getStartPC() < firstBlock.getStartPC()) {
					firstBlock = ab;
					firstPhiMove = phiMove;
				}
			}
			lhs.setAssignQuad(firstPhiMove);
			paq.setDeadCode(true);
		}
	}

	public void fixupAddresses() {
		int address = 0;
		for (int i=0; i<bblocks.length; i+=1) {
			IRBasicBlock b = bblocks[i];
			b.setStartPC(address);
			Iterator it = b.getQuads().iterator();
			while (it.hasNext()) {
				Quad q = (Quad) it.next();
				q.setAddress(address);
				if (!q.isDeadCode()) {
					address += 1;
				}
			}
			b.setEndPC(address);
		}
	}

	private void placePhiFunctions() {
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
		Iterator it = block.getQuads().iterator();
		while (it.hasNext()) {
			Quad q = (Quad) it.next();
			if (q instanceof PhiAssignQuad) {
				PhiAssignQuad aq = (PhiAssignQuad) q;
				if (!aq.isDeadCode()) {
					SSAStack st = getStack(aq.getLHS());
					Variable var = st.peek();
					// If there was no incoming branch to this phi, I think it's dead...
					if (var != null) {
						PhiOperand phi = (PhiOperand) aq.getPhiOperand();
						phi.addSource(var);
					} else {
						aq.setDeadCode(true);
					}
				}
			}
		}
	}

	/**
	 * @param block
	 */
	private void popVariables(IRBasicBlock block) {
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
