/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.fs.command;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DriverException;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.FileSystemType;
import org.jnode.fs.ext2.Ext2FileSystemType;
import org.jnode.fs.fat.Fat;
import org.jnode.fs.fat.FatFileSystemType;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.help.DeviceArgument;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.OptionArgument;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.SyntaxErrorException;

/**
 * @author gbin
 */
public class FormatCommand {

    static final OptionArgument TYPE = new OptionArgument("action",
            "Type parameter",
            new OptionArgument.Option[] { new OptionArgument.Option("-t",
                    "Specify fs type") });

    static final OptionArgument FS = new OptionArgument("fstype",
            "File system type", new OptionArgument.Option[] {
                    new OptionArgument.Option("fat16", "FAT 16 filesystem"),
                    new OptionArgument.Option("fat12", "FAT 12 filesystem"),
                    new OptionArgument.Option("ext2", "EXT2 filesystem"), });

    static final OptionArgument BS_VAL = new OptionArgument("blocksize",
            "block size for ext2 filesystem", new OptionArgument.Option[] {
                    new OptionArgument.Option("1", "1Kb"),
                    new OptionArgument.Option("2", "2Kb"),
                    new OptionArgument.Option("4", "4Kb"), });

    static final DeviceArgument ARG_DEVICE = new DeviceArgument("device-id",
            "the device to print informations about");

    static final Parameter PARAM_TYPE = new Parameter(TYPE, Parameter.MANDATORY);

    static final Parameter PARAM_FS = new Parameter(FS, Parameter.MANDATORY);

    static final Parameter PARAM_BS_VAL = new Parameter(BS_VAL,
            Parameter.OPTIONAL);

    static final Parameter PARAM_DEVICE = new Parameter(ARG_DEVICE,
            Parameter.MANDATORY);

    public static Help.Info HELP_INFO = new Help.Info("format",
            new Syntax[] { new Syntax(
                    "Format a block device with a specified type",
                    new Parameter[] { PARAM_TYPE, PARAM_FS, PARAM_DEVICE,
                            PARAM_BS_VAL }) });

    public static void main(String[] args) throws SyntaxErrorException {
        try {
            ParsedArguments cmdLine = HELP_INFO.parse(args);

            String device = ARG_DEVICE.getValue(cmdLine);
            String FSType = FS.getValue(cmdLine).intern();
            Integer bsize;
            try {
                bsize = Integer.valueOf(BS_VAL.getValue(cmdLine));
            } catch (NumberFormatException nfe) {
                bsize = new Integer(4);
            }

            String fsTypeName;
            Object params;

            int fatSize = 0;
            if (FSType == "fat16") {
                fatSize = Fat.FAT16;
                fsTypeName = FatFileSystemType.NAME;
                params = new Integer(fatSize);
            } else if (FSType == "fat12") {
                fatSize = Fat.FAT12;
                fsTypeName = FatFileSystemType.NAME;
                params = new Integer(fatSize);
            } else if (FSType == "ext2") {
                fsTypeName = Ext2FileSystemType.NAME;
                params = bsize;
            } else
                throw new FileSystemException(
                        "Unsupported FS by format command");

            DeviceManager dm;

            dm = (DeviceManager) InitialNaming.lookup(DeviceManager.NAME);

            Device dev = dm.getDevice(device);
            FileSystemService fileSystemService = InitialNaming
                    .lookup(FileSystemService.NAME);
            FileSystemType type = fileSystemService
                    .getFileSystemTypeForNameSystemTypes(fsTypeName);
            type.format(dev, params);

            // restart the device
            dm.stop(dev);
            dm.start(dev);

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (DeviceNotFoundException e) {
            e.printStackTrace();
        } catch (DriverException e) {
            e.printStackTrace();
        } catch (FileSystemException e) {
            // 
            e.printStackTrace();
        }
    }

}
