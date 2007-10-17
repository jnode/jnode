/*
 * $Id: PingCommand.java 3526 2007-09-23 21:38:55Z lsantha $
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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;

import org.acplt.oncrpc.OncRpcClient;
import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.OncRpcPortmapClient;
import org.acplt.oncrpc.OncRpcProtocols;
import org.acplt.oncrpc.OncRpcServerIdent;
import org.jnode.shell.CommandLine;
import org.jnode.shell.Shell;
import org.jnode.shell.ShellUtils;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.HostNameArgument;

/**
 * @author Andrei Dore
 */
public class RpcInfoCommand {

    private static final String LIST_SERVICES_FORMAT = "%1$10s %2$10s %3$10s %4$10s";

    static final HostNameArgument HOST = new HostNameArgument("host", "host");

    public static Help.Info HELP_INFO = new Help.Info("rpcinfo", new Syntax[] { new Syntax(
	    "Probe the portmapper on host, and print a list of all registered RPC programs.",
	    new Parameter[] { new Parameter(HOST) }) });

    public static void main(String[] args) throws Exception {

	new RpcInfoCommand().execute(new CommandLine(args), System.in, System.out, System.err);

    }

    public RpcInfoCommand() {
    }

    public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {

	ParsedArguments parsedArguments = HELP_INFO.parse(commandLine.toStringArray());

	InetAddress host = HOST.getAddress(parsedArguments);

	listServices(host, out, err);

    }

    private void listServices(InetAddress host, PrintStream out, PrintStream err) {

	OncRpcPortmapClient client = null;
	try {

	    client = new OncRpcPortmapClient(host, OncRpcProtocols.ONCRPC_UDP);

	    OncRpcServerIdent[] servers = client.listServers();

	    out.printf(LIST_SERVICES_FORMAT, "Program", "Version", "Protocol", "Port");
	    out.println();

	    for (int i = 0; i < servers.length; i++) {
		OncRpcServerIdent server = servers[i];

		out.printf(LIST_SERVICES_FORMAT, server.program, server.version, server.protocol == 6 ? "tcp" : "udp",
			server.port);

		out.println();
	    }
	} catch (OncRpcException e) {
	    err.println("Can not make the rpc call to the host " + host.getHostAddress());
	} catch (IOException e) {
	    err.println("Can not connect to  the host " + host.getHostAddress());
	} finally {
	    if (client != null) {
		try {
		    client.close();
		} catch (Exception e) {
		}
	    }
	}

    }
}
