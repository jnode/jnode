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

package org.jnode.fs.command.archive;

import org.jnode.shell.PathnamePattern;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.StringArgument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.PrintWriter;
import java.io.IOException;

import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.tools.zip.AsiExtraField;
import org.apache.tools.zip.ZipExtraField;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

/**
 * @author chris boeriten
 */
public class Zip extends ArchiveCommand {

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
    private static final int ZIP_REQ_ARCH = ZIP_ALL & ~ZIP_INSERT;
    
    private List<Pattern> patterns;
    private List<Pattern> includes;
    private List<Pattern> excludes;
    private List<File> files;
    private List<ZipEntry> fileEntries;
    private List<ZipEntry> dirEntries;
    
    protected long newer;
    protected long older;
    private File archive;
    private ZipFile zarchive;
    protected File tmpDir;
    protected String noCompress;
    protected int mode;
    protected boolean ignore_case;
    protected boolean keep;
    protected boolean overwrite;
    protected boolean backup;
    protected boolean noDirEntry;
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
            if (mode == ZIP_ADD) {
                insert();
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
            if (zarchive != null) {
                try {
                    zarchive.close();
                } catch (IOException _) {
                    // ignore
                }
            }
            exit(0);
        }
    }
    
    private void insert() throws IOException {
        ZipOutputStream zout = null;
        ZipEntry entry;
        InputStream in;
        
        try {
            zout = new ZipOutputStream(new FileOutputStream(archive));
            for (File file : files) {
                in = null;
                try {
                    entry = createEntry(file);
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
                    //
                }
            }
        }
    }
    
    private void list() throws IOException {
        Enumeration<ZipEntry> entries;
        ZipEntry entry;
        int size = 0;
        int csize = 0;
        int count = 0;
        
        printListHeader();
        entries = zarchive.getEntries();
        while (entries.hasMoreElements()) {
            entry = entries.nextElement();
            //debug(entry);
            printListEntry(entry);
            count++;
            size += entry.getSize();
            csize += entry.getCompressedSize();
        }
        printListFooter(size, csize, count);
    }
    
    private void extract() throws IOException {
        InputStream in = null;
        OutputStream out = null;
        File file;
        
        out("Archive: " + archive.getName());
        for (ZipEntry entry : dirEntries) {
            out(String.format("%11s: %s", "creating", entry.getName()));
            file = new File(entry.getName());
            file.mkdirs();
        }
        
        for (ZipEntry entry : fileEntries) {
            out(String.format("%11s: %s", "inflating", entry.getName()));
            file = new File(entry.getName());
            try {
                file.createNewFile();
                in = zarchive.getInputStream(entry);
                out = new FileOutputStream(file);
                processStream(in, out);
            } catch (IOException e) {
                debug(e.getMessage());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                        in = null;
                    } catch (IOException _) {
                        // ignore;
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                        out = null;
                    } catch (IOException _) {
                        // ignore;
                    }
                }
            }
        }
    }
    
    private ZipEntry createEntry(File file) {
        String name = file.getPath();
        ZipEntry entry;
        if (file.isDirectory()) {
            if (!name.endsWith(File.separator)) {
                name = name + File.separator;
            }
            entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.STORED);
            entry.setCrc(0);
            entry.setSize(0);
        } else {
            entry = new ZipEntry(name);
            entry.setMethod(ZipEntry.DEFLATED);
        }
        return entry;
    }
    
    private void printListHeader() {
        out("   Size    CSize     Date   Time   Name");
        out(" -------- -------- -------- -----  ----");
    }
    
    private void printListEntry(ZipEntry entry) {
        out(String.format(" %8d %8d                 %s", entry.getSize(), entry.getCompressedSize(), entry.getName()));
    }
    
    private void printListFooter(int size, int csize, int numFiles) {
        out(" -------- --------                 -------");
        out(String.format(" %8d %8d                 %d files", size, csize, numFiles));
    }
    
    private void printName(String s) {
        if (outMode != 0) {
            out(s);
        }
    }
    
    private void parseOptions(String command) {
        outMode |= OUT_DEBUG;
        if (command.equals("zip")) {
            if (Delete.isSet()) {
                mode = ZIP_DELETE;
            } else if (Freshen.isSet()) {
                mode = ZIP_FRESHEN | ZIP_ADD;
            } else if (Update.isSet()) {
                mode = ZIP_UPDATE | ZIP_ADD;
            } else if (Move.isSet()) {
                mode = ZIP_MOVE;
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
        assert mode != 0 : "Invalid mode";
        
        switch (mode) {
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
                break;
            default :
                throw new UnsupportedOperationException("This mode is not implemented.");
        }
    }
    
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
                    fatal("Could not create archive: " + archive, 1);
                }
            } else {
                fatal("Archive required but not found: " + archive, 1);
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
                fatal("Unable to open archive as ZipFile: " + archive, 1);
            }
            assert zarchive != null : "null zarchive";
        }
        
        assert archive != null : "null archive after getArchive()";
        assert archive.exists() : "archive does not exist, or was not created";
    }
    
    private void parseFiles() {
        files = new ArrayList<File>();
        
        if (Patterns.isSet()) {
            for (String pattern : Patterns.getValues()) {
                if (!PathnamePattern.isPattern(pattern)) {
                    File file = new File(pattern);
                    if (!file.exists()) {
                        debug("File or Directory does not exist: " + file);
                        continue;
                    }
                    if (file.isDirectory()) {
                        addDirectory(file);
                    } else {
                        addFile(file);
                    }
                } else {
                    PathnamePattern pat = PathnamePattern.compilePathPattern(pattern);
                    List<String> list = pat.expand(new File("."));
                    for (String name : list) {
                        File file = new File(name);
                        if (file.isDirectory()) {
                            addDirectory(file);
                        } else {
                            addFile(file);
                        }
                    }
                }
            }
        }
    }
    
    private void parseEntries() {
        assert zarchive != null : "null archive in parseEntries";
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
        
        assert count == (fileEntries.size() + dirEntries.size());
    }
        
    private void addDirectory(File dir) {
        if (!recurse) {
            return;
        }
        files.add(dir);
        
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                addDirectory(file);
            } else {

                addFile(file);
            }
        }
    }
    
    private void addFile(File file) {
        files.add(file);
    }
    
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
