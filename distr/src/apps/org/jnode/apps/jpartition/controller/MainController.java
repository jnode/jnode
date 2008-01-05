package org.jnode.apps.jpartition.controller;

import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.ViewFactory;

public class MainController
{
	final private ErrorReporter errorReporter;

	@SuppressWarnings("unchecked")
	public MainController(ViewFactory viewFactory) throws Exception
	{
		errorReporter = viewFactory.createErrorReporter();

        //support.addEventListener()
        //dispatcher.registerModel("FileDeviceModel",fileDeviceModel);
        //dispatcher.registerView("FileDeviceModel",fileDeviceView,true);

        // CommandProcessor
        Object cmdProcessorView = viewFactory.createCommandProcessorView();

        // Device
        Object deviceView = viewFactory.createDeviceView(cmdProcessorView);

        //support.addEventListener()
        //dispatcher.registerModel("DeviceModel",deviceModel);
        //dispatcher.registerView("DeviceModel",deviceView,true);
	}

/*
	final private DeviceModel model;

	public DeviceController(DeviceModel model)
	{
		this.model = model;
	}


	public static DeviceModel getDevicePartitions()
	{
		DeviceModel devParts = new DeviceModel();
		try {
			org.jnode.driver.DeviceManager devMan = org.jnode.driver.DeviceUtils.getDeviceManager();
			Collection<Device> devices = devMan.getDevicesByAPI(PartitionableBlockDeviceAPI.class);
			for(Device device : devices)
			{
				devParts.add(new DeviceModel((IDEDevice) device));
			}
		} catch (NameNotFoundException e) {
			log.error(e);
		}
		return devParts;
	}
*/

//	public void userProcessCommands() {
//		cmdProcessorModel.processCommands();
//	}
}
