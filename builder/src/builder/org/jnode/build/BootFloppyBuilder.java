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
 
package org.jnode.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.block.FileDevice;
import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.FSFile;
import org.jnode.fs.FileSystemException;
import org.jnode.fs.fat.FatFileSystem;
import org.jnode.fs.fat.FatFileSystemType;
import org.jnode.fs.fat.GrubFatFormatter;
import org.jnode.util.FileUtils;

/**
 * Builder for the boot floppy (in fact, it's not a floppy but a cdrom image in the iso format).
 *
 * @author epr
 */
public class BootFloppyBuilder extends Task {

    private File destFile;

    private String stage1ResourceName;

    private String stage2ResourceName;

    private ArrayList<FileSet> fileSets = new ArrayList<FileSet>();

    /**
     * Build the boot floppy.
     *
     * @throws BuildException
     */
    public void execute() throws BuildException {

        try {
            if (isExecuteNeeded()) {
                createImage();
            }
        } catch (Throwable ex) {
            ex.printStackTrace(System.err);
            throw new BuildException(ex);
        }
    }

    protected boolean isExecuteNeeded() {
        final long lmDest = destFile.lastModified();
        return (getLastModified() > lmDest);
    }

    /**
     * Create the actual bootfloppy.
     *
     * @throws IOException
     * @throws DriverException
     * @throws FileSystemException
     */
    public void createImage() throws IOException, DriverException,
        FileSystemException {

        final FileDevice newFd = new FileDevice(destFile, "rw");
        try {
            newFd.setLength(getDeviceLength());
            formatDevice(newFd);
            final Device sysDev = getSystemDevice(newFd);
            final BlockDeviceAPI sysDevApi = sysDev
                .getAPI(BlockDeviceAPI.class);
            copySystemFiles(sysDev);
            sysDevApi.flush();
        } catch (ApiNotFoundException ex) {
            final IOException ioe = new IOException("BlockDeviceAPI not found on device");
            ioe.initCause(ex);
            throw ioe;
        } finally {
            newFd.close();
        }
    }

    /**
     * Format the given device.
     *
     * @param dev
     * @throws IOException
     */
    protected void formatDevice(Device dev) throws IOException {
        GrubFatFormatter ff = createFormatter();
        try {
            ff.format(dev.getAPI(BlockDeviceAPI.class));
        } catch (ApiNotFoundException ex) {
            final IOException ioe = new IOException("BlockDeviceAPI not found on device");
            ioe.initCause(ex);
            throw ioe;
        }
    }

    /**
     * Gets the device the system files must be copied onto. This enabled a
     * disk to be formatted with partitions.
     *
     * @param rootDevice
     * @return BlockDevice
     */
    protected Device getSystemDevice(Device rootDevice) {
        return rootDevice;
    }

    /**
     * Copy the system files to the given device.
     *
     * @param device
     * @throws IOException
     * @throws FileSystemException
     */
    protected void copySystemFiles(Device device) throws IOException,
        FileSystemException {
        final FatFileSystem fs = new FatFileSystem(device, false, new FatFileSystemType());

        for (FileSet fset : fileSets) {
            processFileSet(fs, fset);
        }

        fs.close();
    }

    private void processFileSet(FatFileSystem fs, FileSet fset) throws IOException {
        final DirectoryScanner ds = fset.getDirectoryScanner(getProject());
        final String[] dirs = ds.getIncludedDirectories();
        for (int i = 0; i < dirs.length; i++) {
            getOrCreateDir(fs, dirs[i]);
        }
        final String[] files = ds.getIncludedFiles();
        for (int i = 0; i < files.length; i++) {
            final String fn = files[i];
            final int idx = fn.lastIndexOf(File.separatorChar);
            final FSDirectory dir;
            final String name;
            if (idx >= 0) {
                dir = getOrCreateDir(fs, fn.substring(0, idx));
                name = fn.substring(idx + 1);
            } else {
                dir = getOrCreateDir(fs, "");
                name = fn;
            }
            final File f = new File(ds.getBasedir(), fn);
            addFile(dir, f, name);
        }
    }

    /**
     * Gets the last modification date of all parameters.
     *
     * @return
     */
    protected long getLastModified() {
        long lm = 0l;
        for (FileSet fset : fileSets) {
            lm = Math.max(lm, getLastModified(fset));
        }
        return lm;
    }

    private long getLastModified(FileSet fset) {
        final DirectoryScanner ds = fset.getDirectoryScanner(getProject());
        final File baseDir = ds.getBasedir();
        long lm = 0l;
        final String[] dirs = ds.getIncludedDirectories();
        for (int i = 0; i < dirs.length; i++) {
            lm = Math.max(lm, new File(baseDir, dirs[i]).lastModified());
        }
        final String[] files = ds.getIncludedFiles();
        for (int i = 0; i < files.length; i++) {
            lm = Math.max(lm, new File(baseDir, files[i]).lastModified());
        }
        return lm;

    }

    private FSDirectory getOrCreateDir(FatFileSystem fs, String dirName)
        throws IOException {
        FSDirectory dir = fs.getRootDir();
        while (dirName.length() > 0) {
            final int idx = dirName.indexOf(File.separatorChar);
            final String part;
            if (idx >= 0) {
                part = dirName.substring(0, idx);
                dirName = dirName.substring(idx + 1);
            } else {
                part = dirName;
                dirName = "";
            }
            FSEntry entry;
            try {
                entry = dir.getEntry(part);
            } catch (IOException ex) {
                // Ignore
                entry = null;
            }
            if (entry == null) {
                entry = dir.addDirectory(part);
            }
            dir = entry.getDirectory();
        }
        return dir;
    }

    /**
     * Add a given file to a given directory with a given filename.
     *
     * @param dir
     * @param src
     * @param fname
     * @throws IOException
     */
    private void addFile(FSDirectory dir, File src, String fname)
        throws IOException {

        long size = src.length();
        /*
         * log.info( "Adding " + src + " as " + fname + " size " + (size /
         * 1024) + "Kb");
         */

        //final byte[] buf = new byte[ (int) size];
        final ByteBuffer buf = ByteBuffer.allocate((int) size);
        InputStream is = new FileInputStream(src);
        FileUtils.copy(is, buf.array());
        is.close();

        final FSFile fh = dir.addFile(fname).getFile();
        fh.setLength(size);
        //fh.write(0, buf, 0, buf.length);
        fh.write(0, buf);

        log("Added " + src + " as " + fname + " size " + (size / 1024) + "Kb");
    }

    /**
     * Returns the destFile.
     *
     * @return File
     */
    public File getDestFile() {
        return destFile;
    }

    /**
     * Sets the destFile.
     *
     * @param destFile The destFile to set
     */
    public void setDestFile(File destFile) {
        this.destFile = destFile;
    }

    protected GrubFatFormatter createFormatter() throws IOException {
        return new GrubFatFormatter(0, stage1ResourceName, stage2ResourceName);
    }

    protected long getDeviceLength() {
        return 1440 * 1024;
    }

    /**
     * @return Returns the stage1ResourceName.
     */
    public final String getStage1ResourceName() {
        return this.stage1ResourceName;
    }

    /**
     * @param stage1ResourceName The stage1ResourceName to set.
     */
    public final void setStage1ResourceName(String stage1ResourceName) {
        this.stage1ResourceName = stage1ResourceName;
    }

    /**
     * @return Returns the stage2ResourceName.
     */
    public final String getStage2ResourceName() {
        return this.stage2ResourceName;
    }

    /**
     * @param stage2ResourceName The stage2ResourceName to set.
     */
    public final void setStage2ResourceName(String stage2ResourceName) {
        this.stage2ResourceName = stage2ResourceName;
    }

    /**
     * Add a fileset to this task.
     *
     * @return
     */
    public FileSet createFileset() {
        final FileSet fs = new FileSet();
        fileSets.add(fs);
        return fs;
    }
}
