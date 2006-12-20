/*
 * $Id: HostArgument.java 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.net.help.argument;

import org.jnode.net.ipv4.IPv4Address;
import org.jnode.shell.help.Argument;
import org.jnode.shell.help.ParsedArguments;

/**
 * @author qades
 */
public class HostArgument extends Argument {

	public HostArgument(String name, String description, boolean multi) {
		super(name, description, multi);
	}

	public HostArgument(String name, String description) {
		super(name, description);
	}

	// here the specific command line completion would be implemented

	public IPv4Address getAddress(ParsedArguments args) {
		String value = getValue(args);
		if( value == null )
			return null;
		return new IPv4Address(value);
	}
}
