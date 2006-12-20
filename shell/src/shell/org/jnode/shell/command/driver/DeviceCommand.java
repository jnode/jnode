/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.shell.command.driver;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.TreeMap;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceAPI;
import org.jnode.driver.DeviceInfoAPI;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DriverException;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.argument.DeviceArgument;
import org.jnode.shell.help.argument.OptionArgument;

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
		for (Class<? extends DeviceAPI> api : dev.implementedAPIs()) {
			out.println(prefix + prefix + api.getName());
		}
		out.println();
        final PrintWriter pw = new PrintWriter(out);
		try {
            final DeviceInfoAPI infoApi = (DeviceInfoAPI)dev.getAPI(DeviceInfoAPI.class);
            if (infoApi != dev) {
                infoApi.showInfo(pw);
            }
        } catch (ApiNotFoundException ex) {
            // Ignore
        }
        if (dev instanceof DeviceInfoAPI) {
            ((DeviceInfoAPI)dev).showInfo(pw);
        }
	}

	/**
	 * Show all devices
	 * 
	 * @param out
	 * @throws NameNotFoundException
	 */
	protected static void showDevices(PrintStream out) throws NameNotFoundException {
		// Create a sorted list
		final TreeMap<String, Device> tm = new TreeMap<String, Device>();
		final DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);

		for (Device dev : dm.getDevices()) {
			tm.put(dev.getId(), dev);
		}
		for (Device dev : tm.values()) {
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
