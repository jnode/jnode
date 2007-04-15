package org.jnode.apps.jpartition.swingview.actions;

import org.jnode.apps.jpartition.commands.BaseDeviceCommand;
import org.jnode.apps.jpartition.commands.InitMbrCommand;
import org.jnode.apps.jpartition.commands.framework.CommandProcessor;
import org.jnode.driver.bus.ide.IDEDevice;

public class InitMbrAction extends BaseDeviceAction 
{	
	public InitMbrAction(IDEDevice device, CommandProcessor processor) {
		super(device, processor, "init MBR");
	}
	
	@Override
	protected BaseDeviceCommand getCommand(IDEDevice device) {
		return new InitMbrCommand(device);
	}
}
