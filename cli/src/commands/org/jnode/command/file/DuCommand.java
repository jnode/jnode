/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Stack;
import javax.naming.NameNotFoundException;
import org.apache.log4j.Logger;
import org.jnode.command.util.AbstractDirectoryWalker;
import org.jnode.driver.Device;
import org.jnode.driver.block.FSBlockDeviceAPI;
import org.jnode.fs.service.FileSystemService;
import org.jnode.naming.InitialNaming;
import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.IntegerArgument;
import org.jnode.shell.syntax.StringArgument;
import org.jnode.util.NumberUtils;
import org.jnode.vm.VmExit;

/**
 * @author Alexander Kerner
 * @author Mario Zsilak
 */
public class DuCommand extends AbstractCommand {

    private static final String HELP_SUPER =
            "With no arguments, `du' reports the disk space for the current directory. Normally the disk space is " +
                "printed in units of 1024 bytes, but this can be overridden";
    private static final String HELP_DIR = "directory to start printing sizes recursively";
    private static final String HELP_ALL = "Show counts for all files, not just directories.";
    private static final String HELP_BLOCK_SIZE_1 =
            "Print sizes in bytes, overriding the default block size";
    private static final String HELP_TOTAL =
            "Print a grand total of all arguments after all arguments have been processed. This can be used to " +
                "find out the total disk usage of a given set of files or directories.";
    private static final String HELP_DEREF_ARGS =
            "Dereference symbolic links that are command line arguments. Does not affect other symbolic links.  " +
                "This is helpful for finding out the disk usage of directories, such as `/usr/tmp', " +
                "which are often symbolic links. (not implemented";
    private static final String HELP_HUMAN_READABLE_1024 =
            "Append a size letter such as `M' for megabytes to each size. Powers of 1024 are used, not 1000; " +
                "`M' stands for 1,048,576 bytes. Use the `-H' or `--si' option if you prefer powers of 1000.";
    private static final String HELP_HUMAN_READABLE_1000 =
            "Append a size letter such as `M' for megabytes to each size.  (SI is the International System of Units, " +
                "which defines these letters as prefixes.)  Powers of 1000 are used, not 1024; " +
                "`M' stands for 1,000,000 bytes.  Use the `-h' or `--human-readable' option if you prefer " +
                "powers of 1024.";
    private static final String HELP_BLOCK_SIZE_1024 =
            "Print sizes in 1024-byte blocks, overriding the default block size";
    private static final String HELP_COUNT_LINKS =
            "Count the size of all files, even if they have appeared already (as a hard link). (not implemented";
    private static final String HELP_DEREF =
            "Dereference symbolic links (show the disk space used by the file or directory that the link points to " +
                "instead of the space used by the link). (not implemented";
    private static final String HELP_MAX_DEPTH =
            "Show the total for each directory (and file if -all) that is at most MAX_DEPTH levels down " +
                "from the root of the hierarchy.  The root is at level 0, so `du --max-depth=0' is equivalent " +
                "to `du -s'. (not tested)";
    private static final String HELP_BLOCK_SIZE_1024x1024 =
            "Print sizes in megabyte (that is, 1,048,576-byte) blocks.";
    private static final String HELP_SUM = "Display only a total for each argument.";
    private static final String HELP_SEPERATE_DIRS =
            "Report the size of each directory separately, not including the sizes of subdirectories.";
    private static final String HELP_ONE_FS =
            "Skip directories that are on different filesystems from the one that the argument " +
                "being processed is on. (not implemented)";
    private static final String HELP_EXCLUDE =
            "When recursing, skip subdirectories or files matching PAT. For example, `du --exclude='*.o'' " +
                "excludes files whose names end in `.o'. (not tested)";
    private static final String HELP_EXCLUDE_FROM =
            "Like `--exclude', except take the patterns to exclude from FILE, one per line.  If FILE is `-', " +
                "take the patterns from standard input. (not implemented)";
    private static final String HELP_BLOCK_SIZE_CUSTOM =
            "Print sizes in the user defined block size, overriding the default block size";
    private static final String HELP_FS_BLOCK_SIZE =
            "Overrides the filesystem block size -- use it for testing";

    private final FileArgument argDir =
            new FileArgument("directory",
                    Argument.OPTIONAL | Argument.MULTIPLE | Argument.EXISTING, HELP_DIR);
    private final FlagArgument argAll = new FlagArgument("all", Argument.OPTIONAL, HELP_ALL);
    private final FlagArgument argBlockSize_1 =
            new FlagArgument("block-size-1", Argument.OPTIONAL, HELP_BLOCK_SIZE_1);
    private final FlagArgument argTotal = new FlagArgument("total", Argument.OPTIONAL, HELP_TOTAL);
    private final FlagArgument argDerefArgs =
            new FlagArgument("derefArgs", Argument.OPTIONAL, HELP_DEREF_ARGS);
    private final FlagArgument argHumanReadable_1024 =
            new FlagArgument("human-readable-1024", Argument.OPTIONAL, HELP_HUMAN_READABLE_1024);
    private final FlagArgument argHumanReadable_1000 =
            new FlagArgument("human-readable-1000", Argument.OPTIONAL, HELP_HUMAN_READABLE_1000);
    private final FlagArgument argBlockSize_1024 =
            new FlagArgument("block-size-1024", Argument.OPTIONAL, HELP_BLOCK_SIZE_1024);
    private final FlagArgument argCountLinks =
            new FlagArgument("count-links", Argument.OPTIONAL, HELP_COUNT_LINKS);
    private final FlagArgument argDereference =
            new FlagArgument("dereference", Argument.OPTIONAL, HELP_DEREF);
    private final IntegerArgument argMaxDepth =
            new IntegerArgument("max-depth", Argument.OPTIONAL, HELP_MAX_DEPTH);
    private final FlagArgument argBlockSize_1024x1024 =
            new FlagArgument("block-size-1024x1024", Argument.OPTIONAL, HELP_BLOCK_SIZE_1024x1024);
    private final FlagArgument argSum = new FlagArgument("summarize", Argument.OPTIONAL, HELP_SUM);
    private final FlagArgument argSeperateDirs =
            new FlagArgument("separate-dirs", Argument.OPTIONAL, HELP_SEPERATE_DIRS);
    private static final FlagArgument argOneFS =
            new FlagArgument("one-file-system", Argument.OPTIONAL, HELP_ONE_FS);
    private final StringArgument argExclude =
            new StringArgument("exclude", Argument.OPTIONAL, HELP_EXCLUDE);
    private static final StringArgument argExcludeFrom =
            new StringArgument("exclude-from", Argument.OPTIONAL, HELP_EXCLUDE_FROM);
    private final IntegerArgument argBlockSize_Custom =
            new IntegerArgument("block-size-custom", Argument.OPTIONAL, HELP_BLOCK_SIZE_CUSTOM);
    private final IntegerArgument argFilesystemBlockSize =
            new IntegerArgument("filesystem-block-size", Argument.OPTIONAL, HELP_FS_BLOCK_SIZE);

    private static final String ERR_PERMISSION = "Permission denied for '%s'%n";

    private static final int DEFAULT_FILESYSTEM_BLOCK_SIZE = 1024;
    private static final int DEFAULT_DISPLAY_BLOCK_SIZE = 1024;

    private Logger logger = Logger.getLogger(getClass());

    private int fsBlockSize;
    private int displayBlockSize;
    private PrintWriter out;
    private PrintWriter err;

    public static void main(String[] args) throws IOException {
        new DuCommand().execute();
    }

    public DuCommand() {
        super(HELP_SUPER);
        registerArguments(argDir, argAll, argBlockSize_1, argTotal, argDerefArgs,
                argHumanReadable_1024, argHumanReadable_1000, argBlockSize_1024, argCountLinks,
                argDereference, argMaxDepth, argBlockSize_1024x1024, argSum, argSeperateDirs,
                argOneFS, argExclude, argExcludeFrom, argBlockSize_Custom, argFilesystemBlockSize);
    }

    public void execute() throws IOException {

        Walker walker = null;
        File[] startPoints = null;

        out = getOutput().getPrintWriter();
        err = getError().getPrintWriter();

        if (argAll.isSet() && argSum.isSet()) {
            err.println("Summarize and show all not possible at the some time!");
            throw new VmExit(1);
        }

        if (argDerefArgs.isSet()) {
            logger.warn(argDerefArgs.getLabel() + " is currently not supported");
        }

        if (argOneFS.isSet()) {
            logger.warn(argOneFS.getLabel() + " is currently not supported");
        }

        if (argExcludeFrom.isSet()) {
            logger.warn(argExcludeFrom.getLabel() + " is currently not supported");
        }

        if (argDereference.isSet()) {
            logger.warn(argDereference.getLabel() + " is currently not supported");
        }

        if (argCountLinks.isSet()) {
            logger.warn(argCountLinks.getLabel() + " is currently not supported");
        }

        startPoints = argDir.getValues();

        if (startPoints.length == 0) {
            startPoints = new File[] {new File(System.getProperty("user.dir"))};
        }

        if (argFilesystemBlockSize.isSet())
            fsBlockSize = argFilesystemBlockSize.getValue();
        else {
            fsBlockSize = getFsBlockSize(startPoints[0]);
        }

        if (argBlockSize_Custom.isSet()) {
            displayBlockSize = argBlockSize_Custom.getValue();
        } else if (argBlockSize_1024x1024.isSet()) {
            displayBlockSize = 1024 * 1024;
        } else if (argBlockSize_1024.isSet()) {
            displayBlockSize = 1024;
        } else if (argBlockSize_1.isSet()) {
            displayBlockSize = 1;
        } else {
            displayBlockSize = DEFAULT_DISPLAY_BLOCK_SIZE;
        }

        if (argSum.isSet() || argTotal.isSet()) {

            long total = 0;

            for (File start : startPoints) {
                walker = new Walker(argMaxDepth, argExclude);
                walker.walk(start);

                printSize(start.getAbsolutePath(), walker.getSize());
                total += walker.getSize();
            }

            if (argTotal.isSet()) {
                printSize("Total", total);
            }
        } else {
            new Walker(argMaxDepth, argExclude).walk(startPoints);
        }
    }

    private void printFileSize(final File filename, final long size) {
        if (argAll.isSet()) {
            out.println(sizeToString(size) + '\t' + filename);
        }
    }

    private void printDirSize(final File filename, final long dirSizeOnly, final long subDirSize) {
        if (!argSum.isSet()) {
            if (argSeperateDirs.isSet()) {
                out.println(sizeToString(dirSizeOnly) + '\t' + filename);
            } else {
                out.println(sizeToString(dirSizeOnly + subDirSize) + '\t' + filename);
            }
        }
    }

    private void printSize(final String filename, final long size) {
        out.println(sizeToString(size) + '\t' + filename);
    }

    private void log(String message) {
        // logger.debug(message);
    }

    /**
     * should be in NumberUtils I guess
     * 
     * @param lenght in bytes of the file / directory
     * @return the number of blocks it uses up depending on the int
     *         displayBlockSize
     */
    private long calc(long bytes) {

        double factor = fsBlockSize / displayBlockSize;
        long ret = -1;

        if (fsBlockSize > displayBlockSize) {
            if (bytes % displayBlockSize == 0) {
                ret = bytes / displayBlockSize;
            } else {
                ret = (long) ((bytes / (fsBlockSize) + 1) * factor);
            }
        } else {
            if (bytes % displayBlockSize == 0) {
                ret = bytes / displayBlockSize;
            } else {
                ret = (long) ((bytes / (displayBlockSize) + 1));
            }
        }

        return ret;
    }

    private String sizeToString(long size) {

        String retValue = null;

        if (argHumanReadable_1024.isSet()) {
            retValue = NumberUtils.toBinaryByte(size);
        } else if (argHumanReadable_1000.isSet()) {
            retValue = NumberUtils.toDecimalByte(size);
        } else {
            retValue = String.valueOf(size);
        }

        return retValue;
    }

    /**
     * taken from the DfCommand
     */
    private int getFsBlockSize(File file) throws IOException {

        int retValue = DEFAULT_FILESYSTEM_BLOCK_SIZE; // default block size
        FileSystemService fss = null;
        Device device = null;
        String path = null;
        String mp = null;

        try {
            fss = InitialNaming.lookup(FileSystemService.NAME);
            path = file.getCanonicalPath();
            mp = null;

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

            if (mp != null) {
                device = fss.getMountPoints().get(mp).getDevice();

                if (device instanceof FSBlockDeviceAPI) {
                    retValue = ((FSBlockDeviceAPI) device).getSectorSize();

                } else {
                    logger.warn("No FSBlockDeviceAPI device for device: " + device);
                    logger.info("Using default block-size: " + DEFAULT_FILESYSTEM_BLOCK_SIZE);
                    logger.info("override with --fs-block-size");
                }
            } else {

                logger.warn("No mount point found for " + path);

                for (String mountPoint : fss.getMountPoints().keySet()) {
                    logger.warn("mountpoints on system: " + mountPoint);
                }

                logger.info("Using default block-size: " + DEFAULT_FILESYSTEM_BLOCK_SIZE);
                logger.info("override with --fs-block-size");
            }

        } catch (NameNotFoundException e) {
            logger.warn("FileSystemService lookup failed ...", e);
            logger.info("Using default block-size: " + DEFAULT_FILESYSTEM_BLOCK_SIZE);
            logger.info("override with --fs-block-size");
        }

        return retValue;
    }

    private class Directory {
        private Directory parent = null;
        private File directory = null;
        private Stack<Directory> subDirs = null;
        private long size = 0;

        public Directory(Directory parent, File directory) {
            this.parent = parent;
            this.directory = directory;
            subDirs = new Stack<Directory>();
        }

        public Directory addDirectory(File file) {
            Directory retValue = null;

            if (file.getParentFile().equals(directory)) {
                retValue = new Directory(this, file);
                subDirs.push(retValue);
            } else {
                logger.warn("addDirectory: tried to add " + file + " to " + directory);
            }

            return retValue;
        }

        public void addFile(File file) {
            if (!file.getParentFile().equals(directory)) {
                logger.warn("addFile: tried to add " + file + " to " + directory);
            }

            printFileSize(file, calc(file.length()));
            size += calc(file.length());
        }

        public Directory getParent() {

            long dirSize = size + calc(directory.length());
            // only the size for this directory + files 
            // (in other words: excludes the size of subDirs)
            long subDirSize = 0;

            while (!subDirs.isEmpty()) {
                subDirSize += subDirs.pop().getSize();
            }

            printDirSize(directory, dirSize, subDirSize);

            size = dirSize + subDirSize;

            return parent;
        }

        public long getSize() {
            return size;
        }
        
        @Override
        public int hashCode() {
            return directory.hashCode();
        }

        public boolean equals(Object other) {
            boolean retValue = false;

            if (other instanceof Directory) {
                retValue = (other != null && ((Directory) other).directory.equals(this.directory));
            } else if (other instanceof File) {
                retValue = (other != null && ((File) other).equals(this.directory));
            }

            return retValue;
        }

        @Override
        public String toString() {
            return directory.toString();
        }
    }

    private class Walker extends AbstractDirectoryWalker {

        long totalSize;
        protected Directory root = null;
        protected Directory currentDir = null;

        private Walker(IntegerArgument argMaxDepth, StringArgument argExclude) {
            super();

            if (argMaxDepth.isSet()) {
                super.setMaxDepth(argMaxDepth.getValue().longValue());
            }

            if (argExclude.isSet()) {
                super.addFilter(new RegexPatternFilter(argExclude.getValue(), true));
            }
        }

        public long getSize() {
            return totalSize;
        }

        @Override
        /**
         * Set the "root" Directory to the Starting Dir
         */
        protected void handleStartingDir(File file) throws IOException {
            log("starting dir: " + file);
            root = new Directory(null, file);
        }

        @Override
        /**
         * Calculate the "root" directory and reset the "current" directory .. we are done for now
         */
        protected void lastAction(boolean wasCancelled) {
            root.getParent();
            currentDir = null;
            totalSize = root.getSize();
        }

        @Override
        /**
         * add the currently handled directory/file to the correct position in the hierarchy
         */
        public void handleDir(File file) {
            log("handleDir: " + file);

            if (currentDir == null || currentDir.equals(file)) {
                currentDir = root;
                return;
            }

            while (!currentDir.equals(file.getParentFile())) {
                log("in while");
                currentDir = currentDir.getParent();
            }

            currentDir = currentDir.addDirectory(file);
        }

        @Override
        public void handleFile(File file) {
            log("handleFile: " + file);

            while (!currentDir.equals(file.getParentFile())) {
                currentDir = currentDir.getParent();
            }
            currentDir.addFile(file);
        }

        @Override
        protected void handleRestrictedFile(File file) throws IOException {
            err.format(ERR_PERMISSION, file);
        }
    }
}
