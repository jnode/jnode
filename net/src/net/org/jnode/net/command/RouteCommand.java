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
 
package org.jnode.net.command;

import static org.jnode.net.ethernet.EthernetConstants.ETH_P_IP;

import java.io.InputStream;
import java.io.PrintStream;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetworkException;
import org.jnode.naming.InitialNaming;
import org.jnode.net.NoSuchProtocolException;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.jnode.net.ipv4.layer.IPv4NetworkLayer;
import org.jnode.net.syntax.IPv4AddressArgument;
import org.jnode.net.syntax.IPv4HostArgument;
import org.jnode.net.util.NetUtils;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * @author epr
 */
public class RouteCommand extends AbstractCommand {
    private final FlagArgument FLAG_ADD =
        new FlagArgument("add", Argument.OPTIONAL, "if set, add a route");
    
    private final FlagArgument FLAG_DEL =
        new FlagArgument("del", Argument.OPTIONAL, "if set, remove a route");
    
	private final IPv4AddressArgument ARG_TARGET = 
	    new IPv4AddressArgument("target", Argument.OPTIONAL, "the target network");
	
	private final IPv4HostArgument ARG_GATEWAY = 
	    new IPv4HostArgument("gateway", Argument.OPTIONAL, "the gateway name or IP address");
	
	private final DeviceArgument ARG_DEVICE = 
	    new DeviceArgument("device", Argument.OPTIONAL, "the device to connect to the foreign network");
	
	
	public RouteCommand() {
	    super("Manage the IPv4 network routing table");
	    registerArguments(FLAG_ADD, FLAG_DEL, ARG_DEVICE, ARG_GATEWAY, ARG_TARGET);
	}

	public static void main(String[] args) throws Exception {
		new RouteCommand().execute(args);
	}

	public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) 
	throws NoSuchProtocolException, NetworkException, NameNotFoundException 
	{
	    final IPv4NetworkLayer ipNL = 
	        (IPv4NetworkLayer) NetUtils.getNLM().getNetworkLayer(ETH_P_IP);
	    final IPv4Address target = ARG_TARGET.getValue();
	    final IPv4Address gateway = ARG_GATEWAY.getValue();
	    final Device device = ARG_DEVICE.getValue();
	    final IPv4ConfigurationService cfg = 
	        (IPv4ConfigurationService) InitialNaming.lookup(IPv4ConfigurationService.NAME);

	    if (FLAG_ADD.isSet()) {
	        cfg.addRoute(target, gateway, device, true);
	    } 
	    else if (FLAG_DEL.isSet()) {
	        cfg.deleteRoute(target, gateway, device);
	    }
	    else {
	        out.println("Routing table");
	        out.println(ipNL.getRoutingTable());
	    }
	}
}
