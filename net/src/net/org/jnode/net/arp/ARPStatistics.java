/*
 * $Id$
 */
package org.jnode.net.arp;

import org.jnode.util.Counter;
import org.jnode.util.Statistic;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class ARPStatistics implements Statistics {

	protected final Counter badlen = new Counter("badlen", "#received packets with datalength larger then packet");
	protected final Counter ipackets = new Counter("ipackets", "total #received packets");
	protected final Counter arpreq = new Counter("arpreq", "#received ARP requests");
	protected final Counter arpreply = new Counter("arpreply", "#ARP replies send");
	protected final Counter rarpreq = new Counter("rarpreq", "#received RARP requests");
	protected final Counter rarpreply = new Counter("rarpreply", "#RARP replies send");
	protected final Counter opackets = new Counter("opackets", "total #output packets");

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
