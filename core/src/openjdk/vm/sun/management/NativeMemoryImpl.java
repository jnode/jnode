/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryUsage;

/**
 * @see sun.management.MemoryImpl
 */
class NativeMemoryImpl {
    /**
     * @see sun.management.MemoryImpl#getMemoryPools0()
     */
    private static MemoryPoolMXBean[] getMemoryPools0() {
        //todo implement it
        return new MemoryPoolMXBean[0];
    }
    /**
     * @see sun.management.MemoryImpl#getMemoryManagers0()
     */
    private static MemoryManagerMXBean[] getMemoryManagers0() {
        //todo implement it
        return new MemoryManagerMXBean[0];
    }
    /**
     * @see sun.management.MemoryImpl#getMemoryUsage0(boolean)
     */
    private static MemoryUsage getMemoryUsage0(MemoryImpl instance, boolean arg1) {
        //todo implement it
        return null;
    }
    /**
     * @see sun.management.MemoryImpl#setVerboseGC(boolean)
     */
    private static void setVerboseGC(MemoryImpl instance, boolean arg1) {
        //todo implement it
    }
}
