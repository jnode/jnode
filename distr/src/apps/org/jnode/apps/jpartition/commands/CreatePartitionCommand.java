package org.jnode.apps.jpartition.commands;

import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.bus.ide.IDEDevice;

public class CreatePartitionCommand extends BasePartitionCommand {

	public CreatePartitionCommand(IDEDevice device, int partitionNumber) {
		super("create partition", device, partitionNumber);
	}

	@Override
	final protected void doExecute() throws CommandException {
		// TODO Auto-generated method stub

	}
}
