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
 
package org.jnode.driver.system.cmos;


/**
 * @author epr
 */
public interface CMOSService {
	
	/** Name used to bind this service in the InitialNaming namespace */
	public static final Class<CMOSService> NAME = CMOSService.class;//"system/cmos";

	/**
	 * Gets the value of a CMOS register with a given nr. 
	 * @param regnr
	 * @return The register value
	 */
	public int getRegister(int regnr);

}
