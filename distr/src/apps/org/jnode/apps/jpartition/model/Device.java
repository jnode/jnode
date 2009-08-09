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

/**
 * A virtual device is used to represents the state of a physical device
 * after all pending operations have been applied. It's used by the user
 * interface to display the expected final result.
 *  
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
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

    /**
     * Get the name if this virtual device.
     * @return name if this virtual device.
     */
    public final String getName() {
        return name;
    }

    /**
     * Get the size if this virtual device.
     * @return size if this virtual device.
     */
    public final long getSize() {
        return size;
    }

    /**
     * Say if the virtual device has a partition table.
     * @return <code>true</code> if the virtual device has a partition table.
     */
    public final boolean hasPartititionTable() {
        return hasPartititionTable;
    }

    /**
     * Get the list of partitions of this virtual device.
     * @return
     */
    public final List<Partition> getPartitions() {
        checkPartitionned();
        return Collections.unmodifiableList(partitions);
    }

    /**
     * {@inheritDoc}
     */
    public final long getEnd() {
        return size - 1;
    }

    /**
     * {@inheritDoc}
     */
    public final long getStart() {
        return 0;
    }

    /**
     * @return true if this virtual device is equals to the given object.
     */
    public final boolean equals(Object o) {
        if (!(o instanceof Device)) {
            return false;
        }

        Device other = (Device) o;
        return name.equals(other.name);
    }

    /**
     * Get the hashcode of this virtual device. 
     */
    public final int hashCode() {
        return name.hashCode();
    }

    /**
     * Get the physical device associated to this virtual device.
     * @return
     */
    final org.jnode.driver.Device getDevice() {
        return device;
    }

    /**
     * Add a partition to this virtual device.
     * @param start The start of this partition (included).
     * @param size The size of this partition.
     * @return The new virtual partition.
     */
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

    /**
     * Remove a partition from this virtual device.
     * @param offset An offset that should match a used partition in this virtual device.
     */
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

    /**
     * Format a partition of this virtual device. 
     * @param offset An offset that should match a used partition in this virtual device.
     * @param formatter The formatter to use.
     */
    final void formatPartition(long offset, Formatter<? extends FileSystem<?>> formatter) {
        int index = findPartition(offset, true);
        if (index < 0) {
            throw new DeviceException("can't format an empty partition");
        }

        Partition part = partitions.get(index);
        part.format(formatter);
    }

    /**
     * Find a partition in this virtual device.
     * @param offset An offset that should match a used partition in this virtual
     * device.
     * @param used true if the matching partition must be used.
     * @return The index (zero based) of the partition in this virtual device, -1
     * if there is no partition at the given offset or its <i>used</i> state
     * doesn't match the <code>used</code> parameter.
     */
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

    /**
     * Checks that the given offset is valid.
     * @param offset The offset to check.
     * @throws DeviceException if the offset is not valid.
     */
    private final void checkOffset(long offset) {
        if ((offset < 0) || (offset >= size)) {
            throw new DeviceException("offset(" + offset + ") out of bounds. should be >=0 and <" +
                    size);
        }
    }

    /**
     * Checks that this virtual device is partitioned.
     * @throws DeviceException if this virtual device is not partitioned.
     */
    private final void checkPartitionned() {
        if (!hasPartititionTable) {
            throw new DeviceException("device has no partition table");
        }
    }

    /**
     * Checks that the given value is inside the bounds.
     * @param bounded The bounds inside which the value should be.
     * @param name The name of the value (used in case a {@link DeviceException} is thrown.
     * @param value The value to check.
     * @throws DeviceException if the value is not inside the bounds.
     */
    private final void checkBounds(Bounded bounded, String valueName, long value) {
        if (value < bounded.getStart()) {
            throw new DeviceException(valueName + " must be >= " + bounded.getStart());
        }
        if (value > bounded.getEnd()) {
            throw new DeviceException(valueName + " must be <= " + bounded.getEnd());
        }
    }
}
