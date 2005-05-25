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
 
package org.jnode.driver.block.floppy;

import org.jnode.driver.block.Geometry;

/**
 * Parameters of the currently loaded floppy disk
 * @author epr
 */
public class FloppyParameters {
	
	/** Geometry of the current floppy disk */
	private final Geometry geometry;
	private final int gap1Size;
	private final int dataRate;
	private final int spec1;
	private final String name;
	
	/**
	 * Create a new instance
	 * @param geometry
	 * @param gap1Size
	 * @param dataRate
	 * @param spec1
	 * @param name
	 */	
	public FloppyParameters(Geometry geometry, int gap1Size, int dataRate, int spec1, String name) {
		this.geometry = geometry;
		this.gap1Size = gap1Size;
		this.dataRate = dataRate;
		this.spec1 = spec1;
		this.name = name;
	}

	/**
	 * Gets the geometry
	 * @return geometry
	 */
	public Geometry getGeometry() {
		return geometry;
	}
	
	/**
	 * Gets the GAP1 size
	 * @return int
	 */
	public int getGap1Size() {
		return gap1Size;
	}
	
	/**
	 * Gets the data rate
	 * @return int
	 */
	public int getDataRate() {
		return dataRate;
	}

	/**
	 * Gets the SPEC1 value
	 * @return int
	 */
	public int getSpec1() {
		return spec1;
	}

	/**
	 * Gets the name of this format
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return String
	 */
	public String toString() {
		return name;
	}

}
