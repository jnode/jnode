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

/**
 * JSR 107 - JCACHE implementation 
 * based on javadoc from 
 * <a href="https://jsr-107-interest.dev.java.net/javadoc/javax/cache/package-summary.html">
 * jsr-107-interest</a> (last update 01/19/2005)<br><br>
 * 
 * @author Fabien DUMINY (fduminy at users.sourceforge.net)
 *
 */
public interface CacheStatistics
{
    public static final int STATISTICS_ACCURACY_NONE = 0;
    public static final int STATISTICS_ACCURACY_BEST_EFFORT = 1;
    public static final int STATISTICS_ACCURACY_GUARANTEED = 2;
    
    /**
     * 
     * @return
     */
    public int getStatisticsAccuracy();
    
    /**
     * 
     * @return
     */
    public int getObjectCount();
    
    /**
     * 
     * @return
     */
    public int getCacheHits();
    
    /**
     * 
     * @return
     */
    public int getCacheMisses();
    
    /**
     * 
     *
     */
    public void clearStatistics();
}
