/*
 * $Id$
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
