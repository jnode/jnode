package org.jnode.apps.jpartition.commands;

import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.bus.ide.IDEDevice;

abstract public class BasePartitionCommand extends BaseDeviceCommand {
	protected final int partitionNumber;

	public BasePartitionCommand(String name, IDEDevice device, int partitionNumber)
	{
		super(name, device);
		this.partitionNumber = partitionNumber;
	}

	abstract protected void doExecute() throws CommandException;

	@Override
	public String toString() {
		return super.toString() + " - partition " + partitionNumber;
	}
}
