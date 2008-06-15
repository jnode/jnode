package org.jnode.apps.jpartition;

import junit.framework.TestSuite;

import org.jnode.apps.jpartition.consoleview.ConsoleViewFactory;
import org.jnode.apps.jpartition.model.TestEmptyDevice;
import org.jnode.apps.jpartition.model.TestNonEmptyDevice;
import org.jnode.apps.jpartition.model.TestOSFacade;
import org.jnode.apps.jpartition.model.TestRemovePartitionFromDevice;
import org.jnode.apps.jpartition.swingview.FileDeviceView;
import org.jnode.apps.jpartition.utils.device.AbstractIDEDevice;
import org.jnode.apps.jpartition.utils.device.DeviceUtils;
import org.jnode.fs.jfat.command.JGrub;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({TestNonEmptyDevice.class, TestEmptyDevice.class,
    TestRemovePartitionFromDevice.class, TestOSFacade.class })
public class JPartitionTest extends TestSuite {
    static {
        // when not in JNode, must be called before anything
        // invoking InitialNaming
        DeviceUtils.initJNodeCore();
    }

    public static void main(String[] args) throws Throwable {
        final ViewFactory vf = new ConsoleViewFactory(System.in, System.out, System.err);
        final ErrorReporter errorReporter = vf.createErrorReporter();
        final Thread t = new Thread() {
            public void run() {
                try {
                    new FileDeviceView(errorReporter);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();

        // DeviceUtils.createFakeDevice(new ErrorReporter());
        AbstractIDEDevice dev = DeviceUtils.createFileDevice();
        JGrub jgrub = new JGrub(System.out, System.err, dev);
        jgrub.install();

        JPartitionCommand.main(args);
    }
}
