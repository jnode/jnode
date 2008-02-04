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

		if(size < MIN_SIZE)
		{
			throw new IllegalArgumentException("size must be > "+MIN_SIZE);
		}
	}

	final public long getStart() {
		return start;
	}

	final public long getEnd()
	{
		return getStart() + size - 1;
	}

	final public long getSize() {
		return size;
	}

	final public boolean isUsed()
	{
		return used;
	}

	final public String toString()
	{
		return "["+getStart()+","+getEnd()+"]";
	}

	final public String getFormat()
	{
		String format = "";
		if(isUsed())
		{
			if(formatter != null)
			{
				format = formatter.getFileSystemType().getName();
			}
			else
			{
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

//	final void moveStart(long delta) {
//		if((delta < 0) && (previous != null))
//		{
//			previous.resize(delta);
//		}
//		else if(delta > 0)
//		{
//			if(previous != null)
//			{
//				previous.resize(delta);
//			}
//		}
//	}

	final void setBounds(long start, long size) {
		this.start = start;
		this.size = size;
	}

	final void format(Formatter<? extends FileSystem<?>> formatter) {
		this.formatter = formatter;
	}
}
