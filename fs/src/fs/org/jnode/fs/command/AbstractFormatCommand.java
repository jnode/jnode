/*
 * $Id: FormatCommand.java 3585 2007-11-13 13:31:18Z galatnm $
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

package org.jnode.fs.command;

import java.io.InputStream;
import java.io.PrintStream;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.Formatter;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.shell.help.argument.DeviceArgument;

/**
 * @author Fabien DUMINY (fduminy at jnode.org)
 *
 */
abstract public class AbstractFormatCommand<T extends FileSystem<?>> extends AbstractCommand {
    private static final DeviceArgument ARG_DEVICE = new DeviceArgument("device-id",
    	"the device to format");

    protected static final Parameter PARAM_DEVICE = new Parameter(ARG_DEVICE,
    		Parameter.MANDATORY);

    abstract protected ParsedArguments parse(CommandLine commandLine) throws SyntaxErrorException;
    abstract protected Formatter<T> getFormatter(ParsedArguments cmdLine);

	final public void execute(CommandLine commandLine, InputStream in, PrintStream out, PrintStream err) throws Exception {
		try {
            ParsedArguments cmdLine = parse(commandLine);

            String device = ARG_DEVICE.getValue(cmdLine);
            Formatter<T> formatter = getFormatter(cmdLine);

            DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);

            Device dev = dm.getDevice(device);
			if(!(dev.getDriver() instanceof FSBlockDeviceAPI)){
				throw new FileSystemException(
                	"device unsupported by format command");
			}
            formatter.format(dev);

            // restart the device
            dm.stop(dev);
            dm.start(dev);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            exit(1);
        } catch (DeviceNotFoundException e) {
            e.printStackTrace();
            exit(2);
        } catch (DriverException e) {
            e.printStackTrace();
            exit(3);
        } catch (FileSystemException e) {
            e.printStackTrace();
            exit(4);
        }
	}
}
