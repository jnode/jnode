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
	static final int MAX_SIZE = 10;
	private final Logger log = Logger.getLogger(getClass());
	private Vector cacheListeners;
	
	public BlockCache(int initialCapacity, float loadFactor) {
		super(Math.min(MAX_SIZE, initialCapacity), loadFactor, true);
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
	
	protected synchronized boolean removeEldestEntry(Map.Entry eldest) {
		log.debug("BlockCache size: "+size());
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
