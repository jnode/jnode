package org.jnode.apps.jpartition.controller;

import it.battlehorse.stamps.Dispatcher;
import it.battlehorse.stamps.factories.DispatcherRegistry;
import it.battlehorse.stamps.factories.TransformerRegistry;
import org.jnode.apps.jpartition.ViewFactory;
import org.jnode.apps.jpartition.model.CommandProcessorModel;
import org.jnode.apps.jpartition.model.DeviceModel;
import org.jnode.apps.jpartition.model.FileDeviceModel;

public class MainController
{
	final private DeviceModel deviceModel;
	final private FileDeviceModel fileDeviceModel;	
	final private CommandProcessorModel cmdProcessorModel;
	
	public MainController(ViewFactory viewFactory) throws Exception
	{		
        TransformerRegistry.getInstance().loadTransformers();
        Dispatcher dispatcher = DispatcherRegistry.getInstance().getBasicDispatcher();

        // FileDevice
        this.fileDeviceModel = new FileDeviceModel();
        Object fileDeviceView = viewFactory.createFileDeviceView(this);
        
        dispatcher.registerModel("FileDeviceModel",fileDeviceModel);
        dispatcher.registerView("FileDeviceModel",fileDeviceView,true);
        
        // CommandProcessor
        this.cmdProcessorModel = new CommandProcessorModel();
        Object cmdProcessorView = viewFactory.createCommandProcessorView(this);
        
        dispatcher.registerModel("CommandProcessorModel",cmdProcessorModel);
        dispatcher.registerView("CommandProcessorModel",cmdProcessorView,true);
        
        // Device
        this.deviceModel = new DeviceModel();
        Object deviceView = viewFactory.createDeviceView(this, fileDeviceView, cmdProcessorView);
        
        dispatcher.registerModel("DeviceModel",deviceModel);
        dispatcher.registerView("DeviceModel",deviceView,true);
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


	public void userAddFakeDisk() {
		fileDeviceModel.addFakeDisk();
	}

	public void userAddVMWareDisk() {
		fileDeviceModel.addVMWareDisk();
	}		

	public void userRemoveFileDevice(Object device) {
		fileDeviceModel.removeFileDevice(device);
	}

	public void userProcessCommands() {
		cmdProcessorModel.processCommands();
	}

	public void userSelectDevice(Object device) {
		deviceModel.setDevice(device);
	}	
}
