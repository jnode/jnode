/*
 * $Id$
 */
package org.jnode.vm.compiler;

import org.jnode.assembler.ObjectResolver;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.classmgr.VmClassLoader;

/**
 * Class used to compile an IMT into a jump table suitable for a specific
 * architecture.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class IMTCompiler extends VmSystemObject {

    /**
     * Initialize this compiler
     * 
     * @param loader
     */
    public abstract void initialize(VmClassLoader loader);

	/**
	 * Compile the given IMT.
	 * 
	 * @param imt
	 * @param imtCollisions
	 */
	public abstract CompiledIMT compile(ObjectResolver resolver, Object[] imt, boolean[] imtCollisions);
}