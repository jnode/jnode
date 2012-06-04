/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.apps.jpartition.commands;

import org.jnode.apps.jpartition.Context;
import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.partitions.command.PartitionHelper;

/**
 * Command used to initialize the MBR (Main Boot Record) of a device.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class InitMbrCommand extends BaseDeviceCommand {

    /**
     * Constructor.
     * @param device The device to use.
     */
    public InitMbrCommand(IDEDevice device) {
        super("init MBR", device);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doExecute(Context context) throws CommandException {
        PartitionHelper helper;
        try { 
            helper = createPartitionHelper(context);
            helper.initMbr();
        } catch (Throwable t) {
            throw new CommandException(t);
        }
    }
}
