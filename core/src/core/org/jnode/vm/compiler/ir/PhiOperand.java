/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

import java.util.ArrayList;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class PhiOperand extends Operand {
	private ArrayList sources;

	/**
	 * @param type
	 */
	public PhiOperand(int type) {
		super(type);
		sources = new ArrayList(); 
	}
	
	public PhiOperand(Operand op) {
		this(op.getType());
		addSource(op);
	}
	
	public void addSource(Operand source) {
		sources.add(source);
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
	public ArrayList getSources() {
		return sources;
	}
}
