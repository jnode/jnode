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
 
package org.jnode.shell.alias;

import org.jnode.shell.ShellException;

/**
 * @author epr
 */
public class NoSuchAliasException extends ShellException {

	/**
	 * 
	 */
	public NoSuchAliasException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoSuchAliasException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NoSuchAliasException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param s
	 */
	public NoSuchAliasException(String s) {
		super(s);
	}
}
