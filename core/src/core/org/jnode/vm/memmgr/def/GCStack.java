/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.vm.Uninterruptible;
import org.jnode.vm.VmSystemObject;

/**
 * @author epr
 */
final class GCStack extends VmSystemObject implements Uninterruptible {
	
	/** The default size of a stack */
	public static final int DEFAULT_STACK_SIZE = 512;
	/** The actual stack */
	private final Object[] stack;
	/** The size of the stack (in objects) */
	private final int size;
	/** The stackpointer */
	private int stackPtr;
	/** Has the stack occurred an overflow? */
	private boolean overflow;
	
	/**
	 * Create a new instance
	 */
	public GCStack() {
		this.stack = new Object[DEFAULT_STACK_SIZE];
		this.size = stack.length;
	}
	
	/**
	 * Push a given object on the stack. If an overflow occurs, mark
	 * the overflow and do not push the object.
	 * @param object
	 */
	public void push(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("Cannot push null object");
		}
		if (stackPtr == size) {
			overflow = true;
		} else {
			stack[stackPtr++] = object;
		}
	}
	
	/**
	 * Gets the last pushed object of the stack and remove it from the stack.
	 * @return The object
	 */
	public Object pop() {
		if (stackPtr == 0) {
			return null;
		} else {
			stackPtr--;
			Object result = stack[stackPtr];
			if (result == null) {
				throw new IllegalStateException("Null object found on GCStack");
			}
			stack[stackPtr] = null;
			return result;
		}
	}
	
	/** 
	 * Is this stack empty?
	 * @return boolean
	 */
	public boolean isEmpty() {
		return (stackPtr == 0);
	}
	
	/**
	 * Has a stackoverflow occurred?
	 * @return boolean
	 */
	public final boolean isOverflow() {
		return overflow;
	}
	
	/**
	 * Reset the contents of this stack to its original state
	 */
	public void reset() {
		stackPtr = 0;
		overflow = false;
	}
}
