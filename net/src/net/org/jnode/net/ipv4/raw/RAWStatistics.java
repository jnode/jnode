/*
 * $Id$
 */
package org.jnode.net.ipv4.raw;

import org.jnode.util.Statistic;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class RAWStatistics implements Statistics {

	/** The list of statistics */
	private final Statistic[] list = new Statistic[] { 
	};

	/**
	 * Gets all statistics
	 */
	public Statistic[] getStatistics() {
		return list;
	}

}
