/*
 * $Id$
 */
package org.jnode.fs.ext2.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andras Nagy
 */
public class BlockCache extends LinkedHashMap{
	//at most MAX_SIZE blocks fit in the cache
	static final int MAX_SIZE = 50;
	private static final Logger log = Logger.getLogger(BlockCache.class);
	
	public BlockCache(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor, true);
	}
	
	public boolean containsKey(Integer key) {
		boolean result = super.containsKey(key);
		if(result)
			log.debug("CACHE HIT, size:"+size());
		else
			log.debug("CACHE MISS");
		return result;
	}
	
	protected boolean removeEldestEntry(Map.Entry eldest) {
		if(size()>MAX_SIZE) {
			((Block)eldest.getValue()).flush();
			return true;
		} else
			return false;
	}
}
