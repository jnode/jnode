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
 
package org.jnode.shell.help;

import org.jnode.shell.CommandLine;

/**
 * @author qades
 */
public class StringArgument extends Argument {

	public StringArgument(String name, String description, boolean multi) {
		super(name, description, multi);
	}

	public StringArgument(String name, String description) {
		super(name, description);
	}

	public String complete(String partial) {
		String result = CommandLine.escape(partial, true);	// force quote
		return result.substring(0, result.length() - 1);	// remove ending quote
	}
	
}
