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

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.GcInfo;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Map.Entry;

import static java.lang.management.ManagementFactory.*;

public class MemoryPoolProxy {
    private String poolName;
    private ProxyClient client;
    private ObjectName  objName;
    private MemoryPoolMXBean pool;
    private Map<ObjectName,Long> gcMBeans;
    private GcInfo lastGcInfo;

    public MemoryPoolProxy(ProxyClient client, ObjectName poolName) throws java.io.IOException {
        this.client = client;
        this.objName = objName;
        this.pool = client.getMXBean(poolName, MemoryPoolMXBean.class);
        this.poolName = this.pool.getName();
        this.gcMBeans = new HashMap<ObjectName,Long>();
        this.lastGcInfo = null;

        String[] mgrNames = pool.getMemoryManagerNames();
        for (String name : mgrNames) {
            try {
                ObjectName mbeanName = new ObjectName(GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE +
                                                      ",name=" + name);
                if (client.isRegistered(mbeanName)) {
                    gcMBeans.put(mbeanName, new Long(0));
                }
            } catch (Exception e) {
                assert false;
            }

        }
    }

    public boolean isCollectedMemoryPool() {
        return (gcMBeans.size() != 0);
    }

    public ObjectName getObjectName() {
        return objName;
    }

    public MemoryPoolStat getStat() throws java.io.IOException {
        long usageThreshold = (pool.isUsageThresholdSupported()
                                  ? pool.getUsageThreshold()
                                  : -1);
        long collectThreshold = (pool.isCollectionUsageThresholdSupported()
                                  ? pool.getCollectionUsageThreshold()
                                  : -1);
        long lastGcStartTime = 0;
        long lastGcEndTime = 0;
        MemoryUsage beforeGcUsage = null;
        MemoryUsage afterGcUsage = null;
        long gcId = 0;
        if (lastGcInfo != null) {
            gcId = lastGcInfo.getId();
            lastGcStartTime = lastGcInfo.getStartTime();
            lastGcEndTime = lastGcInfo.getEndTime();
            beforeGcUsage = lastGcInfo.getMemoryUsageBeforeGc().get(poolName);
            afterGcUsage = lastGcInfo.getMemoryUsageAfterGc().get(poolName);
        }

        Set<Map.Entry<ObjectName,Long>> set = gcMBeans.entrySet();
        for (Map.Entry<ObjectName,Long> e : set) {
            GarbageCollectorMXBean gc =
                client.getMXBean(e.getKey(),
                                 com.sun.management.GarbageCollectorMXBean.class);
            Long gcCount = e.getValue();
            Long newCount = gc.getCollectionCount();
            if (newCount > gcCount) {
                gcMBeans.put(e.getKey(), new Long(newCount));
                lastGcInfo = gc.getLastGcInfo();
                if (lastGcInfo.getEndTime() > lastGcEndTime) {
                    gcId = lastGcInfo.getId();
                    lastGcStartTime = lastGcInfo.getStartTime();
                    lastGcEndTime = lastGcInfo.getEndTime();
                    beforeGcUsage = lastGcInfo.getMemoryUsageBeforeGc().get(poolName);
                    afterGcUsage = lastGcInfo.getMemoryUsageAfterGc().get(poolName);
                    assert(beforeGcUsage != null);
                    assert(afterGcUsage != null);
                }
            }
        }

        MemoryUsage usage = pool.getUsage();
        return new MemoryPoolStat(poolName,
                                  usageThreshold,
                                  usage,
                                  gcId,
                                  lastGcStartTime,
                                  lastGcEndTime,
                                  collectThreshold,
                                  beforeGcUsage,
                                  afterGcUsage);
    }
}
