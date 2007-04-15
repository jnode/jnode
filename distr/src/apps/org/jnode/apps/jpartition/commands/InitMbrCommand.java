package org.jnode.apps.jpartition.commands;

import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.partitions.command.PartitionHelper;

public class InitMbrCommand extends BaseDeviceCommand {

	public InitMbrCommand(IDEDevice device) {
		super("init MBR", device);
	}

	@Override
	protected void doExecute() throws CommandException {
		PartitionHelper helper;
		try {
			helper = new PartitionHelper(device);
			helper.initMbr();
		} catch (Throwable t) {
			throw new CommandException(t);
		}
	}
}
