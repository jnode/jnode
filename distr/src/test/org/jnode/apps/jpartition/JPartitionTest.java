package org.jnode.apps.jpartition;

import junit.framework.TestSuite;

import org.jnode.apps.jpartition.model.TestEmptyDevice;
import org.jnode.apps.jpartition.model.TestNonEmptyDevice;
import org.jnode.apps.jpartition.model.TestOSFacade;
import org.jnode.apps.jpartition.model.TestRemovePartitionFromDevice;
import org.jnode.apps.jpartition.swingview.FileDeviceView;
import org.jnode.apps.jpartition.utils.device.DeviceUtils;
import org.jnode.emu.ShellEmu;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({TestNonEmptyDevice.class, TestEmptyDevice.class,TestRemovePartitionFromDevice.class, TestOSFacade.class})
public class JPartitionTest extends TestSuite {
	static
	{
		// when not in JNode, must be called before anything
		// invoking InitialNaming
		DeviceUtils.initJNodeCore();
	}

	public static void main(String[] args) throws Throwable {
/*
		UserFacade.getInstance().selectDevice("dev1");
		System.out.print("devices:");
		for(String device : UserFacade.getInstance().getDevices())
		{
			System.out.print(device);
			System.out.print(", ");
		}
		System.out.println();

		UserFacade.getInstance().createPartition(0, 5000);
		UserFacade.getInstance().createPartition(5000, 2000);
		UserFacade.getInstance().createPartition(7000, 3000);

		System.out.print("partitions:\n");
		for(Partition partition : UserFacade.getInstance().getPartitions())
		{
			System.out.print("\tstart="+partition.getStart());
			System.out.print(" end="+partition.getEnd());
			System.out.print(" size="+partition.getSize());
			System.out.println(" used="+partition.isUsed());
		}
		System.out.println();
*/
		final ErrorReporter errorReporter = JPartition.createViewFactory(args).createErrorReporter();
		new Thread()
		{
			public void run()
			{
				try {
					new FileDeviceView(errorReporter);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
		JPartition.main(args);
	}

//	public static TestSuite suite()
//	{
//		TestSuite suite = new TestSuite();
//		suite.addTestSuite(TestNonEmptyDevice.class);
//		suite.addTestSuite(TestEmptyDevice.class);
//		suite.addTestSuite(TestRemovePartitionFromDevice.class);
//		suite.addTestSuite(TestOSFacade.class);
//		return suite;
//	}

/*
	private TestModelFacade modelFacade;

	@Override
	protected void setUp() throws Exception {
		modelFacade = new TestModelFacade();
		modelFacade.addDevice(new Device("dev1", 5000));
	}

	public void test1()
	{
		UserFacade.getInstance().selectDevice("dev1");
		System.out.print("devices:");
		for(String device : UserFacade.getInstance().getDevices())
		{
			System.out.print(device);
			System.out.print(", ");
		}
		System.out.println();

		UserFacade.getInstance().createPartition(0, 5000);
		UserFacade.getInstance().createPartition(5000, 2000);
		UserFacade.getInstance().createPartition(7000, 3000);

		System.out.print("partitions:\n");
		for(DevicePart partition : UserFacade.getInstance().getPartitions())
		{
			System.out.print("\tstart="+partition.getStart());
			System.out.print(" end="+partition.getEnd());
			System.out.print(" size="+partition.getSize());
			System.out.println(" used="+partition.isUsed());
		}
		System.out.println();
	}
*/
}
