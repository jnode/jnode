package org.jnode.apps.jpartition.commands;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.jnode.apps.jpartition.commands.framework.BaseCommand;
import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.partitions.command.PartitionHelper;

public abstract class BaseDeviceCommand extends BaseCommand {
    protected final IDEDevice device;

    public BaseDeviceCommand(String name, IDEDevice device) {
        super(name);
        if (device == null) {
            throw new NullPointerException("device is null");
        }

        this.device = device;
    }

    protected final PartitionHelper createPartitionHelper() throws CommandException {
        try {
            //FIXME replace System.out by output stream from (Console)ViewFactory 
            return new PartitionHelper(device, new PrintWriter(new OutputStreamWriter(System.out)));
        } catch (DeviceNotFoundException e) {
            throw new CommandException(e);
        } catch (ApiNotFoundException e) {
            throw new CommandException(e);
        } catch (IOException e) {
            throw new CommandException(e);
        }
    }

    protected abstract void doExecute() throws CommandException;

    @Override
    public String toString() {
        return super.toString() + " - " + device.getId();
    }
}
