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
public class NetworkArgument extends Argument {

	public NetworkArgument(String name, String description, boolean multi) {
		super(name, description, multi);
	}

	public NetworkArgument(String name, String description) {
		super(name, description);
	}

	// here the specific command line completion would be implemented

	public IPv4Address getAddress(ParsedArguments args) {
		String value = getValue(args);
		if( "default".equals(value) )
			value = "0.0.0.0";
		return new IPv4Address(value);
	}
}
