/*
 * $Id$
 */
package org.jnode.vm;

import java.util.HashSet;

/**
 * @author epr
 */
public class VmProcessClassLoader extends ClassLoader {
	
	/** Sets of classname (String) to skip loading via a parent classloader */
	private final HashSet skipClassNames;
	
	/**
	 * Create a new instance
	 * @param parent
	 */
	public VmProcessClassLoader(ClassLoader parent) {
		super(parent);
		skipClassNames = new HashSet();
		skipClassNames.add("java.lang.System");
		skipClassNames.add("org.jnode.vm.VmProcess");
	}
	
	/**
	 * Create a new instance using the system classloader as parent.
	 */
	public VmProcessClassLoader() {
		this(ClassLoader.getSystemClassLoader());
	}

	/**
	 * @param name
	 * @see java.lang.ClassLoader#skipParentLoader(String)
	 * @return
	 */
	public boolean skipParentLoader(String name) {
		name = name.replace('/', '.');
		return skipClassNames.contains(name);
	}

}
