/*
 * $Id$
 */
package org.jnode.fs.ext2.cache;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * @author Andras Nagy
 */
public class BlockCache extends LinkedHashMap{
	//at most MAX_SIZE blocks fit in the cache
	static final int MAX_SIZE = 50;
	private final Logger log = Logger.getLogger(getClass());
	private Vector cacheListeners;
	
	public BlockCache(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor, true);
		cacheListeners = new Vector();
	}
	
	public void addCacheListener(CacheListener listener) {
		cacheListeners.addElement(listener);
	}
		
	/*private boolean containsKey(Integer key) {
		boolean result = super.containsKey(key);
		if(result)
			log.debug("CACHE HIT, size:"+size());
		else
			log.debug("CACHE MISS");
		return result;
	}*/
	
	protected boolean removeEldestEntry(Map.Entry eldest) {
		if(size()>MAX_SIZE) {
			try{
				((Block)eldest.getValue()).flush();
				//notify the listeners
				CacheEvent event = new CacheEvent(eldest.getValue(),CacheEvent.REMOVED);
				Iterator listeners=cacheListeners.iterator();
				while(listeners.hasNext()) {
					((CacheListener)listeners.next()).elementRemoved(event);
				}
			}catch(IOException e) {
				log.error("Exception when flushing a block from the cache", e); 	
			}
			return true;
		} else
			return false;
	}
}
