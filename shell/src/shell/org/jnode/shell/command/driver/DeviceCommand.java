/*
 * $Id$
 */
package org.jnode.shell.command.driver;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.TreeMap;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DriverException;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.help.DeviceArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.OptionArgument;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;

/**
 * @author epr
 */
public class DeviceCommand {

	static final OptionArgument ARG_ACTION =
		new OptionArgument(
			"action",
			"action to do on the device",
			new OptionArgument.Option[] {
				new OptionArgument.Option("restart", "Restart device"),
				new OptionArgument.Option("stop", "Stop device"),
				new OptionArgument.Option("start", "Start device"),
				new OptionArgument.Option("remove", "Remove a device")});

	static final DeviceArgument ARG_DEVICE = new DeviceArgument("device-id", "the device to print informations about");
	static final Parameter PARAM_ACTION = new Parameter(ARG_ACTION, Parameter.MANDATORY);

	static final Parameter PARAM_DEVICE = new Parameter(ARG_DEVICE, Parameter.MANDATORY);

	public static Help.Info HELP_INFO =
		new Help.Info(
			"device",
			new Syntax[] {
				new Syntax("Print information about all devices"),
				new Syntax("Print information about a specific device", new Parameter[] { PARAM_DEVICE }),
				new Syntax("Execute a command on a device", new Parameter[] { PARAM_ACTION, PARAM_DEVICE })
	});

	public static void main(String[] args) throws Exception {
		ParsedArguments cmdLine = HELP_INFO.parse(args);

		if (PARAM_ACTION.isSet(cmdLine)) {
			String action = ARG_ACTION.getValue(cmdLine);
			if (action.compareTo("restart") == 0) {
				restartDevice(ARG_DEVICE.getDevice(cmdLine));
			} else if (action.compareTo("start") == 0) {
				startDevice(ARG_DEVICE.getDevice(cmdLine));
			} else if (action.compareTo("stop") == 0) {
				stopDevice(ARG_DEVICE.getDevice(cmdLine));
			} else if (action.compareTo("remove") == 0) {
				removeDevice(ARG_DEVICE.getDevice(cmdLine));
			}
			return;
		}

		if (!PARAM_DEVICE.isSet(cmdLine)) {
			showDevices(System.out);
		} else {
			showDevice(System.out, ARG_DEVICE.getDevice(cmdLine));
		}
	}

	/**
	 * @param device
	 */
	private static void stopDevice(Device device) {
		try {
			device.getManager().stop(device);
		} catch (DriverException e) {
			e.printStackTrace();
		} catch (DeviceNotFoundException ex) {
            ex.printStackTrace();
        }
	}

	/**
	 * Stop and Remove the given device.
	 * @param device
	 */
	private static void removeDevice(Device device) {
		try {
			device.getManager().stop(device);
			device.getManager().unregister(device);
		} catch (DriverException e) {
			e.printStackTrace();
		} catch (DeviceNotFoundException ex) {
            ex.printStackTrace();
        }
	}

	/**
	 * @param device
	 */
	private static void startDevice(Device device) {
		try {
			device.getManager().start(device);
		} catch (DriverException e) {
			e.printStackTrace();
        } catch (DeviceNotFoundException ex) {
            ex.printStackTrace();
        }
	}

	/**
	 * @param device
	 */
	private static void restartDevice(Device device) {
		try {
			device.getManager().stop(device);
			device.getManager().start(device);
		} catch (DriverException e) {
			e.printStackTrace();
        } catch (DeviceNotFoundException ex) {
            ex.printStackTrace();
        }
	}

	/**
	 * Show all info of the device with a given name
	 * 
	 * @param out
	 * @param dev
	 */
	protected static void showDevice(PrintStream out, Device dev) {
		final String prefix = "    ";
		out.println("Device: " + dev.getId());
		final String drvClassName = dev.getDriverClassName();
		if (dev.isStarted()) {
			out.println(prefix + "state:started");
		} else {
			out.println(prefix + "state:stopped");
		}
		if (drvClassName != null) {
			out.println(prefix + "driver:" + drvClassName);
		} else {
			out.println(prefix + "driver:none");
		}
		out.println(prefix + "implemented API's:");
		for (Iterator i = dev.implementedAPIs().iterator(); i.hasNext();) {
			final Class api = (Class) i.next();
			out.println(prefix + prefix + api.getName());
		}
		out.println();
	}

	/**
	 * Show all devices
	 * 
	 * @param out
	 * @throws NameNotFoundException
	 */
	protected static void showDevices(PrintStream out) throws NameNotFoundException {
		// Create a sorted list
		final TreeMap tm = new TreeMap();
		final DeviceManager dm = (DeviceManager) InitialNaming.lookup(DeviceManager.NAME);

		for (Iterator i = dm.getDevices().iterator(); i.hasNext();) {
			final Device dev = (Device) i.next();
			tm.put(dev.getId(), dev);
		}
		for (Iterator i = tm.values().iterator(); i.hasNext();) {
			final Device dev = (Device) i.next();
			out.print(dev.getId());
			final String drvClassName = dev.getDriverClassName();
			if (dev.isStarted()) {
				out.print("\tstarted");
			} else {
				out.print("\tstopped");
			}
			if (drvClassName != null) {
				out.print("\tdriver:" + drvClassName);
			} else {
				out.print("\tdriver:none");
			}
			out.println();
		}
	}

}
