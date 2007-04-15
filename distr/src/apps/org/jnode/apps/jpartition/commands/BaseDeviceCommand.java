package org.jnode.apps.jpartition.commands;

import org.jnode.apps.jpartition.commands.framework.BaseCommand;
import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.partitions.command.FdiskCommand;

abstract public class BaseDeviceCommand extends BaseCommand {
	protected final IDEDevice device;
	
	public BaseDeviceCommand(String name, IDEDevice device) 
	{
		super(name);
		this.device = device;
	}

	abstract protected void doExecute() throws CommandException;
		
	@Override
	public String toString() {
		return super.toString() + " - " + device.getId();
	}
}
