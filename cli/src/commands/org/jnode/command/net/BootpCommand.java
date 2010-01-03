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

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceAPI;
import org.jnode.driver.net.NetworkException;
import org.jnode.naming.InitialNaming;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;

/**
 * @author epr
 */
public class BootpCommand extends AbstractCommand {

    private static final String help_device = "";
    private static final String help_super = "Configure a network interface using BOOTP";
    private static final String fmt_config = "Trying to configure %s...%n";
    
    private final DeviceArgument argDevice;

    public BootpCommand() {
        super(help_super);
        argDevice = new DeviceArgument("device", Argument.MANDATORY, help_device, NetDeviceAPI.class);
        registerArguments(argDevice);
    }

    public static void main(String[] args) throws Exception {
        new BootpCommand().execute(args);
    }

    public void execute() throws NameNotFoundException, NetworkException {
        final Device dev = argDevice.getValue();
        getOutput().getPrintWriter().format(fmt_config, dev.getId());
        final IPv4ConfigurationService cfg = InitialNaming.lookup(IPv4ConfigurationService.NAME);
        cfg.configureDeviceBootp(dev, true);
    }

}
