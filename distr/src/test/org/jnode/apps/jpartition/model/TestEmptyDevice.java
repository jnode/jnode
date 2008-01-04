package org.jnode.apps.jpartition.model;


public class TestEmptyDevice extends AbstractTestDevice {
	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected long getStartFreeSpace()
	{
		return 0L;
	}

	@Override
	protected long getEndFreeSpace()
	{
		return DEVICE_SIZE - 1;
	}

	@Override
	protected int getIndexFreeSpacePartition()
	{
		return 0;
	}
}
