/*
 * $Id$
 */
package org.jnode.system;

/**
 * Type independent resource interface.
 * 
 * Every resource in the system is owned by an owner and must be
 * released after it has been used.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface Resource {
	
	/**
	 * Gets the owner of this resource.
	 * @return The owner
	 */
	public ResourceOwner getOwner();
	
	/**
	 * Give up this resource. After this method has been called, the resource
	 * cannot be used anymore.
	 */
	public void release();
	
	/**
	 * Gets the parent resource if any.
	 * @return The parent resource, or null if this resource has no parent.
	 */
	public Resource getParent();

}
