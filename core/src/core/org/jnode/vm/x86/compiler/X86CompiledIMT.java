/*
 * $Id$
 */
package org.jnode.vm.x86.compiler;

import org.jnode.vm.compiler.CompiledIMT;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class X86CompiledIMT extends CompiledIMT {

	final byte[] code;

	/**
	 * Initialize this instance.
	 * @param code
	 */
	public X86CompiledIMT(byte[] code) {
		this.code = code;
	}

	/**
	 * @see org.jnode.vm.compiler.CompiledIMT#getIMTAddress()
	 */
	public Object getIMTAddress() {
		return code;
	}
}