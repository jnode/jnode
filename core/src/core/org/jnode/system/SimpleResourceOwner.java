/*
 * $Id$
 */
package org.jnode.system;

/**
 * Simple ResourceOwner implementation.
 * 
 * @see org.jnode.system.ResourceOwner
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SimpleResourceOwner implements ResourceOwner {

	private final String name;
	
	public SimpleResourceOwner(String name) {
		this.name = name;
	}

	/**
	 * @see org.jnode.system.ResourceOwner#getShortDescription()
	 * @return The short description
	 */
	public String getShortDescription() {
		return name;
	}
}
