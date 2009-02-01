/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package javax.cache;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * JSR 107 - JCACHE implementation 
 * based on javadoc from 
 * <a href="https://jsr-107-interest.dev.java.net/javadoc/javax/cache/package-summary.html">
 * jsr-107-interest</a> (last update 01/19/2005)<br><br>
 * 
 * @author Fabien DUMINY (fduminy at users.sourceforge.net)
 *
 */
public class CacheManager
{
    /**
     * Map of Cache(s).
     * Since cache (and CacheManager) are offen accessed by multiple threads,
     * use a synchronized map. (not specified in JSR)
     */
    private Map<String, Cache> caches = new Hashtable<String, Cache>();
    
    /**
     * 
     */
    protected static CacheManager instance;

    /**
     * 
     *
     */
    public CacheManager()
    {
    }

    /**
     * 
     * @return
     */
    static CacheManager getInstance()
    {
        if(instance == null)
        {
            instance = new CacheManager();
        }
        return instance;
    }
       
    /**
     * 
     * @param cacheName
     * @return
     */
    public Cache getCache(String cacheName)
    {
        return caches.get(cacheName);
    }
    
    /**
     * 
     * @param cacheName
     * @param cache
     */
    public void registerCache(String cacheName, Cache cache)
    {
        caches.put(cacheName, cache);
    }
    
    /**
     * 
     * @return
     * @throws CacheException
     */
    public CacheFactory getCacheFactory() throws CacheException
    {
        //TODO I assume the factory is specified through this property (check JSR for confirmation)
        final String property = "javax.cache.CacheFactory";
        final String className = System.getProperty(property);
        if((className == null) || "".equals(className))
        {
            throw new CacheException("The property " + property + 
                                    " must specify a valid class name"); 
        }
        
        try
        {
            Class<?> clazz = Class.forName(className);
            return (CacheFactory) clazz.newInstance();
        }
        catch (ClassNotFoundException e)
        {
            throw new CacheException("Class not found: "+className, e);
        }
        catch (InstantiationException e)
        {
            throw new CacheException("Can't instanciate "+className, e);
        }
        catch (IllegalAccessException e)
        {
            throw new CacheException("Can't access "+className, e);
        } 
    }    
}
