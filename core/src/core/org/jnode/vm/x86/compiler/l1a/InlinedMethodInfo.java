/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.Label;
import org.jnode.vm.bytecode.TypeStack;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InlinedMethodInfo {

	private final VmMethod inlinedMethod;

	private TypeStack exitStack;

	private final Label endOfInlineLabel;

	private TypeStack outerMethodStack;

	/**
	 * Initialize this instance.
	 * 
	 * @param inlinedMethod
	 */
	public InlinedMethodInfo(VmMethod inlinedMethod, Label endOfInlineLabel) {
		this.inlinedMethod = inlinedMethod;
		this.endOfInlineLabel = endOfInlineLabel;
	}

	final void setExitStack(VirtualStack vstack) {
		this.exitStack = vstack.asTypeStack();
	}

	/**
	 * @return Returns the endOfInlineLabel.
	 */
	final Label getEndOfInlineLabel() {
		return endOfInlineLabel;
	}

	/**
	 * Push the stack elements of the outer method stack.
	 * 
	 * @param vstack
	 */
	final void pushOuterMethodStack(ItemFactory ifac, VirtualStack vstack) {
		vstack.pushAll(ifac, outerMethodStack);
	}

	/**
	 * Push the stack elements of the outer method stack and the exit stack.
	 * 
	 * @param vstack
	 */
	final void pushExitStack(ItemFactory ifac, VirtualStack vstack) {
		vstack.reset();
		//vstack.pushAll(outerMethodStack);
		vstack.pushAll(ifac, exitStack);
	}

	/**
	 * @param outerMethodStack
	 *            The outerMethodStack to set.
	 */
	final void setOuterMethodStack(TypeStack outerMethodStack) {
		this.outerMethodStack = outerMethodStack;
	}
}