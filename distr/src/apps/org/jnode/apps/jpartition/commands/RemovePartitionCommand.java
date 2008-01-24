package org.jnode.apps.jpartition.commands;

import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.bus.ide.IDEDevice;

public class RemovePartitionCommand extends BasePartitionCommand {

	public RemovePartitionCommand(IDEDevice device, int partitionNumber) {
		super("remove partition", device, partitionNumber);
	}

	@Override
	final protected void doExecute() throws CommandException {
		// TODO Auto-generated method stub
	}
		
	@Override
	public String toString() {
		return "remove partition " + partitionNumber + " on device" + device.getId();
	}		
	
}
