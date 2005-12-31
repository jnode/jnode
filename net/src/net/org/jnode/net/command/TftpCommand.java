/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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

import org.jnode.net.ipv4.tftp.TFTPClient;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.OptionArgument;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.Syntax;

/**
 * @author markhale
 */
public class TftpCommand {
	
	private static final OptionArgument.Option[] COMMAND_OPTIONS = new OptionArgument.Option[] {
		new OptionArgument.Option("put", "transfer a file to a server"),
		new OptionArgument.Option("get", "transfer a file from a server")
	};
	private static final HostArgument ARG_SERVER = new HostArgument("hostname", "the hostname of the TFTP server");
	private static final OptionArgument ARG_COMMAND = new OptionArgument("command", "must be either PUT or GET", COMMAND_OPTIONS);
	private static final Argument ARG_FILENAME = new Argument("filename", "the file to transfer");
	
	public static Help.Info HELP_INFO = new Help.Info(
		"tftp",
		new Syntax[] {
			new Syntax(
				"Start the TFTP client as an interactive session",
				new Parameter[] {
					new Parameter(ARG_SERVER, Parameter.OPTIONAL)
				}
			),
			new Syntax(
				"Execute the TFTP client non-interactively",
				new Parameter[] {
					new Parameter(ARG_SERVER, Parameter.MANDATORY),
					new Parameter(ARG_COMMAND, Parameter.MANDATORY),
					new Parameter(ARG_FILENAME, Parameter.MANDATORY)
				}
			)
		}
	);
	
	public static void main(String[] args) throws Exception {
		//ParsedArguments cmdLine = HELP_INFO.parse(args);
		TFTPClient.main(args);
		System.out.println();
	}
}

