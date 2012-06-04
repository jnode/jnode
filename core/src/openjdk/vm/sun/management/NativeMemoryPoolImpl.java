/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package sun.management;

import java.lang.management.MemoryUsage;
import java.lang.management.MemoryManagerMXBean;

/**
 * @see sun.management.MemoryPoolImpl
 */
class NativeMemoryPoolImpl {
    /**
     * @see sun.management.MemoryPoolImpl#getUsage0()
     */
    private static MemoryUsage getUsage0(MemoryPoolImpl instance) {
        //todo implement it
        return null;
    }
    /**
     * @see sun.management.MemoryPoolImpl#getPeakUsage0()
     */
    private static MemoryUsage getPeakUsage0(MemoryPoolImpl instance) {
        //todo implement it
        return null;
    }
    /**
     * @see sun.management.MemoryPoolImpl#getCollectionUsage0()
     */
    private static MemoryUsage getCollectionUsage0(MemoryPoolImpl instance) {
        //todo implement it
        return null;
    }
    /**
     * @see sun.management.MemoryPoolImpl#setUsageThreshold0(long, long)
     */
    private static void setUsageThreshold0(MemoryPoolImpl instance, long arg1, long arg2) {
        //todo implement it
    }
    /**
     * @see sun.management.MemoryPoolImpl#setCollectionThreshold0(long, long)
     */
    private static void setCollectionThreshold0(MemoryPoolImpl instance, long arg1, long arg2) {
        //todo implement it
    }
    /**
     * @see sun.management.MemoryPoolImpl#resetPeakUsage0()
     */
    private static void resetPeakUsage0(MemoryPoolImpl instance) {
        //todo implement it
    }
    /**
     * @see sun.management.MemoryPoolImpl#getMemoryManagers0()
     */
    private static MemoryManagerMXBean[] getMemoryManagers0(MemoryPoolImpl instance) {
        //todo implement it
        return new MemoryManagerMXBean[0];
    }
    /**
     * @see sun.management.MemoryPoolImpl#setPoolUsageSensor(sun.management.Sensor)
     */
    private static void setPoolUsageSensor(MemoryPoolImpl instance, Sensor arg1) {
        //todo implement it
    }
    /**
     * @see sun.management.MemoryPoolImpl#setPoolCollectionSensor(sun.management.Sensor)
     */
    private static void setPoolCollectionSensor(MemoryPoolImpl instance, Sensor arg1) {
        //todo implement it
    }
}
