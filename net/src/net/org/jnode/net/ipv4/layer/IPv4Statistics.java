/*
 * $Id$
 */
package org.jnode.net.ipv4.layer;

import org.jnode.util.Statistic;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class IPv4Statistics implements Statistics {

	protected final Statistic badhlen = new Statistic("badhlen", "#received packets with invalid IP header length");
	protected final Statistic badlen = new Statistic("badlen", "#received packets with datalength larger then packet");
	protected final Statistic badsum = new Statistic("badsum", "#received packets with checksum error");
	protected final Statistic badvers = new Statistic("badvers", "#received packets with an IP version other then 4");
	protected final Statistic fragments = new Statistic("fragments", "total #received fragments");
	protected final Statistic ipackets = new Statistic("ipackets", "total #received packets");
	protected final Statistic noproto = new Statistic("noproto", "#received packets with an unknown or unsupported protocol");
	protected final Statistic nodevaddr = new Statistic("nodevaddr", "#received packets not delivered because device address was not set");
	protected final Statistic opackets = new Statistic("opackets", "total #output packets");
	protected final Statistic reassembled = new Statistic("reassembled", "#datagrams reassembled");

	private final Statistic[] list = new Statistic[] {
		badhlen,
		badlen,
		badsum,
		fragments,
		ipackets,
		noproto,
		nodevaddr,
		opackets 
	};

	/**
	 * Gets all statistics
	 */
	public Statistic[] getStatistics() {
		return list;
	}

}
