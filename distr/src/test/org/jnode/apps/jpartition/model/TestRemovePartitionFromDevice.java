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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestRemovePartitionFromDevice extends AbstractTest {
    protected static final int DEVICE_SIZE = 5000;

    private final long beginSpace = 1500;
    private final long sizeSpace = 1000;

    @Test
    public void testRemovePartitionBetweenNonFreeSpaces() {
        final Device device = new CustomDevice("dev1", DEVICE_SIZE);
        device.addPartition(device.getStart(), beginSpace);
        device.addPartition(beginSpace, sizeSpace);
        device.addPartition(2500, 2500);

        device.removePartition(beginSpace + 5);
        List<Partition> partitions = device.getPartitions();
        Assert.assertEquals(3, partitions.size());

        Partition part1 = partitions.get(0);
        assertEquals(0, beginSpace, true, part1);

        Partition part2 = partitions.get(1);
        assertEquals(beginSpace, sizeSpace, false, part2);

        Partition part3 = partitions.get(2);
        long part3Size = DEVICE_SIZE - part1.getSize() - part2.getSize();
        assertEquals(part2.getEnd() + 1, part3Size, true, part3);
    }

    @Test
    public void testRemovePartitionAfterAFreeSpace() {
        final Device device = new CustomDevice("dev1", DEVICE_SIZE);
        device.addPartition(beginSpace, sizeSpace);
        device.addPartition(2500, 2500);

        device.removePartition(beginSpace + 5);
        List<Partition> partitions = device.getPartitions();
        Assert.assertEquals(2, partitions.size());

        Partition part1 = partitions.get(0);
        assertEquals(0, beginSpace + sizeSpace, false, part1);

        Partition part2 = partitions.get(1);
        long part2Size = DEVICE_SIZE - part1.getSize();
        assertEquals(part1.getEnd() + 1, part2Size, true, part2);
    }

    @Test
    public void testRemovePartitionBeforeAFreeSpace() {
        final Device device = new CustomDevice("dev1", DEVICE_SIZE);
        device.addPartition(device.getStart(), beginSpace);
        device.addPartition(beginSpace, sizeSpace);

        device.removePartition(beginSpace + 5);
        List<Partition> partitions = device.getPartitions();
        Assert.assertEquals(2, partitions.size());

        Partition part1 = partitions.get(0);
        assertEquals(0, beginSpace, true, part1);

        Partition part2 = partitions.get(1);
        assertEquals(beginSpace, DEVICE_SIZE - beginSpace, false, part2);
    }

    @Test
    public void testRemovePartitionBetweenTwoFreeSpaces() {
        final Device device = new CustomDevice("dev1", DEVICE_SIZE);
        device.addPartition(beginSpace, sizeSpace);

        device.removePartition(beginSpace + 5);
        List<Partition> partitions = device.getPartitions();
        Assert.assertEquals(1, partitions.size());

        Partition part1 = partitions.get(0);
        assertEquals(0, DEVICE_SIZE, false, part1);
    }

    @Test
    public void testRemoveSinglePartition() {
        final Device device = new CustomDevice("dev1", DEVICE_SIZE);
        device.addPartition(0, DEVICE_SIZE);

        device.removePartition(5);
        List<Partition> partitions = device.getPartitions();
        Assert.assertEquals(1, partitions.size());

        Partition part1 = partitions.get(0);
        assertEquals(0, DEVICE_SIZE, false, part1);
    }

    @Test
    public void testRemovePartitionAtBegin() {
        final Device device = new CustomDevice("dev1", DEVICE_SIZE);
        device.addPartition(0, sizeSpace);
        device.addPartition(sizeSpace, DEVICE_SIZE - sizeSpace);

        device.removePartition(5);
        List<Partition> partitions = device.getPartitions();
        Assert.assertEquals(2, partitions.size());

        Partition part1 = partitions.get(0);
        assertEquals(0, sizeSpace, false, part1);

        Partition part2 = partitions.get(1);
        long part2Size = DEVICE_SIZE - part1.getSize();
        assertEquals(part1.getEnd() + 1, part2Size, true, part2);
    }

    @Test
    public void testRemovePartitionAtEnd() {
        final Device device = new CustomDevice("dev1", DEVICE_SIZE);
        device.addPartition(0, sizeSpace);
        device.addPartition(sizeSpace, DEVICE_SIZE - sizeSpace);

        device.removePartition(DEVICE_SIZE - 5);
        List<Partition> partitions = device.getPartitions();
        Assert.assertEquals(2, partitions.size());

        Partition part1 = partitions.get(0);
        assertEquals(0, sizeSpace, true, part1);

        Partition part2 = partitions.get(1);
        long part2Size = DEVICE_SIZE - part1.getSize();
        assertEquals(sizeSpace, part2Size, false, part2);
    }
}
