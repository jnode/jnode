/*
 * $Id: CompletionException.java 3561 2007-10-20 10:29:35Z lsantha $
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
 
package org.jnode.shell.syntax;

import org.jnode.shell.ShellException;

/**
 * This exception is thrown when the supplied command line arguments are not
 * compatible with the chosen syntax.
 * 
 * @author crawley@jnode.org
 */
public class CommandSyntaxException extends ShellException {
	private static final long serialVersionUID = 1L;

	public CommandSyntaxException(String message) {
		super(message);
	}

	public CommandSyntaxException(String message, Throwable cause) {
		super(message, cause);
	}
}
