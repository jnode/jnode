/*
 * $Id$
 */
package org.jnode.net.ipv4.udp;

import org.jnode.util.Statistic;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class UDPStatistics implements Statistics {

	/** #received datagrams with datalength larger then packet */
	protected final Statistic badlen = new Statistic("badlen");
	/** #received datagrams with checksum error */
	protected final Statistic badsum = new Statistic("badsum");
	/** #received datagrams not delivered because input socket full */
	protected final Statistic fullsock = new Statistic("fullsock");
	/** #received datagrams with packet shorted then header */
	protected final Statistic hdrops = new Statistic("hdrops");
	/** total #received datagrams */
	protected final Statistic ipackets = new Statistic("ipackets");
	/** #received datagrams with no process on destination port */
	protected final Statistic noport = new Statistic("noport");
	/** #received broadcast/multicast datagrams with no process on destination port */
	protected final Statistic noportbcast = new Statistic("nopoartbcast");
	/** total #output datagrams */
	protected final Statistic opackets = new Statistic("opackets");

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
