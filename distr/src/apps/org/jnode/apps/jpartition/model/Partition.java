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
 
package org.jnode.apps.jpartition.model;

import org.jnode.fs.FileSystem;
import org.jnode.fs.Formatter;

/**
 * A virtual partition represents the state of a partition of a physical
 * device after all pending operations have been applied. It's used by the user
 * interface to display the expected final result.
 *  
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class Partition implements Bounded {
    private static final long MIN_SIZE = 1;

    private long start;
    private long size;
    private boolean used;
    private Formatter<? extends FileSystem<?>> formatter;

    Partition(long start, long size, boolean used) {
        this.start = start;
        this.size = size;
        this.used = used;

        if (size < MIN_SIZE) {
            throw new IllegalArgumentException("size must be >= " + MIN_SIZE);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final long getStart() {
        return start;
    }

    /**
     * {@inheritDoc}
     */
    public final long getEnd() {
        return getStart() + size - 1;
    }

    /**
     * Get the size of this partition.
     * @return
     */
    public final long getSize() {
        return size;
    }

    /**
     * Indicates whether this partition is used or not. 
     * @return
     */
    public final boolean isUsed() {
        return used;
    }

    /**
     * @return a {@link String} representation of this partition.
     */
    public final String toString() {
        return "[" + getStart() + "," + getEnd() + "]";
    }

    /**
     * @return One of these values :<br>
     * <ul>
     * <li>"" if this partition is not used</li> 
     * <li>"unformatted" if this partition is not formatted</li>
     * <li>In other cases : the file system format of this partition (ext2, fat32, ...)</li>
     * </ul> 
     */
    public final String getFormat() {
        String format = "";
        if (isUsed()) {
            if (formatter != null) {
                format = formatter.getFileSystemType().getName();
            } else {
                format = "unformatted";
            }
        }
        return format;
    }

    final void setSize(long size) {
        this.size = size;
    }

    void mergeWithNextPartition(long nextPartitionSize) {
        this.size += nextPartitionSize;
    }

    final boolean contains(long offset) {
        long start = getStart();
        return (offset >= start) && ((offset - start) < size);
    }

    // final void moveStart(long delta) {
    // if((delta < 0) && (previous != null))
    // {
    // previous.resize(delta);
    // }
    // else if(delta > 0)
    // {
    // if(previous != null)
    // {
    // previous.resize(delta);
    // }
    // }
    // }

    final void setBounds(long start, long size) {
        this.start = start;
        this.size = size;
    }

    final void format(Formatter<? extends FileSystem<?>> formatter) {
        this.formatter = formatter;
    }
}
