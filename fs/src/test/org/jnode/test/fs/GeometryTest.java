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
 
package org.jnode.test.fs;

import junit.framework.TestCase;

import org.jnode.fs.util.CHS;
import org.jnode.fs.util.Geometry;

/**
 * @author epr
 */
public class GeometryTest extends TestCase {

	/**
	 * Constructor for GeometryTest.
	 * @param arg0
	 */
	public GeometryTest(String arg0) {
		super(arg0);
	}

	public void testLogSec2CHS() {
		Geometry geom = new Geometry(64, 8, 40);
		long max = geom.getTotalSectors();
		for (long logSec = 0; logSec < max; logSec++) {
			CHS chs = geom.getCHS(logSec);
			//System.out.println("logSec=" + logSec + ", chs=" + chs);
			assertEquals("logSec=" + logSec, logSec, geom.getLogicalSector(chs));
		}
	}

}
