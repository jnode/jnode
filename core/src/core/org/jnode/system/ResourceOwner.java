/*
 * $Id$
 */
package org.jnode.system;

/**
 * Owner of a resource.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ResourceOwner {
    
    public static final ResourceOwner SYSTEM = new SimpleResourceOwner("SYSTEM");
	
	/**
	 * Gets a short description of this owner.
	 * @return The short description
	 */
	public String getShortDescription();

}
