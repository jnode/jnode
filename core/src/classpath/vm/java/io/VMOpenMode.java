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
 
package java.io;

/**
 * @author epr
 */
public enum VMOpenMode {
	
	/** Open for reading only, open fails if file does not exist */
	READ ("r", true, false),
	/** Open for writing only, file is created if file does not exist */
	WRITE("w", false, true),
	/** Open for reading and writing, file is created if file does not exist */
	READ_WRITE("rw", true, true);

	private final String mode;
	private final boolean read;	
	private final boolean write;	
	private VMOpenMode(String mode, boolean read, boolean write) {
		this.mode = mode;
		this.read = read;
		this.write = write;
	}
	
	public String toString() {
		return mode;
	}

	/**
	 * Open for (at least) read?
	 */
	public boolean canRead() {
		return read;
	}

	/**
	 * Open for (at least) write?
	 */
	public boolean canWrite() {
		return write;
	}
}
