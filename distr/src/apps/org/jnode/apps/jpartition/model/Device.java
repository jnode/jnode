package org.jnode.apps.jpartition.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.bus.ide.IDEDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.Formatter;


public class Device implements Iterable<Partition>, Bounded {
	final private String name;
	final private long size;
	final private List<Partition> partitions;
	final private org.jnode.driver.Device device;

	Device(String name, long size) {
		this(name, size, null);
	}

	Device(org.jnode.driver.Device dev) throws ApiNotFoundException, IOException {
		this(dev.getId(), dev.getAPI(IDEDeviceAPI.class).getLength(), dev);
	}

	private Device(String name, long size, org.jnode.driver.Device device) {
		this.name = name;
		this.size = size;
		partitions = new ArrayList<Partition>();
		partitions.add(new Partition(0L, size, false));
		this.device = device;
	}

	final public String getName() {
		return name;
	}

	final public long getSize() {
		return size;
	}

	final public Iterator<Partition> iterator() {
		return partitions.iterator();
	}

	final public List<Partition> getPartitions() {
		return Collections.unmodifiableList(partitions);
	}

	final public long getEnd() {
		return size - 1;
	}

	final public long getStart() {
		return 0;
	}

	final public boolean equals(Object o)
	{
		if(!(o instanceof Device))
		{
			return false;
		}

		Device other = (Device) o;
		return name.equals(other.name);
	}

	final org.jnode.driver.Device getDevice()
	{
		return device;
	}

	final public int hashCode()
	{
		return name.hashCode();
	}

	final void addPartition(long start, long size)
	{
		final long end = (start + size - 1);
		checkBounds(this, "start", start);
		checkBounds(this, "end", end);

		Partition newPart = null;
		int index = findPartition(start, false);
		if(index < 0)
		{
			throw new DeviceException("can't add a partition in a used one");
		}

		Partition oldPart = partitions.get(index);
		checkBounds(oldPart, "end", end);

		newPart = new Partition(start, size, true);
		if(oldPart.getSize() == size)
		{
			// replace the unused partition
			partitions.set(index, newPart);
		}
		else if(start == oldPart.getStart())
		{
			// the new partition
			partitions.add(index, newPart);

			// after the new partition
			oldPart.setSize(oldPart.getSize() - size);
			oldPart.setStart(newPart.getEnd() + 1);
			partitions.set(index + 1, oldPart);
		}
		else if(end == oldPart.getEnd())
		{
			// before the new partition
			oldPart.setSize(oldPart.getSize() - size);

			// the new partition
			partitions.add(index + 1, newPart);
		}
		else
		{
			long beginSize = start - oldPart.getStart();
			long endSize = oldPart.getSize() - size - beginSize;

			// before the new partition
			oldPart.setSize(beginSize);

			// the new partition
			partitions.add(index + 1, newPart);

			// after the new partition
			partitions.add(index + 2, new Partition(end + 1, endSize, false));
		}
	}

	final void removePartition(long offset)
	{
		int index = findPartition(offset, true);
		if(index < 0)
		{
			throw new DeviceException("can't remove an empty partition");
		}

		Partition part = partitions.get(index);
		long start = part.getStart();
		long size = part.getSize();
		if(index > 0)
		{
			Partition partBefore = partitions.get(index - 1);
			if(!partBefore.isUsed()			)
			{
				// merge with previous empty partition
				start = partBefore.getStart();
				size += partBefore.getSize();
				partitions.remove(index);
				index--;
			}
		}

		if(index < (partitions.size() - 1))
		{
			Partition partAfter = partitions.get(index + 1);
			if(!partAfter.isUsed())
			{
				// merge with following empty partition
				size += partAfter.getSize();
				partitions.remove(index + 1);
			}
		}

		partitions.set(index, new Partition(start, size, false));
	}

	final void formatPartition(long offset, Formatter<? extends FileSystem> formatter)
	{
		int index = findPartition(offset, true);
		if(index < 0)
		{
			throw new DeviceException("can't format an empty partition");
		}

		Partition part = partitions.get(index);
		part.format(formatter);
	}

/*
	public void moveStart(DevicePart part, long delta)
	{
		if(part.isUsed())
		{
			((Partition) part).moveStart(delta);
		}
	}

	public void moveEnd(DevicePart part, long delta)
	{
		if(part.isUsed())
		{
			//TODO
			//((Partition) part).moveEnd(delta);
		}
	}

	public void move(DevicePart part, long delta)
	{
		moveStart(part, delta);
		moveEnd(part, delta);
	}
*/

	final private int findPartition(long offset, boolean used)
	{
		checkOffset(offset);

		int result = -1;
		int index = 0;
		for(Partition currentPart : this)
		{
			if(currentPart.contains(offset) && (currentPart.isUsed() == used))
			{
				result = index;
				break;
			}
			index++;
		}
		return result;
	}

	final private void checkOffset(long offset)
	{
		if((offset < 0) || (offset >= size))
		{
			throw new DeviceException("offset("+offset+") out of bounds. should be >=0 and <"+size);
		}
	}


	final private void checkBounds(Bounded bounded, String valueName, long value) {
		if(value < bounded.getStart())
		{
			throw new DeviceException(valueName + " must be >= "+ bounded.getStart());
		}
		if(value > bounded.getEnd())
		{
			throw new DeviceException(valueName + " must be <= "+ bounded.getEnd());
		}
	}
}
