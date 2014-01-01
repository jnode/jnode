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

    private static final String help_host = "the host to be probed";
    private static final String help_super = "Probe the portmapper on host, and print a list of all registered RPS " +
                                             "programs.";
    private static final String fmt_list_serv = "%1$10s %2$10s %3$10s %4$10s %5$10s";
    private static final String str_program = "Program";
    private static final String str_version = "Version";
    private static final String str_protocol = "Protocol";
    private static final String str_port = "Port";
    private static final String str_name = "Name";
    private static final String str_portmapper = "portmapper";
    private static final String str_nfs = "nfs";
    private static final String str_mountd = "mountd";
    private static final String str_nlockmgr = "nlockmgr";
    private static final String str_status = "status";
    private static final String str_tcp = "tcp";
    private static final String str_udp = "udp";
    private static final String err_call = "Cannot make the rpc call to host %s%n";
    private static final String err_host = "Unknown hostname %s%n";
    private static final String err_connect = "Cannot connect to host %s%n";
    private static final String fmt_unknown = "unknown service (%d)";

    private final HostNameArgument argHost;

    public RpcInfoCommand() {
        super(help_super);
        argHost = new HostNameArgument("host", Argument.MANDATORY, help_host);
        registerArguments(argHost);
    }

    public static void main(String[] args) throws Exception {
        new RpcInfoCommand().execute(args);
    }
    
    public void execute() {
        OncRpcPortmapClient client = null;
        String hostname = argHost.getValue();
        PrintWriter out = getOutput().getPrintWriter();
        PrintWriter err = getError().getPrintWriter();
        try {
            InetAddress host = InetAddress.getByName(hostname);
            client = new OncRpcPortmapClient(host, OncRpcProtocols.ONCRPC_UDP);

            OncRpcServerIdent[] servers = client.listServers();

            out.printf(fmt_list_serv, str_program, str_version, str_protocol, str_port, str_name);
            out.println();

            for (OncRpcServerIdent server : servers) {
                out.printf(fmt_list_serv, server.program, server.version,
                    server.protocol == 6 ? str_tcp : str_udp,
                    server.port, getName(server.program));
                out.println();
            }
        } catch (OncRpcException e) {
            err.format(err_call, hostname);
            exit(1);
        } catch (UnknownHostException e) {
            err.format(err_host, hostname);
            exit(1);
        } catch (IOException e) {
            err.format(err_connect, hostname);
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
                return str_portmapper;
            case 100003:
                return str_nfs;
            case 100005:
                return str_mountd;
            case 100021:
                return str_nlockmgr;
            case 100024:
                return str_status;
            default:
                return String.format(fmt_unknown, program);
        }
    }
}
