/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import java.util.List;

import org.jnode.util.BootableArrayList;
import org.jnode.vm.compiler.ir.quad.AssignQuad;

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
			Variable v = source;
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

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Operand#getAddressingMode()
	 */
	public int getAddressingMode() {
		Variable first = (Variable) sources.get(0);
		return first.getAddressingMode();
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Operand#simplify()
	 */
	// TODO complete this!
	public Operand simplify() {
		return null;
	}
}
