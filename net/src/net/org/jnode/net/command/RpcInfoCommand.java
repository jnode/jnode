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
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.OncRpcPortmapClient;
import org.acplt.oncrpc.OncRpcProtocols;
import org.acplt.oncrpc.OncRpcServerIdent;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.HostNameArgument;

/**
 * rpcinfo command makes an RPC call to an RPC server and reports what it finds.
 *
 * @author Andrei Dore
 */
public class RpcInfoCommand extends AbstractCommand {

    private static final String LIST_SERVICES_FORMAT = "%1$10s %2$10s %3$10s %4$10s %5$10s";

    private final HostNameArgument ARG_HOST =
        new HostNameArgument("host", Argument.MANDATORY, "the host to be probed");

    public RpcInfoCommand() {
        super("Probe the portmapper on host, and print a list of all registered RPC programs.");
        registerArguments(ARG_HOST);
    }

    public static void main(String[] args) throws Exception {
        new RpcInfoCommand().execute(args);
    }

    public void execute() {
        OncRpcPortmapClient client = null;
        String hostname = ARG_HOST.getValue();
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();
        try {
            InetAddress host = InetAddress.getByName(hostname);
            client = new OncRpcPortmapClient(host, OncRpcProtocols.ONCRPC_UDP);

            OncRpcServerIdent[] servers = client.listServers();

            out.printf(LIST_SERVICES_FORMAT, "Program", "Version", "Protocol", "Port", "Name");
            out.println();

            for (int i = 0; i < servers.length; i++) {
                OncRpcServerIdent server = servers[i];
                out.printf(LIST_SERVICES_FORMAT, server.program, server.version, 
                        server.protocol == 6 ? "tcp" : "udp",
                                server.port, getName(server.program));
                out.println();
            }
        } catch (OncRpcException e) {
            err.println("Cannot make the rpc call to host " + hostname);
            exit(1);
        } catch (UnknownHostException e) {
            err.println("Unknown hostname " + hostname);
            exit(1);
        } catch (IOException e) {
            err.println("Cannot connect to host " + hostname);
            exit(1);
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (OncRpcException e) {
                    // Ignore exception on close
                }
            }
        }
    }

    private String getName(int program) {
        switch (program) {
            case 100000:
                return "portmapper";
            case 100003:
                return "nfs";
            case 100005:
                return "mountd";
            case 100021:
                return "nlockmgr";
            case 100024:
                return "status";
            default:
                return "unknown service (" + program + ")";
        }
    }
}
