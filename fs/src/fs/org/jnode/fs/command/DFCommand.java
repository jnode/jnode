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
 
package org.jnode.fs.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.fs.FileSystem;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;

/**
 * The DF command prints disk usage information for devices with filesystems.
 *
 * @author galatnm@jnode.org
 * @author crawley@jnode.org
 * @author Levente S\u00e1ntha
 */
public class DFCommand extends AbstractCommand {

    private final DeviceArgument ARG_DEVICE = new DeviceArgument(
        "device", Argument.OPTIONAL | Argument.EXISTING,
        "The device for which disk usage inforrmation should be displayed");

    public DFCommand() {
        super("Print file system usage information");
        registerArguments(ARG_DEVICE);
    }

    public void execute() throws NameNotFoundException {
        final FileSystemService fss = InitialNaming.lookup(FileSystemService.NAME);
        final Map<String, String> mountPoints = fss.getDeviceMountPoints();
        PrintWriter out = getOutput().getPrintWriter(false);
        format(out, "ID", true);
        format(out, "Size", false);
        format(out, "Used", false);
        format(out, "Free", false);
        out.println("Mount");
        out.println();
        if (ARG_DEVICE.isSet()) {
            final Device dev = ARG_DEVICE.getValue();
            FileSystem<?> fs = fss.getFileSystem(dev);
            if (fs == null) {
                out.println("No filesystem on device");
            } else {
                displayInfo(out, dev, fs, mountPoints.get(fs.getDevice().getId()));
            }
        } else {
            final DeviceManager dm = InitialNaming.lookup(DeviceManager.NAME);
            for (Device dev : dm.getDevices()) {
                FileSystem<?> fs = fss.getFileSystem(dev);
                if (fs != null) {
                    displayInfo(out, dev, fs, mountPoints.get(fs.getDevice().getId()));
                }
            }
        }
        out.flush();
    }

    /**
     * @param out
     * @param dev
     * @param fs
     * @param mountPoint
     */
    private void displayInfo(PrintWriter out, Device dev, FileSystem<?> fs, String mountPoint) {
        try {

            String str = dev.getId();
            format(out, str, true);

            final long total = fs.getTotalSpace();
            str = total < 0 ? "unknown" : String.valueOf(total);
            format(out, str, false);

            final long free = fs.getFreeSpace();
            str = total < 0 ? "unknown" : String.valueOf(total - free);
            format(out, str, false);

            str = free < 0 ? "unknown" : String.valueOf(free);
            format(out, str, false);

            out.println(mountPoint);
        } catch (IOException ex) {
            out.println("\tError getting disk usage information for " + mountPoint + " on " + dev.getId() +
                " : " + ex.getLocalizedMessage());
        }
    }

    private void format(PrintWriter out, String str, boolean left) {
        int ln;
        ln = 15 - str.length();
        if (ln < 0) {
            str = str.substring(0, 15); 
        } else {
            if (left) {
                out.print(str);
            }
            for (int i = 0; i < ln; i++) out.print(' ');
        }
        if (!left) {
            out.print(str);
        }
        out.print(' ');
    }
}
