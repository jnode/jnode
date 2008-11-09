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
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;

/**
 * @author Fabien DUMINY (fduminy at jnode.org)
 * @author crawley@jnode.org
 */
public abstract class AbstractFormatCommand<T extends FileSystem<?>> extends AbstractCommand {
    
    protected final DeviceArgument ARG_DEVICE = 
        new DeviceArgument("device", Argument.MANDATORY, "the device to format", FSBlockDeviceAPI.class);
    
    public AbstractFormatCommand(String description) {
        super(description);
        registerArguments(ARG_DEVICE);
    }

    protected abstract Formatter<T> getFormatter();

    public final void execute() 
        throws FileSystemException, NameNotFoundException, DeviceNotFoundException, DriverException {
        Device dev = ARG_DEVICE.getValue();
        Formatter<T> formatter = getFormatter();
        formatter.format(dev);

        // restart the device
        final DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);
        dm.stop(dev);
        dm.start(dev);
    }
}
