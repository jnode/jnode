/*
 * $Id$
 *
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
public interface EvictionStrategy
{
    /**
     * 
     * @param key
     * @param value
     * @param ttl
     * @return
     */
    public CacheEntry createEntry(Object key, Object value, long ttl);
    
    /**
     * 
     * @param entry
     */
    public void discardEntry(CacheEntry entry);
    
    /**
     * 
     * @param entry
     */
    public void touchEntry(CacheEntry entry);
    
    /**
     * 
     *
     */
    public void clear();
    
    /**
     * 
     * @param cache
     * @return
     */
    public Map evict(Cache cache);
}
