/*
 * $Id$
 */
package org.jnode.vm.compiler;

import org.jnode.vm.bytecode.BytecodeFlags;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.ControlFlowGraph;
import org.jnode.vm.classmgr.VmByteCode;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CompilerBytecodeParser extends BytecodeParser implements BytecodeFlags {

	private final ControlFlowGraph cfg;
	private final CompilerBytecodeVisitor handler;
	
	/**
	 * @param bc
	 * @param handler
	 */
	protected CompilerBytecodeParser(VmByteCode bc, ControlFlowGraph cfg, CompilerBytecodeVisitor handler) {
		super(bc, handler);
		this.cfg = cfg;
		this.handler = handler;
	}
	
	/**
	 * @see org.jnode.vm.bytecode.BytecodeParser#fireStartInstruction(int)
	 */
	protected void fireStartInstruction(int address) {
		final int flags = cfg.getOpcodeFlags(address);
		super.fireStartInstruction(address);
		if ((flags & F_YIELDPOINT) != 0) {
			handler.yieldPoint();
		}
	}

}
