/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.net.ipv4.tcp;

import org.jnode.util.Counter;
import org.jnode.util.Statistic;
import org.jnode.util.Statistics;

/**
 * @author epr
 */
public class TCPStatistics implements Statistics {

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
    
    /**
     * #received broadcast/multicast datagrams with no process on destination
     * port
     */
    protected final Counter noportbcast = new Counter("nopoartbcast");
    
    /** total #output datagrams */
    protected final Counter opackets = new Counter("opackets");

    /** The list of statistics */
    protected final Statistic[] list =
            new Statistic[] {badlen, badsum, fullsock, hdrops, ipackets, noport, noportbcast,
                opackets};

    /**
     * Gets all statistics
     */
    public Statistic[] getStatistics() {
        return list;
    }
}
