/*
 * $Id$
 */
package org.jnode.net.ipv4.udp;

import org.jnode.util.Counter;
import org.jnode.util.Statistic;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class UDPStatistics implements Statistics {

	/** #received datagrams with datalength larger then packet */
	protected final Counter badlen = new Counter("badlen");
	/** #received datagrams with checksum error */
	protected final Counter badsum = new Counter("badsum");
	/** #received datagrams not delivered because input socket full */
	protected final Counter fullsock = new Counter("fullsock");
	/** #received datagrams with packet shorted then header */
	protected final Counter hdrops = new Counter("hdrops");
	/** total #received datagrams */
	protected final Counter ipackets = new Counter("ipackets");
	/** #received datagrams with no process on destination port */
	protected final Counter noport = new Counter("noport");
	/** #received broadcast/multicast datagrams with no process on destination port */
	protected final Counter noportbcast = new Counter("nopoartbcast");
	/** total #output datagrams */
	protected final Counter opackets = new Counter("opackets");

	/** The list of statistics */
	protected final Statistic[] list = new Statistic[] { 
		badlen,
		badsum,
		fullsock,
		hdrops,
		ipackets,
		noport,
		noportbcast,
		opackets
	};

	/**
	 * Gets all statistics
	 */
	public Statistic[] getStatistics() {
		return list;
	}
}
