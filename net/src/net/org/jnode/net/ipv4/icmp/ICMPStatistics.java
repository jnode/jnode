/*
 * $Id$
 */
package org.jnode.net.ipv4.icmp;

import org.jnode.util.Statistic;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class ICMPStatistics implements Statistics {

	protected final Statistic badlen = new Statistic("badlen", "#received packets with datalength larger then packet");
	protected final Statistic badsum = new Statistic("badsum", "#received packets with checksum error");
	protected final Statistic ipackets = new Statistic("ipackets", "total #received packets");
	protected final Statistic opackets = new Statistic("opackets", "total #output packets");

	private final Statistic[] list = new Statistic[] {
		badlen,
		badsum,
		ipackets,
		opackets 
	};

	/**
	 * Gets all statistics
	 */
	public Statistic[] getStatistics() {
		return list;
	}

}
