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

package org.jnode.driver.block.ramdisk.command;

import java.io.InputStream;
import java.io.PrintStream;
import javax.naming.NameNotFoundException;
import org.jnode.driver.DeviceAlreadyRegisteredException;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.ramdisk.RamDiskDevice;
import org.jnode.driver.block.ramdisk.RamDiskDriver;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.CommandLine;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RamDiskCommand extends AbstractCommand {
    private final FlagArgument FLAG_CREATE = new FlagArgument(
        "create", Argument.MANDATORY, "if set, create the ramdisk");
    private final IntegerArgument ARG_SIZE = new IntegerArgument(
        "size", Argument.OPTIONAL, "the size of the ramdisk");

    public RamDiskCommand() {
        super("Manage RAM 'disks'");
        registerArguments(FLAG_CREATE, ARG_SIZE);
    }

    public static void main(String[] args) throws Exception {
        new RamDiskCommand().execute(args);
    }

    public void execute()
        throws NameNotFoundException, DriverException, DeviceAlreadyRegisteredException {
        final DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);
        if (FLAG_CREATE.isSet()) {
            // Create
            final int size = ARG_SIZE.isSet() ? ARG_SIZE.getValue() : 4 * 4096;
            RamDiskDevice dev = new RamDiskDevice(null, "dummy", size);
            dev.setDriver(new RamDiskDriver(null));
            dm.register(dev);
        }
    }

}
