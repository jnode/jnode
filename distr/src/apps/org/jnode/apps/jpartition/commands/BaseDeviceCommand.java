/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

import java.io.IOException;
import java.io.PrintWriter;

import org.jnode.apps.jpartition.Context;
import org.jnode.apps.jpartition.commands.framework.BaseCommand;
import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.partitions.command.PartitionHelper;

/**
 * Abstract command that is working on a device.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public abstract class BaseDeviceCommand extends BaseCommand {
    /**
     * The device to use.
     */
    protected final IDEDevice device;

    /**
     * Constructor.
     * @param name The name of the command.
     * @param device The device to use.
     */
    public BaseDeviceCommand(String name, IDEDevice device) {
        super(name);
        if (device == null) {
            throw new NullPointerException("device is null");
        }

        this.device = device;
    }

    protected final PartitionHelper createPartitionHelper(Context context) throws CommandException {
        try {
            return new PartitionHelper(device, new PrintWriter(context.getOut()));
        } catch (DeviceNotFoundException e) {
            throw new CommandException(e);
        } catch (ApiNotFoundException e) {
            throw new CommandException(e);
        } catch (IOException e) {
            throw new CommandException(e);
        }
    }

    protected abstract void doExecute(Context context) throws CommandException;

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + " - " + device.getId();
    }
}
