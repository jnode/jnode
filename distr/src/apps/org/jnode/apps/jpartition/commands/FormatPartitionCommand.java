package org.jnode.apps.jpartition.commands;

import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.fs.FileSystem;
import org.jnode.fs.Formatter;

public class FormatPartitionCommand extends BasePartitionCommand {
	private final Formatter<? extends FileSystem> formatter;
	
	public FormatPartitionCommand(IDEDevice device, int partitionNumber, Formatter<? extends FileSystem> formatter) {
		super("format partition", device, partitionNumber);
		this.formatter = formatter;
	}

	@Override
	final protected void doExecute() throws CommandException {
		// TODO Auto-generated method stub
	}
		
	@Override
	public String toString() {
		return "format partition " + partitionNumber + " on device " + device.getId() + " with " + formatter.getFileSystemType().getName();
	}		
}
