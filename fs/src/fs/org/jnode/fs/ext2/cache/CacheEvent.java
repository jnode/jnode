package org.jnode.fs.ext2.cache;

import java.util.EventObject;

/**
 * Event used to notify a CacheListener about events occuring to the cache
 * (currently used only when an element is removed from the cache).
 * 
 * @author Andras Nagy
 */
public class CacheEvent extends EventObject{
	public static final int REMOVED=0;
	private int eventType;
	public CacheEvent(Object source) {
		super(source);
	}

	public CacheEvent(Object source, int type) {
		super(source);
		this.eventType = type;
	}
	/**
	 * Returns the eventType.
	 * @return int
	 */
	public int getEventType() {
		return eventType;
	}

}
