/*
 * Created on Feb 22, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.jnode.test.fs;

import junit.framework.TestCase;

import org.jnode.util.CHS;
import org.jnode.util.Geometry;

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
