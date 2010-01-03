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
 
package org.jnode.command.file;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.naming.NameNotFoundException;

import java.io.File;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceManager;
import org.jnode.fs.FileSystem;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.DeviceArgument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.util.NumberUtils;

/**
 * The DF command prints disk usage information for devices with filesystems.
 *
 * @author galatnm@jnode.org
 * @author crawley@jnode.org
 * @author Levente S\u00e1ntha
 * @author chris boertien
 */
public class DFCommand extends AbstractCommand {
    
    private static final String help_device = "The device for which disk usage information should be displayed";
    private static final String help_path = "Display disk usage info for the file system that contains this path";
    private static final String help_read_dec = "Print output in human readable decimal form (1000)";
    private static final String help_read_bin = "Print output in human readable binary form (1024)";
    private static final String help_all = "Show all file systems, even pseudo file systems";
    private static final String help_block_1k = "Same as -B 1024";
    private static final String help_block = "Print output with a specified block size";
    private static final String help_super = "Print file system usage information";
    private static final String str_id = "ID";
    private static final String str_size = "Size";
    private static final String str_blocks = "blocks";
    private static final String str_used = "Used";
    private static final String str_free = "Free";
    private static final String str_mount = "Mount";
    private static final String str_no_fs = "No filesystem on device";
    private static final String str_unknown = "unknown";
    private static final String err_get_info = "\tError getting disk usage information for %s on %s : %s%n";
    
    private static final int OUT_BINARY  = 1;
    private static final int OUT_DECIMAL = 2;
    private static final int OUT_BLOCKS  = 3;
    
    private static final int DEFAULT_BLOCK_SIZE = 1024;
    
    private final DeviceArgument argDevice;
    private final FileArgument argPath;
    private final FlagArgument argReadDec;
    private final FlagArgument argReadBin;
    private final FlagArgument argAll;
    private final FlagArgument argBlock1k;
    private final IntegerArgument argBlock;
    
    private FileSystemService fss;
    private DeviceManager dm;
    private Map<String, String> mountPoints;
    private PrintWriter out;
    private int outputType;
    private int blockSize;
    @SuppressWarnings("unused")
    private boolean all;
    
    public DFCommand() {
        super(help_super);
        argDevice  = new DeviceArgument("device", Argument.EXISTING, help_device);
        argPath    = new FileArgument("path", Argument.EXISTING, help_path);
        argReadDec = new FlagArgument("human-read-dec", 0, help_read_dec);
        argReadBin = new FlagArgument("human-read-bin", 0, help_read_bin);
        argAll     = new FlagArgument("show-all", 0, help_all);
        argBlock1k = new FlagArgument("block-size-1k", 0, help_block_1k);
        argBlock   = new IntegerArgument("block-size", 0, help_block);
        registerArguments(argDevice, argPath, argReadDec, argReadBin, argAll, argBlock1k, argBlock);
    }
    
    public void execute() throws NameNotFoundException, IOException {
        parseOptions();
        fss         = InitialNaming.lookup(FileSystemService.NAME);
        dm          = InitialNaming.lookup(DeviceManager.NAME);
        mountPoints = fss.getDeviceMountPoints();
        out         = getOutput().getPrintWriter(true);
        
        Device device = null;
        
        printHeader();
        
        if (argPath.isSet()) {
            device = getDeviceForPath(argPath.getValue());
        } else if (argDevice.isSet()) {
            device = argDevice.getValue();
        } else {
            for (Device dev : dm.getDevices()) {
                FileSystem<?> fs = fss.getFileSystem(dev);
                if (fs != null) {
                    displayInfo(out, dev, fs, mountPoints.get(fs.getDevice().getId()));
                }
            }
            out.flush();
            exit(0);
        }
        if (device != null) {
            FileSystem<?> fs = fss.getFileSystem(device);
            if (fs == null) {
                out.println(str_no_fs);
            } else {
                displayInfo(out, device, fs, mountPoints.get(fs.getDevice().getId()));
            }
            out.flush();
            exit(0);
        }
    }
    
    private Device getDeviceForPath(File file) throws IOException {
        String path = file.getCanonicalPath();
        String mp = null;
        for (String mountPoint : fss.getMountPoints().keySet()) {
            if (path.startsWith(mountPoint)) {
                if (mp != null) {
                    if (!mp.startsWith(mountPoint)) {
                        continue;
                    }
                }
                mp = mountPoint;
            }
        }
        if (mp == null) {
            throw new AssertionError("No fs device for " + path);
        }
        return fss.getMountPoints().get(mp).getDevice();
    }
    
    private void printHeader() {
        format(out, str_id, true);
        if (outputType == OUT_BLOCKS) {
            format(out, String.format("%s-%s", NumberUtils.toBinaryByte(blockSize), str_blocks), false);
        } else {
            format(out, str_size, false);
        }
        format(out, str_used, false);
        format(out, str_free, false);
        out.println(str_mount);
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
            long total = fs.getTotalSpace();
            long free = fs.getFreeSpace();
            
            format(out, str, true);
            
            str = total < 0 ? str_unknown : valueOf(total, true);
            format(out, str, false);
            
            str = total < 0 ? str_unknown : valueOf(total - free, true);
            format(out, str, false);
            
            str = free < 0 ? str_unknown : valueOf(free, false);
            format(out, str, false);
            
            out.println(mountPoint);
        } catch (IOException ex) {
            out.format(err_get_info, mountPoint, dev.getId(), ex.getLocalizedMessage());
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
    
    private String valueOf(long size, boolean up) {
        switch(outputType) {
            case OUT_DECIMAL :
                return NumberUtils.toDecimalByte(size, 0);
            case OUT_BINARY :
                return NumberUtils.toBinaryByte(size, 0);
            case OUT_BLOCKS :
                return toBlock(size, blockSize, up);
            default :
                return String.valueOf(size);
        }
    }
    
    private String toBlock(long size, long blockSize, boolean up) {
        return String.valueOf(size / blockSize + ((up && ((size % blockSize) > 0)) ? 1 : 0));
    }
    
    private void parseOptions() {
        if (argReadDec.isSet()) {
            outputType = OUT_DECIMAL;
        } else if (argReadBin.isSet()) {
            outputType = OUT_BINARY;
        } else {
            outputType = OUT_BLOCKS;
        }
        
        all = argAll.isSet();
        
        if (argBlock1k.isSet()) {
            blockSize = 1024;
        } else if (argBlock.isSet()) {
            blockSize = argBlock.getValue();
        } else {
            blockSize = getDefaultBlock();
        }
    }
    
    private int getDefaultBlock() {
        /* Env vars are broken
        String DF_BLOCK_SIZE = System.getenv("DF_BLOCK_SIZE");
        String BLOCK_SIZE = System.getenv("BLOCK_SIZE");
        String BLOCKSIZE = System.getenv("BLOCKSIZE");
        String POSIXLY_CORRECT = System.getenv("POSIXLY_CORRECT");
        
        String size = null;
        if (DF_BLOCK_SIZE != null) {
            size = DF_BLOCK_SIZE;
        } else if (BLOCK_SIZE != null) {
            size = BLOCK_SIZE;
        } else if (BLOCKSIZE != null) {
            size = BLOCKSIZE;
        } else if (POSIXLY_CORRECT != null) {
            return 512;
        } else {
            return DEFAULT_BLOCK_SIZE;
        }
        
        try {
            return Integer.parseInt(size);
        } catch (NumberFormatException e) {
            return DEFAULT_BLOCK_SIZE;
        }
        */
        return DEFAULT_BLOCK_SIZE;
    }
}
