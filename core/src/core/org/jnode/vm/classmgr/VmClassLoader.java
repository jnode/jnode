/*
 * $Id$
 */

package org.jnode.vm.classmgr;

import java.security.ProtectionDomain;

import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.compiler.CompiledIMT;

/**
 * Interface for the delegation of loading classes with a given name
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmClassLoader extends VmSystemObject {

	/**
	 * Load a class into memory, but do not resolve or compile its method
	 * @param className
	 * @param resolve
	 * @return The loaded class
	 * @throws ClassNotFoundException
	 */
	public abstract VmType loadClass(String className, boolean resolve) throws ClassNotFoundException;

	/**
	 * Find a loaded class with the given name.
	 * @param className
	 * @return The loaded class, or null if not found.
	 */
	public abstract VmType findLoadedClass(String className);
	
	/**
	 * Define a byte-array of class data into a loaded class.
	 * 
	 * @param name
	 * @param data
	 * @param offset
	 * @param length
	 * @param protDomain
	 * @return VmClass
	 */
	public abstract VmType defineClass(String name, byte[] data, int offset, int length, ProtectionDomain protDomain);
	
	/**
	 * Gets the ClassLoader belonging to this loader.
	 * 
	 * @return ClassLoader
	 */
	public abstract ClassLoader asClassLoader();

	/**
	 * Compile the given method
	 * 
	 * @param vmMethod The method to compile
	 * @param optLevel The optimization level
	 * @param enableTestCompilers If true, test compilers at taken into account when selecting the compiler
	 */
	public abstract void compileRuntime(VmMethod vmMethod, int optLevel, boolean enableTestCompilers);
	
    /**
     * Compile the given IMT.
     */
    public abstract CompiledIMT compileIMT(IMTBuilder builder);
    
	/**
	 * Gets the architecture used by this loader.
	 * @return The architecture
	 */
	public abstract VmArchitecture getArchitecture();
	
	/**
	 * Should prepared classes be compiled.
	 * @return boolean
	 */
	public abstract boolean isCompileRequired();
	
	/**
	 * Is this the system classloader.
	 * @return boolean
	 */
	public abstract boolean isSystemClassLoader();
	
	/**
	 * Does a resource with a given name exist in this loader.
	 * @param resName
	 * @return boolean
	 */
	public abstract boolean resourceExists(String resName);

    /**
     * Gets the statics table.
     * 
     * @return The statics table
     */
    public abstract VmStatics getStatics();

    /**
     * Gets the selector map used to create unique method selectors.
     * @return
     */
    protected abstract SelectorMap getSelectorMap();
}