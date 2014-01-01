/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.command.net;

import static org.jnode.net.ethernet.EthernetConstants.ETH_P_IP;

import java.io.PrintWriter;

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
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * @author epr
 */
public class RouteCommand extends AbstractCommand {
    
    private static final String help_add = "if set, add a route";
    private static final String help_del = "if set, remove a route";
    private static final String help_target = "the target network";
    private static final String help_gateway = "the gateway name or IP address";
    private static final String help_device = "the device to connect to the foreign network";
    private static final String help_super = "Manage the IPv4 network routing table";
    private static final String str_table = "Routing table:";
    
    private final FlagArgument argAdd;
    private final FlagArgument argDel;
    private final IPv4AddressArgument argTarget;
    private final IPv4HostArgument argGateway;
    private final DeviceArgument argDevice;

    public RouteCommand() {
        super(help_super);
        argAdd     = new FlagArgument("add", Argument.OPTIONAL, help_add);
        argDel     = new FlagArgument("del", Argument.OPTIONAL, help_del);
        argTarget  = new IPv4AddressArgument("target", Argument.OPTIONAL, help_target);
        argGateway = new IPv4HostArgument("gateway", Argument.OPTIONAL, help_gateway);
        argDevice  = new DeviceArgument("device", Argument.OPTIONAL, help_device);
        registerArguments(argAdd, argDel, argDevice, argGateway, argTarget);
    }

    public static void main(String[] args) throws Exception {
        new RouteCommand().execute(args);
    }

    public void execute() throws NoSuchProtocolException, NetworkException, NameNotFoundException {
        final IPv4NetworkLayer ipNL =
                (IPv4NetworkLayer) NetUtils.getNLM().getNetworkLayer(ETH_P_IP);
        final IPv4Address target = argTarget.getValue();
        final IPv4Address gateway = argGateway.getValue();
        final Device device = argDevice.getValue();
        final IPv4ConfigurationService cfg = InitialNaming.lookup(IPv4ConfigurationService.NAME);

        if (argAdd.isSet()) {
            cfg.addRoute(target, gateway, device, true);
        } else if (argDel.isSet()) {
            cfg.deleteRoute(target, gateway, device);
        } else {
            PrintWriter out = getOutput().getPrintWriter();
            out.println(str_table);
            out.println(ipNL.getRoutingTable());
        }
    }
}
