/*
 * $Id$
 */
package org.jnode.driver.block.ramdisk.command;

import org.jnode.driver.DeviceManager;
import org.jnode.driver.block.ramdisk.RamDiskDevice;
import org.jnode.driver.block.ramdisk.RamDiskDriver;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.help.DeviceArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.IntegerArgument;
import org.jnode.shell.help.OptionArgument;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RamDiskCommand {
	
	static final OptionArgument ARG_ACTION =
		new OptionArgument(
			"action",
			"action to do on the ramdisk",
			new OptionArgument.Option[] {
				new OptionArgument.Option("create", "Create a ramdisk"),
				});

	static final DeviceArgument ARG_DEVICE = new DeviceArgument("device-id", "the device to print informations about");
	static final IntegerArgument ARG_SIZE = new IntegerArgument("size", "the size of the ramdisk");
	
	static final Parameter PARAM_ACTION = new Parameter(ARG_ACTION, Parameter.MANDATORY);
	static final Parameter PARAM_DEVICE = new Parameter(ARG_DEVICE, Parameter.MANDATORY);
	static final Parameter PARAM_SIZE = new Parameter(ARG_SIZE, Parameter.OPTIONAL);

	public static Help.Info HELP_INFO =
		new Help.Info(
			"ramdisk",
			new Syntax[] {
				new Syntax("Create a ramdisk", new Parameter[] { PARAM_ACTION, PARAM_SIZE })
	});

	public static void main(String[] args) 
	throws Exception {
		final ParsedArguments cmdLine = HELP_INFO.parse(args);
		
		final DeviceManager dm = (DeviceManager)InitialNaming.lookup(DeviceManager.NAME);
		if (PARAM_ACTION.isSet(cmdLine)) {
			// Create
			final int size;
			if (PARAM_SIZE.isSet(cmdLine)) {
				size = ARG_SIZE.getInteger(cmdLine);
			} else {
				size = 4*4096;
			}
			RamDiskDevice dev = new RamDiskDevice(null, "dummy", size);
			dev.setDriver(new RamDiskDriver(null));
			dm.register(dev);
		}

	}

}
