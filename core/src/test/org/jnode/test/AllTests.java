/*
 * $Id$
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
