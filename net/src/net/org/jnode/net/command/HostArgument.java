/*
 * $Id$
 */
package org.jnode.net.command;

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
