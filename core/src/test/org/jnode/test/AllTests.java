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
 
package org.jnode.test;

import org.apache.log4j.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author epr
 */
public class AllTests {

	final static Logger log = Logger.getLogger(AllTests.class);
	public static Test suite() {
		log.info("Starting test");
		TestSuite suite = new TestSuite("Test for org.jnode.test");
		//$JUnit-BEGIN$
		suite.addTest(new TestSuite(TestVmClass.class));
		suite.addTest(new TestSuite(SignatureTest.class));
		//$JUnit-END$
		return suite;
	}
}
