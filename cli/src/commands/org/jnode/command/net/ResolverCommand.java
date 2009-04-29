/*
 * $Id$
 *
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
 
package org.jnode.command.net;

import java.io.PrintWriter;
import java.util.Collection;

import org.jnode.driver.net.NetworkException;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.util.ResolverImpl;
import org.jnode.net.syntax.IPv4AddressArgument;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * This command class manages the DNS resolver.
 * 
 * @author hagar-wize
 */
public class ResolverCommand extends AbstractCommand {

    private static final String help_add = "If set, add a DNS server";
    private static final String help_del = "If set, remove a DNS server";
    private static final String help_server = "the DNS server's hostname or IP address";
    private static final String help_super = "Manage JNode's DNS resolver";
    private static final String str_no_servers = "No DNS servers found.";
    private static final String str_servers = "DNS servers:";
    
    // FIXME this should not be restricted to IPv4 addresses.
    private final FlagArgument argAdd;
    private final FlagArgument argDel;
    private final IPv4AddressArgument argDnsServer;

    public ResolverCommand() {
        super(help_super);
        argAdd       = new FlagArgument("add", Argument.OPTIONAL, help_add);
        argDel       = new FlagArgument("del", Argument.OPTIONAL, help_del);
        argDnsServer = new IPv4AddressArgument("server", Argument.OPTIONAL, help_server);
        registerArguments(argAdd, argDel, argDnsServer);
    }

    public static void main(String[] args) throws Exception {
        new ResolverCommand().execute(args);
    }

    public void execute() throws NetworkException {
        IPv4Address server = argDnsServer.getValue();
        PrintWriter out = getOutput().getPrintWriter();
        if (argAdd.isSet()) {
            // Add a DNS server
            ResolverImpl.addDnsServer(server);
        } else if (argDel.isSet()) {
            // Remove a DNS server
            ResolverImpl.removeDnsServer(server);
        } else {
            // List the DNS servers that the resolver uses
            Collection<String> resolvers = ResolverImpl.getDnsServers();
            if (resolvers.size() == 0) {
                out.println(str_no_servers);
            } else {
                out.println(str_servers);
                for (String dnsServer : resolvers) {
                    out.println(dnsServer);
                }
            }
        }
    }
}
