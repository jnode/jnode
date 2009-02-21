/*
 * Copyright 2004 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package sun.tools.jconsole;

import java.lang.management.MemoryUsage;

public class MemoryPoolStat {
    private String      poolName;
    private long        usageThreshold;
    private MemoryUsage usage;
    private long        lastGcId;
    private long        lastGcStartTime;
    private long        lastGcEndTime;
    private long        collectThreshold;
    private MemoryUsage beforeGcUsage;
    private MemoryUsage afterGcUsage;

    MemoryPoolStat(String name,
                   long usageThreshold,
                   MemoryUsage usage,
                   long lastGcId,
                   long lastGcStartTime,
                   long lastGcEndTime,
                   long collectThreshold,
                   MemoryUsage beforeGcUsage,
                   MemoryUsage afterGcUsage) {
        this.poolName = name;
        this.usageThreshold = usageThreshold;
        this.usage = usage;
        this.lastGcId = lastGcId;
        this.lastGcStartTime = lastGcStartTime;
        this.lastGcEndTime = lastGcEndTime;
        this.collectThreshold = collectThreshold;
        this.beforeGcUsage = beforeGcUsage;
        this.afterGcUsage = afterGcUsage;
    }

    /**
     * Returns the memory pool name.
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * Returns the current memory usage.
     */
    public MemoryUsage getUsage() {
        return usage;
    }

    /**
     * Returns the current usage threshold.
     * -1 if not supported.
     */
    public long getUsageThreshold() {
        return usageThreshold;
    }

    /**
     * Returns the current collection usage threshold.
     * -1 if not supported.
     */
    public long getCollectionUsageThreshold() {
        return collectThreshold;
    }

    /**
     * Returns the Id of GC.
     */
    public long getLastGcId() {
        return lastGcId;
    }


    /**
     * Returns the start time of the most recent GC on
     * the memory pool for this statistics in milliseconds.
     *
     * Return 0 if no GC occurs.
     */
    public long getLastGcStartTime() {
        return lastGcStartTime;
    }

    /**
     * Returns the end time of the most recent GC on
     * the memory pool for this statistics in milliseconds.
     *
     * Return 0 if no GC occurs.
     */
    public long getLastGcEndTime() {
        return lastGcEndTime;
    }

    /**
     * Returns the memory usage before the most recent GC started.
     * null if no GC occurs.
     */
    public MemoryUsage getBeforeGcUsage() {
        return beforeGcUsage;
    }

    /**
     * Returns the memory usage after the most recent GC finished.
     * null if no GC occurs.
     */
    public MemoryUsage getAfterGcUsage() {
        return beforeGcUsage;
    }
}
