/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.fs.fat;

/**
 * <description>
 * 
 * @author epr
 */
public class GrubBootSector extends BootSector {

	/**
	 * Constructor for GrubBootSector.
	 * @param size
	 */
	public GrubBootSector(int size) {
		super(size);
	}

	/**
	 * Constructor for GrubBootSector.
	 * @param src
	 */
	public GrubBootSector(byte[] src) {
		super(src);
	}
	
	/**
	 * Gets the first sector of stage2 
	 * @return long
	 */
	public long getStage2Sector() {
		return get32(0x44);
	}

	/**
	 * Sets the first sector of stage2
	 */
	public void setStage2Sector(long v) {
		set32(0x44, v);
	}

}
