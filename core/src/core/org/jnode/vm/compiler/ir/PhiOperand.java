/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import java.util.List;

import org.jnode.util.BootableArrayList;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class PhiOperand extends Operand {
	private BootableArrayList sources;
	private int varIndex;

	public PhiOperand() {
		this(UNKNOWN);
	}

	/**
	 * @param type
	 */
	public PhiOperand(int type) {
		super(type);
		sources = new BootableArrayList(); 
	}
	
	public void addSource(Variable source) {
		sources.add(source);
		int type = getType();
		if (type == UNKNOWN) {
			setType(source.getType());
			Variable v = (Variable) source;
			varIndex = v.getIndex();
		} else if (type != source.getType()) {
			throw new AssertionError("phi operand source types don't match");
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("phi(");
		int n = sources.size();
		for (int i=0; i<n; i+=1) {
			sb.append(sources.get(i).toString());
			if (i < n-1) {
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * @return
	 */
	public List getSources() {
		return sources;
	}
	
	public int getIndex() {
		return varIndex;
	}

	public Operand simplify() {
		int n = sources.size();
		if (n == 1) {
			Variable var = (Variable) sources.get(0);
			return var.simplify();
		} else {
			Variable first = (Variable) sources.get(0);
			// We can't use var.simplify() here because the result might
			// be a constant, which complicates code generation.
			// sources should contain only Variable instances.
			first = first.simplifyCopy();
			for (int i=1; i<n; i+=1) {
				Variable var = (Variable) sources.get(i);
				var = var.simplifyCopy();
				AssignOP assignOP = var.getAssignOP();
				// This is more efficient than generating phi moves at the end
				// of the block. Basically all phi sources are merged into the
				// first.
				assignOP.setLHS(first);
				
				// This might be in a loop, in which case this variable is live
				// at least until the end of the loop. This looks tricky, but I
				// think it's correct.
				IRBasicBlock block = assignOP.getBasicBlock().getLastPredecessor();
				first.setLastUseAddress(block.getEndPC()-1);
			}
			// This is bold assumption that the first phi source was assigned
			// before any others. I'm not sure if this is always true...
			return first;
		}
	}
}
