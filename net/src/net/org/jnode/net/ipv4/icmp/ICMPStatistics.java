/*
 * $Id$
 */
package org.jnode.net.ipv4.icmp;

import org.jnode.util.Counter;
import org.jnode.util.Statistic;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class ICMPStatistics implements Statistics {

	protected final Counter badlen = new Counter ("badlen", "#received packets with datalength larger then packet");
	protected final Counter badsum = new Counter ("badsum", "#received packets with checksum error");
	protected final Counter ipackets = new Counter ("ipackets", "total #received packets");
	protected final Counter opackets = new Counter ("opackets", "total #output packets");

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
