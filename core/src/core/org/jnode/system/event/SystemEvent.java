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
 
package org.jnode.system.event;

/**
 * @author epr
 */
public class SystemEvent {

	protected final int id;
	private long time;
	private boolean consumed;

	/**
	 * Create a new system event
	 * @param id
	 * @param time
	 */	
	public SystemEvent(int id, long time) {
		this.id = id;
		this.time = time;
	}
	
	/**
	 * Create a new system event
	 */
	public SystemEvent() {
		this(-1, System.currentTimeMillis());
	}
	
	/**
	 * Create a new system event
	 * @param id
	 */
	public SystemEvent(int id) {
		this(id, System.currentTimeMillis());
	}
	
	/**
	 * @return int
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return long
	 */
	public long getTime() {
		return time;
	}
	
	/**
	 * Mark this event as being consumed.
	 */
	public void consume() {
		consumed = true;
	}
	
	/**
	 * Has this event been consumed.
	 * @return boolean
	 */
	public boolean isConsumed() {
		return consumed;
	}
}
