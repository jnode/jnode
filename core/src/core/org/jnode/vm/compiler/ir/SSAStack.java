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

import org.jnode.util.BootableArrayList;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class SSAStack {
	private BootableArrayList stack;
	private int count;
	private Variable variable;

	/**
	 * 
	 */
	public SSAStack(Variable variable) {
		this.variable = variable;
		count = 0;
		stack = new BootableArrayList();
	}
	
	public Variable peek() {
		int n = stack.size();
		// This deals with cases where there are excessive phis (unpruned SSA)
		if (n <= 0) {
			return null;
		}
		Variable var = (Variable) stack.get(n - 1);
		return var;
	}
	
	public Variable getNewVariable() {
		count += 1;
		Variable var = (Variable) variable.clone();
		var.setSSAValue(count);
		stack.add(var);
		return var;
	}
	
	public Variable pop() {
		Variable var = (Variable) stack.remove(stack.size() - 1);
		return var;
	}
}
