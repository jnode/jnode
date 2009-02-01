/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.net.arp;

import org.jnode.util.Counter;
import org.jnode.util.Statistic;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class ARPStatistics implements Statistics {

    protected final Counter badlen =
            new Counter("badlen", "#received packets with datalength larger then packet");
    protected final Counter ipackets = new Counter("ipackets", "total #received packets");
    protected final Counter arpreq = new Counter("arpreq", "#received ARP requests");
    protected final Counter arpreply = new Counter("arpreply", "#ARP replies send");
    protected final Counter rarpreq = new Counter("rarpreq", "#received RARP requests");
    protected final Counter rarpreply = new Counter("rarpreply", "#RARP replies send");
    protected final Counter opackets = new Counter("opackets", "total #output packets");

    private final Statistic[] list =
            new Statistic[] {badlen, ipackets, arpreq, arpreply, rarpreq, rarpreply, opackets};

    /**
     * Gets all statistics
     */
    public Statistic[] getStatistics() {
        return list;
    }

}
