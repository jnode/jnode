
/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

/**
 * @author Madhu Siddalingaiah
 * 
 * Describes the location of a variable at runtime.
 * The register allocator defines the location of a variable, this
 * is either a RegisterLocation or a StackLocation.
 */
public abstract class Location {
	private String name;
	
	public Location(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
}
