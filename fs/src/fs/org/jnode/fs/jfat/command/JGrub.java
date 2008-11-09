/*
 * $Id: FatConstants.java 2224  Tanmoy $
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


package org.jnode.fs.jfat.command;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.fs.FileSystem;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;

/**
 * The Grub Installer for JNode.
 * 
 * TODO: Adding more options for supporting JGRUB with user specified File
 * System wise. Adding more command support for grub installation.
 * 
 * @author Tango Devian
 */
public class JGrub {
    private final PrintWriter out;
    private final MBRFormatter stage1;
    private final Stage1_5 stage1_5;
    private final Stage2 stage2;
    private final Device device;
    private final String mountPoint;

    public JGrub(PrintWriter out, Device device) throws GrubException {
        this(out, device, new MBRFormatter(), new Stage1_5(), new Stage2());
    }

    protected JGrub(PrintWriter out, Device device, MBRFormatter stage1,
            Stage1_5 stage1_5, Stage2 stage2) throws GrubException {
        this.out = out;
        this.stage1 = stage1;
        this.stage1_5 = stage1_5;
        this.stage2 = stage2;
        this.device = device;

        mountPoint = getMountPoint(device);
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void install() throws GrubException {
        final String deviceName = device.getId();
        out.println("Installing GRUB to " + deviceName);

        DeviceManager dm;
        try {
            dm = DeviceUtils.getDeviceManager();
        } catch (NameNotFoundException e1) {
            throw new GrubException("can't find device manager", e1);
        }

        // workaround. TODO add a new interface to FileSystemService to find
        // parent device ?
        int i = deviceName.length() - 1;
        while ((i >= 0) && Character.isDigit(deviceName.charAt(i))) {
            i--;
        }
        final String parentDeviceName = deviceName.substring(0, i + 1);
        final int partitionNumber = Integer.parseInt(deviceName.substring(i + 1));
        //

        final Device parentDevice = getDevice(dm, parentDeviceName);
        final BlockDeviceAPI parentDeviceApi = getBlockDeviceAPI(parentDevice);

        stage1.format(parentDeviceApi);
        stage1_5.format(parentDeviceApi, partitionNumber);

        stage2.format(mountPoint);

        restart(dm, parentDevice);
        out.println("GRUB has been successfully installed to " + deviceName + ".");
    }

    private String getMountPoint(Device device) throws GrubException {
        FileSystemService fss = null;
        try {
            fss = InitialNaming.lookup(FileSystemService.NAME);
        } catch (NameNotFoundException e) {
            throw new GrubException("filesystem not found", e);
        }

        FileSystem<?> filesystem = fss.getFileSystem(device);
        if (filesystem == null) {
            throw new GrubException("can't find filesystem for device " + device.getId());
        }

        Map<String, FileSystem<?>> mountPoints = fss.getMountPoints();
        String mountPoint = null;
        for (String fullPath : mountPoints.keySet()) {
            FileSystem<?> fs = mountPoints.get(fullPath);
            if (fs == filesystem) {
                mountPoint = fullPath;
                break;
            }
        }

        if (mountPoint == null) {
            throw new GrubException("can't find mount point for filesystem " + filesystem);
        }

        if (!mountPoint.endsWith(File.separator)) {
            mountPoint += File.separatorChar;
        }

        return mountPoint;
    }

    private void restart(DeviceManager dm, Device device) throws GrubException {
        out.println("Restarting device " + device.getId());
        try {
            dm.stop(device);
            dm.start(device);
        } catch (DeviceNotFoundException e) {
            throw new GrubException("device not found : " + device.getId(), e);
        } catch (DriverException e) {
            throw new GrubException("device must be a partition device", e);
        }
    }

    private Device getDevice(DeviceManager dm, String deviceName) throws GrubException {
        Device parentDevice = null;
        try {
            parentDevice = dm.getDevice(deviceName);
        } catch (DeviceNotFoundException e1) {
            throw new GrubException("can't find device with name " + deviceName, e1);
        }
        return parentDevice;
    }

    private BlockDeviceAPI getBlockDeviceAPI(Device device) throws GrubException {
        BlockDeviceAPI deviceApi = null;
        try {
            deviceApi = device.getAPI(BlockDeviceAPI.class);
        } catch (ApiNotFoundException e) {
            throw new GrubException("device must be a partition device", e);
        }

        return deviceApi;
    }
}
