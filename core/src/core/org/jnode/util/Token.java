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
 
package org.jnode.util;

/**
 * @author epr
 */
public class Token {

	private boolean locked;
	
	/**
	 * Create a new instance
	 */
	public Token() {
		this.locked = false;
	}
	
	/**
	 * Claim ownership of this token
	 * @param owner
	 * @param timeout
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	public synchronized void claim(Object owner, long timeout) 
	throws InterruptedException, TimeoutException {
		final long start = System.currentTimeMillis();
		while (locked) {
			wait(timeout);
			if (locked) {
				if (System.currentTimeMillis() > start + timeout) {
					throw new TimeoutException();
				}
			}
		}
		this.locked = true;
	}

	/**
	 * Release ownership of this token
	 */
	public synchronized void release() {
		this.locked = false;
		notifyAll();
	}
}
