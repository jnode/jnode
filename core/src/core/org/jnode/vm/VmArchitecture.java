/*
 * $Id$
 */
package org.jnode.vm;

import org.jnode.vm.compiler.Compiler;

/**
 * Class describing a specific system architecture.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmArchitecture extends VmSystemObject {

	/**
	 * Gets the name of this architecture.
	 * 
	 * @return Name
	 */
	public abstract String getName();

	/**
	 * Gets the size in bytes of an object reference.
	 * 
	 * @return Reference size
	 */
	public abstract int getReferenceSize();

	/**
	 * Gets the stackreader for this architecture.
	 * 
	 * @return Stack reader
	 */
	public abstract VmStackReader getStackReader();

	/**
	 * Gets all compilers for this architecture.
	 * 
	 * @return The compilers, sorted by optimization level, from least optimizations to most
	 *         optimizations.
	 */
	public abstract Compiler[] getCompilers();
}
