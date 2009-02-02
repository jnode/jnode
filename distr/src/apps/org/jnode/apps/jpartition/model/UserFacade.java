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
 
package org.jnode.apps.jpartition.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jnode.apps.jpartition.ErrorReporter;
import org.jnode.apps.jpartition.commands.CreatePartitionCommand;
import org.jnode.apps.jpartition.commands.FormatPartitionCommand;
import org.jnode.apps.jpartition.commands.RemovePartitionCommand;
import org.jnode.apps.jpartition.commands.framework.Command;
import org.jnode.apps.jpartition.commands.framework.CommandProcessor;
import org.jnode.apps.jpartition.commands.framework.CommandProcessorListener;
import org.jnode.driver.bus.ide.IDEDevice;
import org.jnode.fs.FileSystem;
import org.jnode.fs.Formatter;
import org.jnode.fs.ext2.BlockSize;
import org.jnode.fs.ext2.Ext2FileSystemFormatter;
import org.jnode.fs.fat.FatFileSystemFormatter;
import org.jnode.fs.fat.FatType;
import org.jnode.fs.jfat.ClusterSize;

public class UserFacade {
    private static final Logger LOG = Logger.getLogger(UserFacade.class);

    static {
        LOG.setLevel(Level.DEBUG);
    }

    private static final UserFacade INSTANCE = new UserFacade();

    private final Map<String, Device> devices = new HashMap<String, Device>();
    private Device selectedDevice;
    private UserListener userListener;

    private final Map<String, Formatter<? extends FileSystem<?>>> formatters =
            new HashMap<String, Formatter<? extends FileSystem<?>>>();
    private Formatter<? extends FileSystem<?>> selectedFormatter;
    private ErrorReporter errorReporter;

    private CommandProcessor cmdProcessor;

    public static UserFacade getInstance() {
        return INSTANCE;
    }

    private UserFacade() {
        refreshDevicesFromOS();

        addFormatter(new FatFileSystemFormatter(FatType.FAT32));
        addFormatter(new org.jnode.fs.jfat.FatFileSystemFormatter(ClusterSize._16Kb));
        addFormatter(new Ext2FileSystemFormatter(BlockSize._4Kb));
    }

    public void setErrorReporter(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        cmdProcessor = new CommandProcessor(errorReporter);
    }

    public void selectFormatter(String name) {
        selectedFormatter = formatters.get(name);
    }

    public void selectDevice(String name) {
        selectDevice(name, false); // called by the user => no need to notify
    }

    public Device getSelectedDevice() {
        return selectedDevice;
    }

    private void selectDevice(String name, boolean notify) {
        selectedDevice = devices.get(name);
        if (notify && (userListener != null)) {
            userListener.selectionChanged(selectedDevice);
        }
    }

    public void setUserListener(UserListener listener) {
        this.userListener = listener;

        OSListener osListener = new OSListener() {
            public void deviceAdded(Device addedDevice) {
                devices.put(addedDevice.getName(), addedDevice);
                userListener.deviceAdded(addedDevice.getName());
            }

            public void deviceRemoved(Device removedDevice) {
                devices.remove(removedDevice.getName());
                userListener.deviceRemoved(removedDevice.getName());

                if ((selectedDevice != null) && selectedDevice.equals(removedDevice)) {
                    selectDevice(null, true); // not called by user => need to
                                                // notify
                }
            }

            public void errorHappened(OSFacadeException e) {
                if (errorReporter != null) {
                    errorReporter.reportError(LOG, UserFacade.this, e);
                }
            }
        };

        try {
            OSFacade.getInstance().setOSListener(osListener);
        } catch (OSFacadeException e) {
            osListener.errorHappened(e);
        }
    }

    public void addCommandProcessorListener(CommandProcessorListener listener) {
        cmdProcessor.addListener(listener);
    }

    public String[] getFormatters() {
        String[] names = formatters.keySet().toArray(new String[formatters.size()]);
        Arrays.sort(names);
        return names;
    }

    public String[] getDeviceNames() {
        String[] names = devices.keySet().toArray(new String[devices.size()]);
        Arrays.sort(names);
        return names;
    }

    public List<Device> getDevices() {
        List<Device> devs = new ArrayList<Device>(devices.values());
        Collections.sort(devs, new Comparator<Device>() {
            public int compare(Device dev1, Device dev2) {
                return dev1.getName().compareTo(dev2.getName());
            }
        });
        return devs;
    }

    public List<Partition> getPartitions() throws Exception {
        checkSelectedDevice();
        return selectedDevice.getPartitions();
    }

    public Partition createPartition(long start, long size) throws Exception {
        checkSelectedDevice();

        Partition newPart = selectedDevice.addPartition(start, size);
        cmdProcessor.addCommand(new CreatePartitionCommand((IDEDevice) selectedDevice.getDevice(),
                0, start, size));

        return newPart;
    }

    public void removePartition(long offset) throws Exception {
        checkSelectedDevice();

        selectedDevice.removePartition(offset);
        cmdProcessor.addCommand(new RemovePartitionCommand((IDEDevice) selectedDevice.getDevice(),
                0)); // TODO set parameters
    }

    public void formatPartition(long offset) throws Exception {
        checkSelectedDevice();
        checkSelectedFormatter();

        Formatter<? extends FileSystem<?>> formatter = selectedFormatter.clone();
        selectedDevice.formatPartition(offset, formatter);
        Command cmd =
                new FormatPartitionCommand((IDEDevice) selectedDevice.getDevice(), 0, formatter); // TODO
                                                                                                    // set
                                                                                                    // parameters
        cmdProcessor.addCommand(cmd);
    }

    public void resizePartition(long offset, long size) throws Exception {
        checkSelectedDevice();

        // selectedDevice.resizePartition(offset, selectedFormatter.clone());
        // cmdProcessor.addCommand(new FormatPartitionCommand((IDEDevice)
        // selectedDevice.getDevice(), 0)); //TODO set parameters
    }

    public void applyChanges() {
        cmdProcessor.process();
        refreshDevicesFromOS();
    }

    public List<Command> getPendingCommands() {
        return cmdProcessor.getPendingCommands();
    }

    private void refreshDevicesFromOS() {
        devices.clear();
        String selectedDev = (selectedDevice == null) ? null : selectedDevice.getName();

        try {
            for (Device device : OSFacade.getInstance().getDevices()) {
                devices.put(device.getName(), device);
            }

            selectDevice(selectedDev, true); // not called by user => need to
                                                // notify
        } catch (OSFacadeException e) {
            if (errorReporter != null) {
                errorReporter.reportError(LOG, this, e);
            }
        }
    }

    private void checkSelectedDevice() throws Exception {
        if (selectedDevice == null) {
            throw new Exception("no device selected");
        }
    }

    private void checkSelectedFormatter() throws Exception {
        if (selectedFormatter == null) {
            throw new Exception("no formatter selected");
        }
    }

    private void addFormatter(Formatter<? extends FileSystem<?>> formatter) {
        formatters.put(formatter.getFileSystemType().getName(), formatter);
    }
}
