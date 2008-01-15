package org.jnode.apps.jpartition.model;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.utils.device.DeviceUtils;
import org.junit.Before;

public class TestOSFacade extends AbstractTest {

	@Before
	public void setUp() throws Exception {
		DeviceUtils.createFakeDevice(new ErrorReporter());
		UserFacade.getInstance().getDevices();
		//selectedDevice = new Device("dev1", 10000);
	}
}
