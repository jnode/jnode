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

import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.naming.InitialNaming;
import org.jnode.net.ProtocolAddressInfo;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;

/**
 * @author markhale
 * @author crawley@jnode.org
 */
public class DhcpCommand extends AbstractCommand {

    private final DeviceArgument ARG_DEVICE = new DeviceArgument(
            "device", Argument.MANDATORY, "the network interface device to be configured", NetDeviceAPI.class);

    public DhcpCommand() {
        super("Configure a network interface using DHCP");
        registerArguments(ARG_DEVICE);
    }

    public static void main(String[] args) throws Exception {
        new DhcpCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) 
        throws DeviceNotFoundException, NameNotFoundException, ApiNotFoundException, 
        UnknownHostException, NetworkException {
        final Device dev = ARG_DEVICE.getValue();

        // The DHCP network configuration process will attempt to configure the DNS.  This will only work if
        // the IP address 127.0.0.1 is bound to the loopback network interface.  And if there isn't, JNode's
        // network layer is left in a state that will require a reboot to unjam it (AFAIK).  
        //
        // So, check that loopback is correctly bound ...
        Device loopback = ((DeviceManager) InitialNaming.lookup(DeviceManager.NAME)).getDevice("loopback");
        NetDeviceAPI api = (NetDeviceAPI) loopback.getAPI(NetDeviceAPI.class);
        ProtocolAddressInfo info = api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
        if (info == null || !info.contains(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}))) {
            err.println("The loopback network device is not bound to IP address 127.0.0.1");
            err.println("Run 'ifconfig loopback 127.0.0.1 255.255.255.255' to fix this.");
            exit(1);
        }

        // Now it should be safe to do the DHCP configuration.
        out.println("Configuring network device " + dev.getId() + "...");
        final IPv4ConfigurationService cfg = 
            (IPv4ConfigurationService) InitialNaming.lookup(IPv4ConfigurationService.NAME);
        cfg.configureDeviceDhcp(dev, true);
    }
}
