/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryUsage;
import com.sun.management.GcInfo;

/**
 * @see sun.management.GcInfoBuilder
 */
class NativeGcInfoBuilder {
    /**
     * @see sun.management.GcInfoBuilder#getNumGcExtAttributes(java.lang.management.GarbageCollectorMXBean)
     */
    private static int getNumGcExtAttributes(GcInfoBuilder instance, GarbageCollectorMXBean arg1) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.GcInfoBuilder#fillGcAttributeInfo(java.lang.management.GarbageCollectorMXBean, int, java.lang.String[], char[], java.lang.String[])
     */
    private static void fillGcAttributeInfo(GcInfoBuilder instance, GarbageCollectorMXBean arg1, int arg2, String[] arg3, char[] arg4, String[] arg5) {
        //todo implement it
    }
    /**
     * @see sun.management.GcInfoBuilder#getLastGcInfo0(java.lang.management.GarbageCollectorMXBean, int, java.lang.Object[], char[], java.lang.management.MemoryUsage[], java.lang.management.MemoryUsage[])
     */
    private static GcInfo getLastGcInfo0(GcInfoBuilder instance, GarbageCollectorMXBean arg1, int arg2, Object[] arg3, char[] arg4, MemoryUsage[] arg5, MemoryUsage[] arg6) {
        //todo implement it
        return null;
    }
}
