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
 
package org.jnode.util;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ObjectUtils {

	/**
	 * Compare the two objects and return true if they are equal, false otherwise.
	 * If one of the object is null and the other is not, false is returned.
	 * @param a Can be null
	 * @param b Can be null
	 * @return True if (a == b) || a.equals(b)
	 */
	public static boolean equals(Object a, Object b) {
		if (a == b) {
			return true;
		} else if ((a != null) && (b != null)) {
			return a.equals(b);
		} else {
			return false;
		}
	}
}
