/*
 * $Id$
 */
package org.jnode.test;

import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Log4jTest {

	public static void main(String[] args) {
		
		//System.getProperties().setProperty("log4j.defaultInitOverride", "true");
		//BasicConfigurator.configure();
		
		final Logger log = Logger.getLogger(Log4jTest.class);
		
		log.debug("This is a debug message");
		log.info("This is a info message");
		log.warn("This is a warn message");
		log.error("This is a error message");
		log.fatal("This is a fatal message");
		
	}
}
