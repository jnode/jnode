package org.jnode.fs.hfsplus.command;

import org.jnode.fs.Formatter;
import org.jnode.fs.command.AbstractFormatCommand;
import org.jnode.fs.hfsplus.HfsPlusFileSystem;

public class FormatHfsPlusCommand extends AbstractFormatCommand<HfsPlusFileSystem> {

	public FormatHfsPlusCommand() {
		super("Format a block device with HFS+ filesystem");
	}

	@Override
	protected Formatter<HfsPlusFileSystem> getFormatter() {
		// TODO implement it.
		return null;
	}

}
