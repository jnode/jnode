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
 
package org.jnode.net.command;

import java.util.Iterator;

import org.jnode.net.arp.ARPNetworkLayer;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.util.NetUtils;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;

/**
 * @author epr
 */
public class ArpCommand {

	static final Parameter PARAM_DELETE = new Parameter("d", "delete the ARP cache", Parameter.MANDATORY);

	public static Help.Info HELP_INFO = new Help.Info("arp", 
		new Syntax[] { 
			new Syntax("Print ARP cache"), 
			new Syntax("Clear ARP cache", new Parameter[] { PARAM_DELETE })
	});

	/**
	 * Execute this command
	 */
	public static void main(String[] args) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		ARPNetworkLayer arp = (ARPNetworkLayer) NetUtils.getNLM().getNetworkLayer(EthernetConstants.ETH_P_ARP);
		if (PARAM_DELETE.isSet(cmdLine)) {
			arp.getCache().clear();
			System.out.println("Cleared the ARP cache");
		} else {
			for (Iterator i = arp.getCache().entries().iterator(); i.hasNext();) {
				System.out.println(i.next());
			}
		}
	}
}
