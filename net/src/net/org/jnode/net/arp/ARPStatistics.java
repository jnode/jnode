/*
 * $Id$
 */
package org.jnode.net.arp;

import org.jnode.util.Statistic;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class ARPStatistics implements Statistics {

	protected final Statistic badlen = new Statistic("badlen", "#received packets with datalength larger then packet");
	protected final Statistic ipackets = new Statistic("ipackets", "total #received packets");
	protected final Statistic arpreq = new Statistic("arpreq", "#received ARP requests");
	protected final Statistic arpreply = new Statistic("arpreply", "#ARP replies send");
	protected final Statistic rarpreq = new Statistic("rarpreq", "#received RARP requests");
	protected final Statistic rarpreply = new Statistic("rarpreply", "#RARP replies send");
	protected final Statistic opackets = new Statistic("opackets", "total #output packets");

	private final Statistic[] list = new Statistic[] {
		badlen,
		ipackets,
		arpreq,
		arpreply,
		rarpreq,
		rarpreply,
		opackets 
	};

	/**
	 * Gets all statistics
	 */
	public Statistic[] getStatistics() {
		return list;
	}

}
