/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.apps.jpartition.model;

import org.jnode.fs.FileSystem;
import org.jnode.fs.Formatter;

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

    public final long getStart() {
        return start;
    }

    public final long getEnd() {
        return getStart() + size - 1;
    }

    public final long getSize() {
        return size;
    }

    public final boolean isUsed() {
        return used;
    }

    public final String toString() {
        return "[" + getStart() + "," + getEnd() + "]";
    }

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
