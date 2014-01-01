/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.command.archive;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipExtraField;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.jnode.command.util.AbstractDirectoryWalker;
import org.jnode.command.util.AbstractDirectoryWalker.ModTimeFilter;
import org.jnode.command.util.AbstractDirectoryWalker.PathnamePatternFilter;
import org.jnode.shell.PathnamePattern;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * TODO test return codes
 * TODO make sure no archive is created if the creation fails
 * TODO implement delete
 * TODO implement update
 * TODO implement freshen
 * @author chris boeriten
 */
public class Zip extends ArchiveCommand {

    private static final boolean DEBUG = true;
    
    private static final String help_delete = "remove the list of entries from the archive";
    private static final String help_freshen = "[zip] Replaces entries in the archive with files from the file " +
                                               "system if they exist and are newer than the entry.\n[unzip] Replaces " +
                                               "files on the file system with entries from the archive if they exist " +
                                               "and are newer than the file.";
    private static final String help_update = "Like freshen, except it will also add files if they do not exist";
    private static final String help_test = "[zip] Tests the archive before finishing. If the archive is corrupt " +
                                            "then the original archive is restored, if any. This will also skip " +
                                            "deleting files in a move operation.\n[unzip] Tests the archive, " +
                                            "reporting wether the archive is corrupt or not.";
    private static final String help_move = "add the list of files to the archive, removing them from the file system";
    private static final String help_list = "list the contents of the archive";
    private static final String help_no_path = "store/extract the file with only its file name and no path prefix";
    private static final String help_archive = "the zip archive to use";
    private static final String help_patterns = "file matching patterns(wildcards)";
    
    private static final String fmt_extract  = "%11s: %s";
    private static final String fmt_footer   = " %8d %8d                   %d files";
    private static final String fmt_entry    = " %8d %8d                 %d %s";
    private static final String fmt_warn_dup = "%s: %s: %s";
    private static final String fatal_create_arch  = "Could not create archive: ";
    private static final String fatal_req_arch     = "Archive required but not found: ";
    private static final String fatal_create_zfile = "Unable to open archive as ZipFile: ";
    private static final String fatal_inv_args     = "zip error: Invalid arguments (cannot repeat names in zip file)";
    private static final String fatal_walking      = "Exception while walking.";
    private static final String fatal_read_stdin   = "Exception while reading stdin.";
    private static final String str_header_1    = "   Size    CSize     Date   Time   M Name";
    private static final String str_header_2    = " -------- -------- -------- -----  - ----";
    private static final String str_footer      = " -------- --------                   -------";
    private static final String str_archive     = "Archive: ";
    private static final String str_creating    = "creating";
    private static final String str_inflating   = "inflating";
    private static final String str_adding      = "adding";
    private static final String str_zip_warn    = "zip warning";
    private static final String str_fullname_1  = " first full name";
    private static final String str_fullname_2  = "second full name";
    private static final String str_name_repeat = "name in zip file repeated";
    
    protected final StringArgument Patterns;
    protected final FileArgument Archive;
    protected final FlagArgument Delete;
    protected final FlagArgument Freshen;
    protected final FlagArgument Update;
    protected final FlagArgument Test;
    protected final FlagArgument Move;
    protected final FlagArgument List;
    protected final FlagArgument NoPath;
    
    private static final int ZIP_ADD      = 0x01;
    private static final int ZIP_MOVE     = 0x02;
    private static final int ZIP_EXTRACT  = 0x04;
    private static final int ZIP_DELETE   = 0x08;
    private static final int ZIP_LIST     = 0x10;
    private static final int ZIP_TEST     = 0x20;
    private static final int ZIP_FRESHEN  = 0x40;
    private static final int ZIP_UPDATE   = 0x80;
    private static final int ZIP_ALL      = 0x3F;
    private static final int ZIP_INSERT   = ZIP_ADD | ZIP_MOVE;
    @SuppressWarnings("unused")
    private static final int ZIP_REQ_ARCH = ZIP_ALL & ~ZIP_INSERT;
    
    /* Populated in ZipCommand and UnzipCommand */
    protected List<String> includes;
    protected List<String> excludes;
    protected List<String> excludeDirs;
    
    private List<File> files;
    private List<ZipEntry> fileEntries;
    private List<ZipEntry> dirEntries;
    
    protected long newer;
    protected long older;
    private File archive;
    private ZipFile zarchive;
    protected File tmpDir;
    protected String[] noCompress;
    protected int mode;
    protected boolean ignore_case;
    protected boolean keep;
    protected boolean overwrite;
    protected boolean backup;
    protected boolean noDirEntry;
    protected boolean noPath;
    protected boolean recurse;
    protected boolean filesStdin;
    protected boolean useStdout;
    
    public Zip(String s) {
        super(s);
        Delete  = new FlagArgument("delete", Argument.OPTIONAL, help_delete);
        Freshen = new FlagArgument("freshen", Argument.OPTIONAL, help_freshen);
        Update  = new FlagArgument("update", Argument.OPTIONAL, help_update);
        Test    = new FlagArgument("test", Argument.OPTIONAL, help_test);
        Move    = new FlagArgument("move", Argument.OPTIONAL, help_move);
        List    = new FlagArgument("list", Argument.OPTIONAL, help_list);
        NoPath  = new FlagArgument("no-path", Argument.OPTIONAL, help_no_path);
        Patterns = new StringArgument("patterns", Argument.OPTIONAL | Argument.MULTIPLE, help_patterns);
        Archive = new FileArgument("archive", Argument.MANDATORY, help_archive);
    }
    
    public void execute(String command) {
        super.execute("zcat");
        parseOptions(command);
        
        try {
            if ((mode & ZIP_ADD) != 0) {
                insert();
                if ((mode & ZIP_MOVE) != 0) {
                    for (File file : files) {
                        file.delete();
                    }
                }
                return;
            }
            
            if (mode == ZIP_EXTRACT) {
                extract();
                return;
            }
            
            if (mode == ZIP_LIST) {
                list();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(zarchive);
            exit(0);
        }
    }
    
    private void insert() throws IOException {
        ZipOutputStream zout = null;
        ZipEntry entry;
        InputStream in;
        
        try {
            zout = new ZipOutputStream(archive);
            for (File file : files) {
                in = null;
                entry = createEntry(file);
                out(String.format(fmt_extract, str_adding, entry.getName()));
                try {
                    if (!file.isDirectory()) {
                        if ((in = openFileRead(file)) == null) {
                            continue;
                        }
                        zout.putNextEntry(entry);
                        processStream(in, zout);
                    } else {
                        zout.putNextEntry(entry);
                    }
                    zout.closeEntry();
                } catch (IOException e) {
                    debug(e.getMessage());
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                }
            }
        } finally {
            if (zout != null) {
                try {
                    zout.finish();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
    
    private void list() throws IOException {
        int size = 0;
        int csize = 0;
        int count = 0;
        
        printListHeader();
        for (ZipEntry entry : dirEntries) {
            printListEntry(entry);
            count++;
        }
        
        for (ZipEntry entry : fileEntries) {
            printListEntry(entry);
            count++;
            size  += entry.getSize();
            csize += entry.getCompressedSize();
        }
        printListFooter(size, csize, count);
    }
    
    private void extract() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        File file;
        
        out(str_archive + archive.getName());
        
        for (ZipEntry entry : dirEntries) {
            out(String.format(fmt_extract, str_creating, entry.getName()));
            file = new File(entry.getName());
            file.mkdirs();
        }
        
        for (ZipEntry entry : fileEntries) {
            out(String.format(fmt_extract, str_inflating, entry.getName()));
            file = new File(entry.getName());
            try {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
                in = zarchive.getInputStream(entry);
                if ((out = openFileWrite(file, false, false)) == null) {
                    continue;
                }
                processStream(in, out);
            } catch (IOException e) {
                debug(e.getMessage());
            } finally {
                close(in);
                close(out);
            }
        }
    }
    
    /**
     * Creates a ZipEntry for the specified file.
     *
     * If the file is a directory it will have a trailing slash
     * appended to its name. This is used to distinguish files
     * from directories in the archive.
     *
     * If the -j option is given, then only the file name is stored,
     * not its full pathname. Conflicts are dealt with in parseFiles()
     */
    private ZipEntry createEntry(File file) {
        String name = file.getPath();
        ZipEntry entry;
        if (file.isDirectory()) {
            if (!name.endsWith(File.separator)) {
                name = name + File.separator;
            }
            entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.STORED);
        } else {
            if (noPath) {
                name = file.getName();
            }
            entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.DEFLATED);
            if (noCompress != null && noCompress.length > 0) {
                for (String suf : noCompress) {
                    if (name.endsWith(suf)) {
                        entry.setMethod(ZipEntry.STORED);
                        break;
                    }
                }
            }
        }
        return entry;
    }
    
    private void parseOptions(String command) {
        if (DEBUG || Debug.isSet()) {
            outMode |= OUT_DEBUG;
        }
        if (Verbose.isSet()) {
            outMode |= OUT_NOTICE;
        }
        if (Quiet.isSet()) {
            outMode = 0;
        }
        if (command.equals("zip")) {
            if (Delete.isSet()) {
                mode = ZIP_DELETE;
            } else if (Freshen.isSet()) {
                mode = ZIP_FRESHEN | ZIP_ADD;
            } else if (Update.isSet()) {
                mode = ZIP_UPDATE | ZIP_ADD;
            } else if (Move.isSet()) {
                mode = ZIP_MOVE | ZIP_ADD;
            } else {
                mode = ZIP_ADD;
            }
        } else if (command.equals("unzip")) {
            if (Freshen.isSet()) {
                mode = ZIP_FRESHEN | ZIP_EXTRACT;
            } else if (Update.isSet()) {
                mode = ZIP_UPDATE | ZIP_EXTRACT;
            } else if (List.isSet()) {
                mode = ZIP_LIST;
            } else if (Test.isSet()) {
                mode = ZIP_TEST;
            } else {
                mode = ZIP_EXTRACT;
            }
        }
        
        noPath = NoPath.isSet();
        
        switch (mode & (ZIP_ADD | ZIP_EXTRACT | ZIP_LIST)) {
            case ZIP_ADD :
                parseFiles();
                getArchive(true, false);
                break;
            case ZIP_EXTRACT :
                getArchive(false, true);
                parseEntries();
                break;
            case ZIP_LIST :
                getArchive(false, true);
                parseEntries();
                break;
            default :
                throw new UnsupportedOperationException("This mode is not implemented.");
        }
    }
    
    /**
     * Instantiates the archive.
     *
     * If zipfile is true, than a ZipFile object for the archive is also created.
     *
     * This will exit with an error if:
     * - The archive does not exist, create is true, but an exception was thrown on creation.
     * - The archive does not exist, and create is false.
     * - The archive exists and create is true. (FIXME)
     * - A ZipFile was requested and there was an exception during instantiation.
     */
    private void getArchive(boolean create, boolean zipfile) {
        archive = Archive.getValue();
        
        if (archive.getName().equals("-")) {
            // pipe to stdout
        }
        
        if (!archive.exists()) {
            if (create) {
                try {
                    archive.createNewFile();
                } catch (IOException e) {
                    fatal(fatal_create_arch + archive, 1);
                }
            } else {
                fatal(fatal_req_arch + archive, 1);
            }
        } else {
            if (create) {
                fatal("Archive exists, refused to overwrite: " + archive, 1);
            }
        }
        
        if (zipfile) {
            try {
                zarchive = new ZipFile(archive);
            } catch (IOException e) {
                debug(e.getMessage());
                fatal(fatal_create_zfile + archive, 1);
            }
        }
    }
    
    private class Walker extends AbstractDirectoryWalker {
        @Override
        public void handleFile(final File file) throws IOException {
            addFile(file);
        }
        @Override
        public void handleDir(final File file) throws IOException {
            assert !(noDirEntry || noPath) : "handleDir called when noDirEntry || noPath";
            addFile(file);
        }
    }
    
    /**
     * Creates a list of files based on the files and filename patterns given
     * on the command line. If we're using recursion, then a directory walker
     * is used with the given include/exclude filters if any are in use.
     *
     * The -D/-j options prevent directories from being listed.
     * The -x/-i options are used to add exclude/include patterns.
     * The -r option turns on recursion
     *
     * This will exit with an error if there was an exception while walking.
     */
    private void parseFiles() {
        files = new ArrayList<File>();
        List<File> dirs = new ArrayList<File>();
        
        if (filesStdin) {
            parseFilesStdin();
        }
        if (Patterns.isSet()) {
            for (String pattern : Patterns.getValues()) {
                if (!PathnamePattern.isPattern(pattern)) {
                    File file = new File(pattern);
                    if (!file.exists()) {
                        debug("File or Directory does not exist: " + file);
                        continue;
                    }
                    if (file.isDirectory()) {
                        dirs.add(file);
                    } else {
                        addFile(file);
                    }
                } else {
                    PathnamePattern pat = PathnamePattern.compilePathPattern(pattern);
                    List<String> list = pat.expand(new File("."));
                    for (String name : list) {
                        File file = new File(name);
                        if (file.isDirectory()) {
                            dirs.add(file);
                        } else {
                            addFile(file);
                        }
                    }
                }
            }
        }
        
        if (recurse && dirs.size() > 0) {
            Walker walker = new Walker();
            if (noDirEntry || noPath) {
                walker.addFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return !file.isDirectory();
                    }
                });
            }
            if (excludes != null && excludes.size() > 0) {
                for (String pattern : excludes) {
                    walker.addFilter(new PathnamePatternFilter(pattern, true));
                }
            }
            if (includes != null && includes.size() > 0) {
                for (String pattern : includes) {
                    walker.addFilter(new PathnamePatternFilter(pattern, false));
                }
            }
            if (newer > 0) {
                walker.addFilter(new ModTimeFilter(newer, true));
            }
            if (older > 0) {
                walker.addFilter(new ModTimeFilter(older, false));
            }
            try {
                walker.walk(dirs);
            } catch (IOException e) {
                debug(e.getMessage());
                fatal(fatal_walking, 1);
            }
        }
    }
    
    /**
     * Parses files from stdin, one file per line.
     *
     * If the file does not exist, it is ignored and omitted.
     *
     * This will exit with an error if there is an exception while reading stdin.
     */
    private void parseFilesStdin() {
        LineNumberReader reader = new LineNumberReader(stdinReader);
        String line;
        File file;
        
        try {
            while ((line = reader.readLine()) != null) {
                file = new File(line);
                if (file.exists()) {
                    addFile(file);
                }
            }
        } catch (IOException e) {
            fatal(fatal_read_stdin, 1);
        }
    }
    
    /**
     * Adds a file to the list of files.
     *
     * If the -j option is used, then the list is scanned for the file name.
     * This is done because -j strips the path from the pathname, which can cause
     * collisions if two files from separate directories with the same name are added.
     */
    private void addFile(File file) {
        if (noPath) {
            // this isn't effecient by any means, but this is not an often-used
            // case, and when it is, its not likely that files.size() is going
            // to grow very large.
            for (File f : files) {
                if (f.getName().equals(file.getName())) {
                    printDuplicateError(file, f);
                    fatal(fatal_inv_args, 1);
                }
            }
        }
        files.add(file);
    }
    
    @SuppressWarnings("unchecked")
    private void parseEntries() {
        int count = 0;
        
        ZipEntry entry;
        Enumeration<ZipEntry> entries = zarchive.getEntries();
        fileEntries = new ArrayList<ZipEntry>();
        dirEntries = new ArrayList<ZipEntry>();
        
        while (entries.hasMoreElements()) {
            count++;
            entry = entries.nextElement();
            if (entry.isDirectory()) {
                dirEntries.add(entry);
            } else {
                fileEntries.add(entry);
            }
        }
    }
    
    private void printListHeader() {
        out(str_header_1);
        out(str_header_2);
    }
    
    private void printListEntry(ZipEntry entry) {
        out(String.format(fmt_entry, entry.getSize(), entry.getCompressedSize(), entry.getMethod(), entry.getName()));
    }
    
    private void printListFooter(int size, int csize, int numFiles) {
        out(str_footer);
        out(String.format(fmt_footer, size, csize, numFiles));
    }
    
    private void printDuplicateError(File A, File B) {
        error(String.format(fmt_warn_dup, str_zip_warn, str_fullname_1, A.getPath()));
        error(String.format(fmt_warn_dup, str_zip_warn, str_fullname_2, B.getPath()));
        error(String.format(fmt_warn_dup, str_zip_warn, str_name_repeat, A.getName()));
    }
    
    @SuppressWarnings("unused")
    private void printName(String s) {
        if (outMode != 0) {
            out(s);
        }
    }
    
    @SuppressWarnings("unused")
    private void debug(ZipEntry entry) {
        debug("Name: " + entry.getName());
        debug("Directory: " + entry.isDirectory());
        debug("Platform: " + entry.getPlatform());
        debug("Mode: " + entry.getUnixMode());
        debug("IAttr: " + entry.getInternalAttributes());
        debug("EAttr: " + entry.getExternalAttributes());
        debug("CSize: " + entry.getCompressedSize());
        debug("Size: " + entry.getSize());
        debug("MTime: " + entry.getTime());
        debug("Method: " + entry.getMethod());
        debug("CRC: " + entry.getCrc());
        debug("Comment: " + entry.getComment());
        ZipExtraField[] extra = entry.getExtraFields();
        if (extra != null && extra.length > 0) {
            debug("--Extra--");
            for (ZipExtraField field : extra) {
                debug("CDL: " + field.getCentralDirectoryLength().getValue() + " FDL: " + 
                                field.getLocalFileDataLength().getValue());
            }
        }
    }
}
