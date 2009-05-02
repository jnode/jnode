/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

/**
 * Abstract command that is working on a partition.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public abstract class BasePartitionCommand extends BaseDeviceCommand {
    /**
     * Number (zero based) of the device's partition.
     */
    protected final int partitionNumber;

    /**
     * Constructor.
     * @param name The name of the command.
     * @param device The device to use.
     * @partitionNumber Number (zero based) of the device's partition.
     */
    public BasePartitionCommand(String name, IDEDevice device, int partitionNumber) {
        super(name, device);
        this.partitionNumber = partitionNumber;
    }

    protected abstract void doExecute(Context context) throws CommandException;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + " - partition " + partitionNumber;
    }
}
