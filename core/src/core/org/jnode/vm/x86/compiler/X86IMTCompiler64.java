/*
 * $Id$
 */
package org.jnode.vm.x86.compiler;

import org.jnode.assembler.ObjectResolver;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.compiler.CompiledIMT;
import org.jnode.vm.compiler.IMTCompiler;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class X86IMTCompiler64 extends IMTCompiler implements
		X86CompilerConstants {

	public CompiledIMT compile(ObjectResolver resolver, Object[] imt,
			boolean[] imtCollisions) {
		// TODO implement me
		return new X86CompiledIMT(new byte[4]);
	}

	public void initialize(VmClassLoader loader) {
		// TODO Auto-generated method stub

	}
}
