/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.net.command;

import org.jnode.net.ipv4.IPv4Address;
import org.jnode.net.ipv4.util.ResolverImpl;
import org.jnode.shell.help.*;


public class ResolverCommand
{
  private static final String FUNC_ADD = "add";
  private static final String FUNC_DEL = "del";


  private static final OptionArgument ARG_FUNCTION =
    new OptionArgument(
      "function",
      "the function to perform",
      new OptionArgument.Option[] { new OptionArgument.Option(FUNC_ADD, "add a dns server"), new OptionArgument.Option(FUNC_DEL, "delete a dns server")});

  private static final HostArgument ARG_DNSSERVER = new HostArgument("dns server", "the dns server IP address");


  public static Help.Info HELP_INFO =
    new Help.Info(
      "resolver",
      new Syntax[] {
        new Syntax("Print the dns servers"),
        new Syntax(
          "Add or remove a dns server",
          new Parameter[] {
            new Parameter(ARG_FUNCTION, Parameter.MANDATORY),
            new Parameter(ARG_DNSSERVER, Parameter.MANDATORY)
          })
  });


  public static void main(String[] args) throws Exception {
    ParsedArguments cmdLine = HELP_INFO.parse(args);

    if (cmdLine.size() == 0)
    {
      System.out.println("DNS servers");
      ResolverImpl.printDnsServers();
    }
    else
    {
      String func = ARG_FUNCTION.getValue(cmdLine);
      IPv4Address server = ARG_DNSSERVER.getAddress(cmdLine);

      if (FUNC_ADD.equals(func))
      {
        ResolverImpl.addDnsServer(server);
      }
      else if (FUNC_DEL.equals(func))
      {
        ResolverImpl.removeDnsServer(server);
      }
    }

    System.out.println();
  }
}
