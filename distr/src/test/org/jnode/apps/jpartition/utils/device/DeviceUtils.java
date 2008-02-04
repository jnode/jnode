package org.jnode.apps.jpartition.utils.device;

import java.io.File;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.swingview.FileDeviceView;
import org.jnode.apps.jpartition.utils.BasicNameSpace;
import org.jnode.apps.vmware.disk.VMWareDisk;
import org.jnode.apps.vmware.disk.tools.DiskFactory;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.naming.InitialNaming;
import org.jnode.naming.NameSpace;
import org.jnode.test.fs.driver.stubs.StubDeviceManager;
import org.jnode.util.OsUtils;

public class DeviceUtils {
	private static final long DEFAULT_FILE_SIZE = 1024*1024;
	private static final Logger log = Logger.getLogger(FileDeviceView.class);

	private static boolean coreInitialized = false;
	final static public void initJNodeCore()  {
		if(!OsUtils.isJNode() && !coreInitialized)
		{

	        try {
				//ShellEmu.main(new String[0]);
		        NameSpace namespace = new BasicNameSpace();
		        InitialNaming.setNameSpace(namespace);

	        	InitialNaming.bind(DeviceManager.NAME, StubDeviceManager.INSTANCE);
			} catch (NameAlreadyBoundException e) {
				throw new RuntimeException(e);
			} catch (NamingException e) {
				throw new RuntimeException(e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			coreInitialized = true;
		}
	}

	public static IDEDevice createFakeDevice(ErrorReporter errorReporter)
	{
		IDEDevice device = null;
		try
		{
			String name = findUnusedName("fake");
			FakeIDEDevice fd = new FakeIDEDevice(name, true, true, DEFAULT_FILE_SIZE);
			if(addDevice(fd))
			{
				device = fd;
			}
			else
			{
				errorReporter.reportError(log, DeviceUtils.class.getName(), "failed to add device");
			}
		} catch (Exception e) {
			log.error(e);
		}

		return device;
	}

	public static IDEDevice createVMWareDevice(ErrorReporter errorReporter) {
		IDEDevice device = null;

		try
		{
			AbstractIDEDevice fd = createVMWareDevice();
			if(addDevice(fd))
			{
				device = fd;
			}
			else
			{
				errorReporter.reportError(log, DeviceUtils.class.getName(), "failed to add device");
			}
		} catch (Exception e) {
			log.error(e);
		}

		return device;
	}

	private static AbstractIDEDevice createVMWareDevice() throws Exception
	{
		File tmpFile = File.createTempFile("disk", "");
		File directory = tmpFile.getParentFile();
		String name = tmpFile.getName();

		File mainFile = DiskFactory.createSparseDisk(directory, name, DEFAULT_FILE_SIZE);
		VMWareDisk vmwareDisk = new VMWareDisk(mainFile);

		AbstractIDEDevice dev = new VMWareIDEDevice(name,
				true, true, vmwareDisk);
		return dev;
	}

	public static AbstractIDEDevice createFileDevice() throws Exception
	{
		File tmpFile = File.createTempFile("disk", "");
		File directory = tmpFile.getParentFile();
		String name = tmpFile.getName();

		AbstractIDEDevice dev = new FileIDEDevice(name,
				true, true, new File(directory, name), DEFAULT_FILE_SIZE);
		return dev;
	}

	public static void restart(Device device)
	{
		DeviceManager devMan;
		try {
			devMan = org.jnode.driver.DeviceUtils.getDeviceManager();

			devMan.stop(device);
			devMan.start(device);
		} catch (NameNotFoundException e) {
			log.error(e);
		} catch (DeviceNotFoundException e) {
			log.error(e);
		} catch (DriverException e) {
			log.error(e);
		}
	}

	public static boolean addDevice(AbstractIDEDevice device)
	{
		boolean success = false;
		try
		{
			DeviceManager devMan = org.jnode.driver.DeviceUtils.getDeviceManager();
			devMan.register(device);
			success = true;

//			PartitionHelper helper = new PartitionHelper(device);
//			helper.initMbr();
//			helper.write();
//			if(helper.hasValidMBR())
//			{
//				helper.modifyPartition(0, true, 0, DEFAULT_FILE_SIZE,
//						false, IBMPartitionTypes.PARTTYPE_WIN95_FAT32);
//			}
		} catch (NameNotFoundException e) {
			log.error(e);
		} catch (DeviceAlreadyRegisteredException e) {
			log.error(e);
		} catch (DriverException e) {
			log.error(e);
//		} catch (DeviceNotFoundException e) {
//			log.error(e);
//		} catch (ApiNotFoundException e) {
//			log.error(e);
//		} catch (IOException e) {
//			log.error(e);
		}

		return success;
	}

	public static String findUnusedName(String baseName) throws NameNotFoundException
	{
		DeviceManager devMan = org.jnode.driver.DeviceUtils.getDeviceManager();
		String name = null;
		int i = 0;
		do
		{
			String newName = baseName + "-" + i;
			try {
				devMan.getDevice(newName);
				i++;
			} catch (DeviceNotFoundException e) {
				name = newName;
			}
		}
		while(name == null);

		return name;
	}
}
