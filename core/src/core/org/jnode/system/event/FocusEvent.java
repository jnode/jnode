/*
 * $Id$
 */
package org.jnode.system.event;

/**
 * @author epr
 */
public class FocusEvent extends SystemEvent {

	public static final int FOCUS_LOST = 101;
	public static final int FOCUS_GAINED = 102;

	/**
	 * @param id
	 * @param time
	 */
	public FocusEvent(int id, long time) {
		super(id, time);
	}

	/**
	 * @param id
	 */
	public FocusEvent(int id) {
		super(id);
	}
	
	public boolean isFocusLost() {
		return (id == FOCUS_LOST);
	}

	public boolean isFocusGained() {
		return (id == FOCUS_GAINED);
	}

}
