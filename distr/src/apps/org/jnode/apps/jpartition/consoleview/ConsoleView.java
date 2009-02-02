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
 
package org.jnode.apps.jpartition.consoleview;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.Context;
import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.commands.framework.Command;
import org.jnode.apps.jpartition.consoleview.components.Component;
import org.jnode.apps.jpartition.consoleview.components.NumberField;
import org.jnode.apps.jpartition.consoleview.components.Options;
import org.jnode.apps.jpartition.consoleview.components.YesNo;
import org.jnode.apps.jpartition.model.Device;
import org.jnode.apps.jpartition.model.Partition;
import org.jnode.apps.jpartition.model.UserFacade;

class ConsoleView extends Component {
    private static final Logger log = Logger.getLogger(ConsoleView.class);

    private final boolean install;
    private Partition selectedPartition;

    ConsoleView(InputStream in, PrintStream out, ErrorReporter errorReporter, boolean install) {
        super(new Context(in, out, errorReporter));
        this.install = install;

        try {
            start();
        } catch (Throwable e) {
            errorReporter.reportError(log, this, e);
        }

        println();

        if (selectedPartition == null) {
            print("selectedPartition=none");
        } else {
            print("selectedPartition=" + PartitionLabelizer.INSTANCE.getLabel(selectedPartition));
        }

        if (UserFacade.getInstance().getSelectedDevice() == null) {
            println(" on no device");
        } else {
            println(" on device " +
                    DeviceLabelizer.INSTANCE.getLabel(UserFacade.getInstance().getSelectedDevice()));
        }
    }

    private void start() throws Exception {
        if (!selectDevice()) {
            println("no device to partition");
            return;
        }

        if (!UserFacade.getInstance().getSelectedDevice().hasPartititionTable()) {
            println("device has no partition table");
            return;
        }

        selectPartition();

        List<Command> pendingCommands = UserFacade.getInstance().getPendingCommands();
        if (!pendingCommands.isEmpty()) {
            YesNo yesNo = new YesNo(context);
            println();
            println("The following modifications are pending :");
            for (Command cmd : pendingCommands) {
                println("\t" + cmd);
            }

            boolean apply = yesNo.show("Would you like to apply them ?");
            if (apply) {
                UserFacade.getInstance().applyChanges();
            }
        }
    }

    private boolean selectDevice() throws IOException {
        boolean deviceSelected = false;

        List<Device> devices = UserFacade.getInstance().getDevices();
        if ((devices != null) && !devices.isEmpty()) {
            Options devicesOpt = new Options(context);
            int choice =
                    (int) devicesOpt.show("Select a device", devices, DeviceLabelizer.INSTANCE);

            String device = devices.get(choice - 1).getName();
            UserFacade.getInstance().selectDevice(device);
            println("device=" + device);
            deviceSelected = true;
        }

        return deviceSelected;
    }

    private void selectPartition() throws Exception {
        List<Partition> partitions = UserFacade.getInstance().getPartitions();
        if ((partitions.size() == 1) && !partitions.get(0).isUsed()) {
            YesNo yesNo = new YesNo(context);
            boolean create = yesNo.show("There is no partition. Would you like to create one ?");
            if (create) {
                selectedPartition = createPartition(partitions.get(0));
            }
        }

        if (selectedPartition == null) {
            partitions = UserFacade.getInstance().getPartitions();

            Options partitionsOpt = new Options(context);
            int choice =
                    (int) partitionsOpt.show("Select a partition", partitions,
                            PartitionLabelizer.INSTANCE);

            selectedPartition = partitions.get(choice - 1);
        }

        if (selectedPartition != null) {
            if (install) {
                formatPartition(selectedPartition);
            } else {
                modifyPartition(selectedPartition);
            }
        }
    }

    private Partition createPartition(Partition freePart) throws Exception {
        long size = freePart.getSize();
        NumberField sizeField = new NumberField(context);
        size = sizeField.show("Size of the new partition ", size, 1, size);

        return UserFacade.getInstance().createPartition(freePart.getStart(), size);
    }

    private void modifyPartition(Partition partition) throws Exception {
        if (partition.isUsed()) {
            final String[] operations = new String[] {"format partition", "remove partition"};

            Options partitionsOpt = new Options(context);
            int choice = (int) partitionsOpt.show("Select an operation", operations);
            switch (choice) {
                case 0:
                    formatPartition(partition);
                    break;
                case 1:
                    removePartition(partition);
                    break;
            }
        } else {
            final String[] operations = new String[] {"add partition"};

            Options partitionsOpt = new Options(context);
            int choice = (int) partitionsOpt.show("Select an operation", operations);
            switch (choice) {
                case 0:
                    createPartition(partition);
                    break;
            }
        }
    }

    private void removePartition(Partition partition) throws Exception {
        YesNo yesNo = new YesNo(context);
        boolean remove = yesNo.show("Would like you to remove the partition ?");

        if (remove) {
            UserFacade.getInstance().removePartition(partition.getStart() + 1);
        }
    }

    private void formatPartition(Partition partition) throws Exception {
        String[] formatters = UserFacade.getInstance().getFormatters();
        Options partitionsOpt = new Options(context);
        int choice = (int) partitionsOpt.show("Select a filesystem", formatters);
        String formatter = formatters[choice];

        UserFacade.getInstance().selectFormatter(formatter);

        UserFacade.getInstance().formatPartition(partition.getStart() + 1);
    }
}
