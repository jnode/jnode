
/*
 * Created on Nov 25, 2004 4:16:36 PM
 *
 * Copyright (c) 2004 Madhu Siddalingaiah
 * All rights reserved
 * mailto:madhu@madhu.com
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
		System.out.println("peek(): " + variable);
		int n = stack.size();
		// TODO don't know if this right, but it avoids exceptions for certain
		// rewritePhiParams calls
		if (n <= 0) {
			return null;
		}
		Variable var = (Variable) stack.get(n - 1);
		System.out.println("peek(): " + var);
		return var;
	}
	
	public Variable getNewVariable() {
		count += 1;
		System.out.println("getNewVariable(): " + variable);
		Variable var = (Variable) variable.clone();
		var.setSSAValue(count);
		stack.add(var);
		System.out.println("getNewVariable(): " + var);
		return var;
	}
	
	public Variable pop() {
		Variable var = (Variable) stack.remove(stack.size() - 1);
		System.out.println("pop(): " + var);
		return var;
	}
}
