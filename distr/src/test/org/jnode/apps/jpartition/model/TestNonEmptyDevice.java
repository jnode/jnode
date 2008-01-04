package org.jnode.apps.jpartition.model;


public class TestNonEmptyDevice extends AbstractTestDevice {
	private static final long PARTITION1_SIZE = 500;
	private static final long PARTITION3_SIZE = 700;

	@Override
	protected long getStartFreeSpace()
	{
		return PARTITION1_SIZE;
	}

	@Override
	protected long getEndFreeSpace()
	{
		return DEVICE_SIZE - PARTITION3_SIZE - 1;
	}

	@Override
	protected int getIndexFreeSpacePartition()
	{
		return 1;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		device.addPartition(0, PARTITION1_SIZE);
		device.addPartition(DEVICE_SIZE - PARTITION3_SIZE, PARTITION3_SIZE);
	}
}
