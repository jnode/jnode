package org.jnode.fs.ext2.cache;

import java.util.Hashtable;
/*
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.fs.ext2.INode;
*/

/**
 * @author Andras Nagy
 */
public class INodeCache extends Hashtable {
	public INodeCache(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}
}

/*
public class INodeCache extends LinkedHashMap {
	//at most MAX_SIZE blocks fit in the cache
	static final int MAX_SIZE = 10;
	private final Logger log = Logger.getLogger(getClass());

	public INodeCache(int initialCapacity, float loadFactor) {
		super(Math.min(MAX_SIZE, initialCapacity), loadFactor, true);
		//super(initialCapacity,loadFactor);
		log.setLevel(Level.DEBUG);
	}

//	public boolean containsKey(Integer key) {
//		boolean result = super.containsKey(key);
//		if(result)
//			log.debug("INODE CACHE HIT, size:"+size());
//		else
//			log.debug("INODE CACHE MISS");
//		return result;
//	}
	
	protected synchronized boolean removeEldestEntry(final Map.Entry eldest) {
		log.debug("INodeCache size: "+size());
		INode eldestINode = (INode)eldest.getValue();
		synchronized(eldestINode) {
			while(eldestINode.isLocked()) {
				//an other thread has indicated that this inode  
				//should not be removed from the cache
				try{
					log.info("** Waiting for an other thread to unlock inode "+eldestINode);
					eldestINode.wait(1000);
				}catch(InterruptedException iex) {
				}
			}
		
			if(size()>MAX_SIZE) {
				try{
					eldestINode.flush();
				}catch(Exception e) {
					log.error("Can't flush inode!",e);
				}
				return true;
			} else
				return false;
		}
	}
}
*/