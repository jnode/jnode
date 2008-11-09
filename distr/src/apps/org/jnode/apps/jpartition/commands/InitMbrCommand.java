package org.jnode.apps.jpartition.commands;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.partitions.command.PartitionHelper;

public class InitMbrCommand extends BaseDeviceCommand {

    public InitMbrCommand(IDEDevice device) {
        super("init MBR", device);
    }

    @Override
    protected void doExecute() throws CommandException {
        PartitionHelper helper;
        try {
            //FIXME replace System.out by output stream from (Console)ViewFactory 
            helper = new PartitionHelper(device, new PrintWriter(new OutputStreamWriter(System.out)));
            helper.initMbr();
        } catch (Throwable t) {
            throw new CommandException(t);
        }
    }
}
