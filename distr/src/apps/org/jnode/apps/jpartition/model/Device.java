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
 
package org.jnode.apps.jpartition.model;

import java.util.Collections;
import java.util.List;

import org.jnode.fs.FileSystem;
import org.jnode.fs.Formatter;

public class Device implements Bounded {
    private final String name;
    private final long size;
    private final List<Partition> partitions;
    private final org.jnode.driver.Device device;
    private final boolean hasPartititionTable;

    Device(String name, long size, org.jnode.driver.Device device, List<Partition> partitions) {
        this.name = name;
        this.size = size;
        this.partitions = partitions;
        this.device = device;
        this.hasPartititionTable = !partitions.isEmpty();
    }

    public final String getName() {
        return name;
    }

    public final long getSize() {
        return size;
    }

    public final boolean hasPartititionTable() {
        return hasPartititionTable;
    }

    public final List<Partition> getPartitions() {
        checkPartitionned();
        return Collections.unmodifiableList(partitions);
    }

    public final long getEnd() {
        return size - 1;
    }

    public final long getStart() {
        return 0;
    }

    public final boolean equals(Object o) {
        if (!(o instanceof Device)) {
            return false;
        }

        Device other = (Device) o;
        return name.equals(other.name);
    }

    public final int hashCode() {
        return name.hashCode();
    }

    final org.jnode.driver.Device getDevice() {
        return device;
    }

    final Partition addPartition(long start, long size) {
        final long end = (start + size - 1);
        checkBounds(this, "start", start);
        checkBounds(this, "end", end);

        int index = findPartition(start, false);
        if (index < 0) {
            throw new DeviceException("can't add a partition in a used one");
        }

        Partition oldPart = partitions.get(index);
        checkBounds(oldPart, "end", end);

        Partition newPart = new Partition(start, size, true);
        if (oldPart.getSize() == size) {
            // replace the unused partition
            partitions.set(index, newPart);
        } else if (start == oldPart.getStart()) {
            // the new partition
            partitions.add(index, newPart);

            // after the new partition
            oldPart.setBounds(newPart.getEnd() + 1, oldPart.getSize() - size);
            partitions.set(index + 1, oldPart);
        } else if (end == oldPart.getEnd()) {
            // before the new partition
            oldPart.setSize(oldPart.getSize() - size);

            // the new partition
            partitions.add(index + 1, newPart);
        } else {
            long beginSize = start - oldPart.getStart();
            long endSize = oldPart.getSize() - size - beginSize;

            // before the new partition
            oldPart.setSize(beginSize);

            // the new partition
            partitions.add(index + 1, newPart);

            // after the new partition
            partitions.add(index + 2, new Partition(end + 1, endSize, false));
        }

        return newPart;
    }

    final void removePartition(long offset) {
        int index = findPartition(offset, true);
        if (index < 0) {
            throw new DeviceException("can't remove an empty partition");
        }

        Partition part = partitions.get(index);
        long start = part.getStart();
        long size = part.getSize();

        if (index > 0) {
            Partition partBefore = partitions.get(index - 1);
            if (!partBefore.isUsed()) {
                // merge with previous empty partition
                start = partBefore.getStart();
                size += partBefore.getSize();
                partitions.remove(index);
                index--;
            }
        }

        if (index < (partitions.size() - 1)) {
            Partition partAfter = partitions.get(index + 1);
            if (!partAfter.isUsed()) {
                // merge with following empty partition
                size += partAfter.getSize();
                partitions.remove(index + 1);
            }
        }

        partitions.set(index, new Partition(start, size, false));
    }

    final void formatPartition(long offset, Formatter<? extends FileSystem<?>> formatter) {
        int index = findPartition(offset, true);
        if (index < 0) {
            throw new DeviceException("can't format an empty partition");
        }

        Partition part = partitions.get(index);
        part.format(formatter);
    }

    private final int findPartition(long offset, boolean used) {
        checkPartitionned();
        checkOffset(offset);

        int result = -1;
        int index = 0;
        for (Partition currentPart : partitions) {
            if (currentPart.contains(offset) && (currentPart.isUsed() == used)) {
                result = index;
                break;
            }
            index++;
        }
        return result;
    }

    private final void checkOffset(long offset) {
        if ((offset < 0) || (offset >= size)) {
            throw new DeviceException("offset(" + offset + ") out of bounds. should be >=0 and <" +
                    size);
        }
    }

    private final void checkPartitionned() {
        if (!hasPartititionTable) {
            throw new DeviceException("device has no partition table");
        }
    }

    private final void checkBounds(Bounded bounded, String valueName, long value) {
        if (value < bounded.getStart()) {
            throw new DeviceException(valueName + " must be >= " + bounded.getStart());
        }
        if (value > bounded.getEnd()) {
            throw new DeviceException(valueName + " must be <= " + bounded.getEnd());
        }
    }
}
