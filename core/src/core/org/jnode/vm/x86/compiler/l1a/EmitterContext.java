/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.vm.x86.compiler.X86CompilerHelper;

/**
 * @author Patrik Reali
 *
 * Store information needed by each emitter class
 */
final class EmitterContext {

	/** The output stream */
	private final AbstractX86Stream os;
	
	/** Helper class */
	private final X86CompilerHelper helper;
	
	private X86RegisterPool pool;

	/**
	 * Create a new context
	 */
	EmitterContext(AbstractX86Stream os, X86CompilerHelper helper) {
		this.os = os;
		this.helper = helper;
		pool = new X86RegisterPool();
	}

	/**
	 * Return the current emitter's stream
	 * @return
	 */
	AbstractX86Stream getStream() {
		return os;
	}
	
	/**
	 * return the current emitter's helper
	 * 
	 * @return
	 */
	X86CompilerHelper getHelper() {
		return helper;
	}
	
	/**
	 * return the current emitter's register pool
	 * 
	 * @return
	 */
	X86RegisterPool getPool() {
		return pool;
	}
}
