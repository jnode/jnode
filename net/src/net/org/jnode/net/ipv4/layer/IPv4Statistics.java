/*
 * $Id$
 */
package org.jnode.net.ipv4.layer;

import org.jnode.util.Counter;
import org.jnode.util.Statistic;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class IPv4Statistics implements Statistics {

	protected final Counter badhlen = new Counter("badhlen", "#received packets with invalid IP header length");
	protected final Counter badlen = new Counter("badlen", "#received packets with datalength larger then packet");
	protected final Counter badsum = new Counter("badsum", "#received packets with checksum error");
	protected final Counter badvers = new Counter("badvers", "#received packets with an IP version other then 4");
	protected final Counter fragments = new Counter("fragments", "total #received fragments");
	protected final Counter ipackets = new Counter("ipackets", "total #received packets");
	protected final Counter noproto = new Counter("noproto", "#received packets with an unknown or unsupported protocol");
	protected final Counter nodevaddr = new Counter("nodevaddr", "#received packets not delivered because device address was not set");
	protected final Counter opackets = new Counter("opackets", "total #output packets");
	protected final Counter reassembled = new Counter("reassembled", "#datagrams reassembled");

	private final Counter[] list = new Counter[] {
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
