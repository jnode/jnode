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
import java.util.Collection;

import org.jnode.driver.net.NetworkException;
import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.util.ResolverImpl;
import org.jnode.net.syntax.IPv4AddressArgument;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * This command class manages the DNS resolver.
 * 
 * @author hagar-wize
 */
public class ResolverCommand extends AbstractCommand {
    // FIXME this should not be restricted to IPv4 addresses.
    private final FlagArgument FLAG_ADD =
            new FlagArgument("add", Argument.OPTIONAL, "if set, add a DNS server");

    private final FlagArgument FLAG_DEL =
            new FlagArgument("del", Argument.OPTIONAL, "if set, remove a DNS server");

    private final IPv4AddressArgument ARG_DNS_SERVER =
            new IPv4AddressArgument("server", Argument.OPTIONAL, "the DNS server's hostname or IP address");

    public ResolverCommand() {
        super("Manage JNode's DNS resolver");
        registerArguments(FLAG_ADD, FLAG_DEL, ARG_DNS_SERVER);
    }

    public static void main(String[] args) throws Exception {
        new ResolverCommand().execute(args);
    }

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err)
        throws NetworkException {
        IPv4Address server = ARG_DNS_SERVER.getValue();
        if (FLAG_ADD.isSet()) {
            // Add a DNS server
            ResolverImpl.addDnsServer(server);
        } else if (FLAG_DEL.isSet()) {
            // Remove a DNS server
            ResolverImpl.removeDnsServer(server);
        } else {
            // List the DNS servers that the resolver uses
            Collection<String> resolvers = ResolverImpl.getDnsServers();
            if (resolvers.size() == 0) {
                out.println("No DNS servers found.");
            } else {
                out.println("DNS servers");
                for (String dnsServer : resolvers) {
                    out.println(dnsServer);
                }
            }
        }
    }
}
