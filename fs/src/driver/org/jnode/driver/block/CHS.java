/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.block;

/**
 * @author epr
 */
public class CHS {
	
	private final int cylinder;
	private final int head;
	private final int sector;
	
	/**
	 * @param c
	 * @param h
	 * @param s
	 * @throws IllegalArgumentException
	 */
	public CHS(int c, int h, int s) throws IllegalArgumentException {
		if (c < 0) {
			throw new IllegalArgumentException("Cylinder < 0");
		}
		if (h < 0) {
			throw new IllegalArgumentException("Head < 0");
		}
		if (s < 1) {
			throw new IllegalArgumentException("Sector < 1");
		}
		this.cylinder = c;
		this.head = h;
		this.sector = s;
	}

	/**
	 * @return int
	 */
	public int getCylinder() {
		return cylinder;
	}

	/**
	 * @return int
	 */
	public int getHead() {
		return head;
	}

	/**
	 * @return int
	 */
	public int getSector() {
		return sector;
	}
	
	/**
	 * @param obj
	 * @see java.lang.Object#equals(java.lang.Object)
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		if ((obj != null) && (obj instanceof CHS)) {
			CHS o = (CHS)obj;
			return (cylinder == o.cylinder) && (head == o.head) && (sector == o.sector);
		} else {
			return false;
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return String
	 */
	public String toString() {
		return "" + cylinder + "/" + head + "/" + sector;
	}

}
