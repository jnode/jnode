/*
 * $Id$
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
