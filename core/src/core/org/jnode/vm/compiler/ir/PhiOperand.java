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
	public Operand simplify() {
		int n = sources.size();
		for (int i=0; i<n; i+=1) {
			Variable src = (Variable) sources.get(i);
			Operand op = src.simplify();
			if (op instanceof StackVariable || op instanceof LocalVariable) {
				sources.set(i, op);
			} else {
				src.getAssignQuad().setDeadCode(false);
			}
		}
		return this;
	}
}
