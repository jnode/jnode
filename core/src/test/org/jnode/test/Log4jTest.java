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
