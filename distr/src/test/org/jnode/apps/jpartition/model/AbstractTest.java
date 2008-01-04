package org.jnode.apps.jpartition.model;

import org.jnode.apps.jpartition.utils.device.DeviceUtils;
import org.junit.Assert;

import junit.framework.TestCase;

abstract public class AbstractTest {
	static
	{
		// when not in JNode, must be called before anything
		// invoking InitialNaming
		DeviceUtils.initJNodeCore();
	}

	protected void assertEquals(long start, long size, boolean used, Partition part)
	{
		Assert.assertEquals(start, part.getStart());
		Assert.assertEquals(size, part.getSize());
		Assert.assertEquals(start + size - 1, part.getEnd());
		Assert.assertEquals(used, part.isUsed());
	}
}
