/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.util.NumberUtils;
import org.vmmagic.unboxed.Word;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmX86Thread64 extends VmX86Thread {

	// State when not running
	volatile Word r8;
	volatile Word r9;
	volatile Word r10;
	volatile Word r11;
	volatile Word r12;
	volatile Word r13;
	volatile Word r14;
	volatile Word r15;

	/**
	 * 
	 */
	public VmX86Thread64() {
		super();
	}

	/**
	 * @param stack
	 */
	public VmX86Thread64(byte[] stack) {
		super(stack);
	}

	/**
	 * @param javaThread
	 */
	public VmX86Thread64(Thread javaThread) {
		super(javaThread);
	}

	protected final int getReferenceSize() {
		return VmX86Architecture64.SLOT_SIZE;
	}

	public String getReadableErrorState() {
		return "RAX " + NumberUtils.hex(exEax.toLong()) + " RBX "
				+ NumberUtils.hex(exEbx.toLong()) + " RCX "
				+ NumberUtils.hex(exEcx.toLong()) + " RDX "
				+ NumberUtils.hex(exEdx.toLong()) + " RSI "
				+ NumberUtils.hex(exEsi.toLong()) + " RDI "
				+ NumberUtils.hex(exEdi.toLong()) + " RSP "
				+ NumberUtils.hex(exEsp.toLong()) + " RIP "
				+ NumberUtils.hex(exEip.toLong()) + " CR2 "
				+ NumberUtils.hex(exCr2.toLong()) + " RFLAGS "
				+ NumberUtils.hex(exEflags.toInt());
	}
}
