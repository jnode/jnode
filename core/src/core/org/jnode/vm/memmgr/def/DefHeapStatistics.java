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
 
package org.jnode.vm.memmgr.def;

import java.io.IOException;
import java.util.TreeMap;

import org.jnode.util.NumberUtils;
import org.jnode.vm.facade.HeapStatistics;
import org.jnode.vm.facade.NoObjectFilter;
import org.jnode.vm.facade.ObjectFilter;
import org.jnode.vm.objects.VmSystemObject;

/**
 * @author Martin Husted Hartvig (hagar@jnode.org)
 */

final class DefHeapStatistics extends VmSystemObject implements HeapStatistics {

    private int minInstanceCount = 0;
    private long minTotalSize = 0;
    private ObjectFilter objectFilter = NoObjectFilter.INSTANCE;
    private final TreeMap<String, HeapCounter> countData = new TreeMap<String, HeapCounter>();

    private static final char NEWLINE = '\n';
    private static final String USAGE = " memory usage=";
    private static final String NO_MATCHING_OBJECT = "No object is matching criteria";
    private static final String SUMMARY = "Summary : ";
    private static final String CLASSES = " classe(s) ";
    private static final String INSTANCES = " instances(s) ";
    
    public boolean contains(String classname) {
    	// If we don't accept this class, we pretend to have it already to (maybe) avoid unnecessary work
    	// and memory allocation (we also hope to avoid a call to add(String, int)).
        return !objectFilter.accept(classname) || countData.containsKey(classname);
    }

    public void add(String className, int size) {
    	if (objectFilter.accept(className)) {	    	
	        HeapCounter count = (HeapCounter) countData.get(className);
	
	        if (count == null) {
	            count = new HeapCounter(className, size);
	            countData.put(className, count);
	        }
	
	        count.inc();
    	}
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setObjectFilter(ObjectFilter objectFilter) {
        this.objectFilter = (objectFilter == null) ? NoObjectFilter.INSTANCE : objectFilter;
    }
    
    /**
     * {@inheritDoc}
     * @throws IOException 
     */
    public void writeTo(Appendable a) throws IOException {
        boolean first = true;

        if (countData.isEmpty()) {
            a.append(NO_MATCHING_OBJECT);
        } else {
            int nbClasses = 0;
            int nbInstances = 0;
            int totalSize = 0;
            
            for (HeapCounter c : countData.values()) {
                if ((c.getInstanceCount() >= minInstanceCount) && (c.getTotalSize() >= minTotalSize)) {
                    if (first) {
                        first = false;
                    } else {
                        a.append(NEWLINE);
                    }
                    c.append(a);
                    
                    nbClasses++;
                    nbInstances += c.getInstanceCount();
                    totalSize += c.getTotalSize();
                }
            }
            
            if (nbClasses == 0) {
                a.append(NO_MATCHING_OBJECT);                
            } else {
                a.append(NEWLINE);
                a.append(SUMMARY).append(Integer.toString(nbClasses)).append(CLASSES);
                a.append(Integer.toString(nbInstances)).append(INSTANCES);
                appendUsage(a, totalSize);
            }
        }
        a.append(NEWLINE);
    }
    
    private static void appendUsage(Appendable a, long size) throws IOException {
        a.append(USAGE);
        if (size >= 1024) {
            a.append(NumberUtils.toBinaryByte(size)).append(" (");
            a.append(Long.toString(size)).append("b)");
        } else {
            a.append(Long.toString(size)).append('b');
        }

    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        try {
            writeTo(sb);
        } catch (IOException e) {
            // normally, it will never happen 
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    static final class HeapCounter {

        private final String name;
        private int instanceCount;
        private int objectSize = 0;

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

        public void append(Appendable a) throws IOException {
            a.append(name);
            a.append("  #");
            a.append(Integer.toString(instanceCount));

            if (objectSize != 0) {
                appendUsage(a, getTotalSize());
            }
        }
    }
}
