/*
 * $Id$
 */
package org.jnode.vm.compiler;

import org.jnode.vm.bytecode.BasicBlock;
import org.jnode.vm.bytecode.BytecodeVisitor;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class CompilerBytecodeVisitor extends BytecodeVisitor {

	/**
	 * The given basic block is about to start.
	 */
	public abstract void startBasicBlock(BasicBlock bb);
	
	/**
	 * The started basic block has finished.
	 */
	public abstract void endBasicBlock();
}
