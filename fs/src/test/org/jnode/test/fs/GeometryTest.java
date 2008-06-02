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

package org.jnode.test.fs;

import junit.framework.TestCase;
import org.jnode.driver.block.CHS;
import org.jnode.driver.block.Geometry;
import org.jnode.driver.block.Geometry.GeometryException;

/**
 * @author epr
 */
public class GeometryTest extends TestCase {

    /**
     * Constructor for GeometryTest.
     *
     * @param arg0
     */
    public GeometryTest(String arg0) {
        super(arg0);
    }

    public void testLogSec2CHS() throws GeometryException {
        Geometry geom = new Geometry(64, 8, 40);
        long max = geom.getTotalSectors();
        for (long logSec = 0; logSec < max; logSec++) {
            CHS chs = geom.getCHS(logSec);
            //System.out.println("logSec=" + logSec + ", chs=" + chs);
            assertEquals("logSec=" + logSec, logSec, geom.getLogicalSector(chs));
        }
    }

}
