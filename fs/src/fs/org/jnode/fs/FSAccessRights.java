/*
 * $Id$
 */
package org.jnode.fs;

import java.io.IOException;
import java.security.Principal;

/**
 * This interface described the accessright for a given FSEntry.
 * 
 * @author epr
 */
public interface FSAccessRights extends FSObject {
	
	/**
	 * Gets the owner of the entry.
	 * @throws IOException
	 */
	public Principal getOwner()
	throws IOException;

}
