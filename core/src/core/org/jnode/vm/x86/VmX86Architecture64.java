/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.vm.VmProcessor;
import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.compiler.IMTCompiler;
import org.jnode.vm.x86.compiler.X86IMTCompiler64;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmX86Architecture64 extends VmX86Architecture {

	/** Size of an object reference */
	public static final int SLOT_SIZE = 8;

    /** The IMT compiler */
    private final X86IMTCompiler64 imtCompiler;

	/**
	 * Initialize this instance.
	 */
	public VmX86Architecture64() {
		this("L1A");
	}

	/**
	 * Initialize this instance.
	 * 
	 * @param compiler
	 */
	public VmX86Architecture64(String compiler) {
		super(compiler);
		this.imtCompiler = new X86IMTCompiler64();
	}

	/**
	 * @see org.jnode.vm.VmArchitecture#createProcessor(int,
	 *      org.jnode.vm.classmgr.VmStatics)
	 */
	public final VmProcessor createProcessor(int id, VmStatics statics) {
		return new VmX86Processor64(id, this, statics, null);
	}

	/**
	 * @see org.jnode.vm.VmArchitecture#getIMTCompiler()
	 */
	public final IMTCompiler getIMTCompiler() {
		return imtCompiler;
	}

	/**
	 * @see org.jnode.vm.VmArchitecture#getName()
	 */
	public final String getName() {
		return "x86_64";
	}

	/**
	 * @see org.jnode.vm.VmArchitecture#getReferenceSize()
	 */
	public final int getReferenceSize() {
		return SLOT_SIZE;
	}
}
