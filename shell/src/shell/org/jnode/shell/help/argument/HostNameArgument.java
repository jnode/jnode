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
 
package org.jnode.shell.help.argument;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jnode.shell.help.Argument;
import org.jnode.shell.help.ParsedArguments;

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
