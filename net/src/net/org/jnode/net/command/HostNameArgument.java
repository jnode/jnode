/*
 * $Id$
 */
package org.jnode.net.command;

import org.jnode.shell.help.Argument;
import org.jnode.shell.help.ParsedArguments;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Martin Hartvig
 */

public class HostNameArgument extends Argument
{

	public HostNameArgument(String _name, String _description, boolean _multi)
  {
		super(_name, _description, _multi);
	}

	public HostNameArgument(String _name, String _description)
  {
		super(_name, _description);
	}


	public InetAddress getAddress(ParsedArguments _parsedArguments) throws UnknownHostException
  {
		String value = getValue(_parsedArguments);

		if (value == null)
			return null;

    return InetAddress.getByName(value);
	}
}
