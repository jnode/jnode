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
