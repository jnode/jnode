/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
