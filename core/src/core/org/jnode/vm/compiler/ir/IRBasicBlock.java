/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import java.util.Iterator;
import java.util.List;

import org.jnode.util.BootableArrayList;
import org.jnode.vm.compiler.ir.quad.PhiAssignQuad;
import org.jnode.vm.compiler.ir.quad.Quad;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class IRBasicBlock {
	private boolean startOfExceptionHandler;
	private int endPC;
	private int startPC;
	private Variable[] variables;
	private static int blockIndex = 1;
	private static int postOrderCounter = 1;
	
	private String name;

	/**
	 * This blocks immediate dominator (up the dom tree)
	 */
	private IRBasicBlock idominator;
	
	/**
	 * The blocks immediately dominated (down the dom tree)
	 */
	private List dominatedBlocks;
	
	/**
	 * The blocks in the dominance frontier of this block
	 */
	private List dominanceFrontier;
	
	/**
	 * The immediate successors of this block
	 */
	private List successors;
	
	/**
	 * The immediate predecessors of this block
	 */
	private List predecessors;
	
	/**
	 * Depth from the root (root = 0, next lower = 1 etc., unknown = -1)
	 */
	private int postOrderNumber;
	
	/**
	 * The quads in this block
	 */
	private List quads;
	private BootableArrayList defList;
	
	// The stack offset at the beginning of this block
	// In some cases, e.g. terniary operators, this is important
	private int stackOffset;

	/**
	 * @param startPC
	 * @param endPC
	 * @param startOfExceptionHandler
	 */
	public IRBasicBlock(
		int startPC,
		int endPC,
		boolean startOfExceptionHandler) {

		this.startPC = startPC;
		this.endPC = endPC;
		this.startOfExceptionHandler = startOfExceptionHandler;

		this.stackOffset = -1; // We'll check to make sure this is set

		this.name = "B" + startPC;
		predecessors = new BootableArrayList();
		successors = new BootableArrayList();
		dominatedBlocks = new BootableArrayList();
		postOrderNumber = -1;
		dominanceFrontier = new BootableArrayList();
		quads = new BootableArrayList();
		defList = new BootableArrayList();
	}

	/**
	 * @param address
	 */
	public IRBasicBlock(int address) {
		this(address, -1, false);
	}

	private void addPredecessor(IRBasicBlock p) {
		if (!predecessors.contains(p)) {
			predecessors.add(p);
		}
	}

	public void addDominatedBlock(IRBasicBlock db) {
		if (!dominatedBlocks.contains(db)) {
			dominatedBlocks.add(db);
		}
	}

	/**
	 * @return
	 */
	public Variable[] getVariables() {
		if (variables == null) {
			if (variables == null) {
				variables = idominator.getVariables();
			}
		}
		if (variables == null) {
			throw new AssertionError("variables are null!");
		}
		return variables;
	}

	/**
	 * @param variables
	 */
	public void setVariables(Variable[] variables) {
		this.variables = variables;
	}

	/**
	 * @return
	 */
	public int getStackOffset() {
		if (stackOffset < 0) {
			stackOffset = idominator.getStackOffset();
		}
		return stackOffset;
	}

	/**
	 * @param initialOffset
	 */
	public void setStackOffset(int initialOffset) {
		stackOffset = initialOffset;
	}

	/**
	 * @param quad
	 */
	public void add(Quad q) {
		addDef(q);
		quads.add(q);
	}

	public void add(PhiAssignQuad paq) {
		if (!quads.contains(paq)) {
			System.out.println("adding PhiAssignQuad " + paq + " to " + this);
			addDef(paq);
			quads.add(0, paq);
		}
	}

	private void addDef(Quad q) {
		Operand def = q.getDefinedOp();
		if (def instanceof Variable &&
			!defList.contains(def)) {
			
			defList.add(def);
		}
	}

	/**
	 * @return
	 */
	public List getSuccessors() {
		return successors;
	}

	/**
	 * @return
	 */
	public IRBasicBlock getIDominator() {
		return idominator;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public List getPredecessors() {
		return predecessors;
	}

	/**
	 * @param block
	 */
	public void addSuccessor(IRBasicBlock block) {
		if (!successors.contains(block)) {
			successors.add(block);
			block.addPredecessor(this);
		}
	}

	/**
	 * @param block
	 */
	public void setIDominator(IRBasicBlock block) {
		idominator = block;
		if (block != null) {
			block.addDominatedBlock(this);
		}
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @return
	 */
	public int getPostOrderNumber() {
		return postOrderNumber;
	}
	
	/**
	 * @param i
	 */
	public void setPostOrderNumber(int i) {
		postOrderNumber = i;
	}

	public void computePostOrder(List list) {
		setPostOrderNumber(0);
		Iterator it = successors.iterator();
		while (it.hasNext()) {
			IRBasicBlock b = (IRBasicBlock) it.next();
			if (b.getPostOrderNumber() < 0) {
				b.computePostOrder(list);
			}
		}
		setPostOrderNumber(postOrderCounter++);
		list.add(this);
	}

	public void printDomTree() {
		System.out.print(getName() + " doms:");
		IRBasicBlock d = getIDominator();
		while (d != null) {
			System.out.print(" " + d.getName());
			d = d.getIDominator();
		}
		System.out.println();
	}
	
	public void printPredecessors() {
		System.out.print(getName() + " preds:");
		Iterator it = predecessors.iterator();
		while (it.hasNext()) {
			IRBasicBlock b = (IRBasicBlock) it.next();
			System.out.print(" " + b.getName());
		}
		System.out.println();
	}

	/**
	 * @param b
	 */
	public void addDominanceFrontier(IRBasicBlock b) {
		if (!dominanceFrontier.contains(b)) {
			dominanceFrontier.add(b);
		}
	}

	/**
	 * @return
	 */
	public List getDominanceFrontier() {
		return dominanceFrontier;
	}

	/**
	 * @return
	 */
	public List getDominatedBlocks() {
		return dominatedBlocks;
	}

	/**
	 * @return
	 */
	public List getQuads() {
		return quads;
	}

	/**
	 * @return
	 */
	public BootableArrayList getDefList() {
		return defList;
	}

	/**
	 * @return
	 */
	public int getEndPC() {
		return endPC;
	}

	/**
	 * @return
	 */
	public int getStartPC() {
		return startPC;
	}

	/**
	 * @param i
	 */
	public void setEndPC(int i) {
		endPC = i;
	}

	/**
	 * @param i
	 */
	public void setStartPC(int i) {
		startPC = i;
	}

	/**
	 * @return
	 */
	public boolean isStartOfExceptionHandler() {
		return startOfExceptionHandler;
	}

	/**
	 * @param b
	 */
	public void setStartOfExceptionHandler(boolean b) {
		startOfExceptionHandler = b;
	}

	/**
	 * @param pc
	 * @return
	 */
	public boolean contains(int pc) {
		return ((pc >= startPC) && (pc < endPC));
	}
	
	public String toString() {
		return name;
	}
}
