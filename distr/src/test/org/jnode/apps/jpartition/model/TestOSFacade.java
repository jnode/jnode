package org.jnode.apps.jpartition.model;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.utils.device.DeviceUtils;
import org.jnode.test.AnnotationTest.Test;
import org.junit.Before;

public class TestOSFacade extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        DeviceUtils.createFakeDevice(new ErrorReporter());
        UserFacade.getInstance().getDeviceNames();
        // selectedDevice = new Device("dev1", 10000);
    }

    @Test
    public void removeMe() {
    }
}
