/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

import java.io.PrintWriter;
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
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;

/**
 * @author markhale
 * @author crawley@jnode.org
 */
public class DhcpCommand extends AbstractCommand {

    private static final String help_device  = "the network interface device to be configured";
    private static final String help_super   = "Configure a network interface using DHCP";
    private static final String err_loopback = "The loopback network device is not bound to IP address 127.0.0.1%n" +
                                               "Run 'ifconfig loopback 127.0.0.1 255.255.255.255' to fix this.%n";
    private static final String fmt_config   = "Configuring network device %s...%n";
    
    private final DeviceArgument argDevice;

    public DhcpCommand() {
        super(help_super);
        argDevice = new DeviceArgument("device", Argument.MANDATORY, help_device, NetDeviceAPI.class);
        registerArguments(argDevice);
    }

    public static void main(String[] args) throws Exception {
        new DhcpCommand().execute(args);
    }

    public void execute() throws DeviceNotFoundException, NameNotFoundException, ApiNotFoundException, 
        UnknownHostException, NetworkException {
        final Device dev = argDevice.getValue();

        // The DHCP network configuration process will attempt to configure the DNS.  This will only work if
        // the IP address 127.0.0.1 is bound to the loopback network interface.  And if there isn't, JNode's
        // network layer is left in a state that will require a reboot to unjam it (AFAIK).  
        //
        // So, check that loopback is correctly bound ...
        Device loopback = (InitialNaming.lookup(DeviceManager.NAME)).getDevice("loopback");
        NetDeviceAPI api = loopback.getAPI(NetDeviceAPI.class);
        ProtocolAddressInfo info = api.getProtocolAddressInfo(EthernetConstants.ETH_P_IP);
        if (info == null || !info.contains(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}))) {
            PrintWriter err = getError().getPrintWriter();
            err.format(err_loopback);
            exit(1);
        }

        // Now it should be safe to do the DHCP configuration.
        getOutput().getPrintWriter().format(fmt_config, dev.getId());
        final IPv4ConfigurationService cfg = InitialNaming.lookup(IPv4ConfigurationService.NAME);
        cfg.configureDeviceDhcp(dev, true);
    }
}
