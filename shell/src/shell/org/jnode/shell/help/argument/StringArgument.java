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

import org.jnode.driver.console.CompletionInfo;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Argument;

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

	public void complete(CompletionInfo completion, String partial) {
		String result = CommandLine.doEscape(partial, true);	// force quote
		result = result.substring(0, result.length() - 1);	// remove ending quote
		completion.addCompletion(result, true);
	}
	
}
