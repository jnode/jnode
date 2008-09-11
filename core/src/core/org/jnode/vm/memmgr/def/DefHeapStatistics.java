/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm.memmgr.def;

import java.util.TreeMap;

import org.jnode.util.NumberUtils;
import org.jnode.vm.memmgr.HeapStatistics;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */

final class DefHeapStatistics extends HeapStatistics {

    private int minInstanceCount = 0;
    private long minTotalSize = 0;
    private final TreeMap<String, HeapCounter> countData = new TreeMap<String, HeapCounter>();

    private static final char newline = '\n';

    public boolean contains(String classname) {
        return countData.containsKey(classname);
    }

    public void add(String className, int size) {
        HeapCounter count = (HeapCounter) countData.get(className);

        if (count == null) {
            count = new HeapCounter(className, size);
            countData.put(className, count);
        }

        count.inc();

        count = null;
    }

    /**
     * Sets the minimum number of instances a class must have before
     * it is listed in toString.
     *
     * @param count
     */
    public void setMinimumInstanceCount(int count) {
        this.minInstanceCount = count;
    }

    /**
     * Sets the minimum bytes of occupied memory by all instances of a class
     * before it is listed in toString.
     *
     * @param bytes
     */
    public void setMinimumTotalSize(long bytes) {
        this.minTotalSize = bytes;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (HeapCounter c : countData.values()) {
            if ((c.getInstanceCount() >= minInstanceCount) && (c.getTotalSize() >= minTotalSize)) {
                if (first) {
                    first = false;
                } else {
                    sb.append(newline);
                }
                c.append(sb);
            }
        }

        return sb.toString();
    }

    static final class HeapCounter {

        private final String name;
        private int instanceCount;
        private int objectSize = 0;

        private static final String usage = " memory usage=";

        public HeapCounter(String objectName, int objectSize) {
            this.name = objectName;
            this.objectSize = objectSize;
            this.instanceCount = 0;
        }

        public void inc() {
            instanceCount++;
        }

        public int getInstanceCount() {
            return instanceCount;
        }

        public int getObjectSize() {
            return objectSize;
        }

        public long getTotalSize() {
            return objectSize * (long) instanceCount;
        }

        public void append(StringBuilder sb) {
            sb.append(name);
            sb.append("  #");
            sb.append(instanceCount);

            if (objectSize != 0) {
                sb.append(usage);
                long size = getTotalSize();
                if (size >= 1024) {
                    sb.append(NumberUtils.size(size) + " (" + getTotalSize() + "b)");
                } else {
                    sb.append(size + "b");
                }
            }
        }
    }
}
