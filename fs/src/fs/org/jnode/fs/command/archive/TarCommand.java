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

import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.tar.TarOutputStream;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.StringArgument;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;

public class TarCommand extends ArchiveCommand {
    
    private static final String help_append  = "append entries to an archive";
    private static final String help_concat  = "concatenate multiple archives";
    private static final String help_create  = "create a new tar archive";
    private static final String help_delete  = "delete entries from an archive";
    private static final String help_diff    = "find differences between the archive and file system";
    private static final String help_extract = "extract the entries from an archive";
    private static final String help_list    = "list the contents of an archive";
    private static final String help_update  = "only append files that are newer than the copy in the archive";
    
    private static final String help_backup    = "backup files instead of overwriting";
    private static final String help_bzip      = "compress the archive with bzip2";
    private static final String help_dir       = "change to directory";
    private static final String help_exclude   = "exclude files matching <pattern>";
    private static final String help_file      = "use the given archive";
    private static final String help_file_list = "get names to extract or archive from <file>";
    private static final String help_gzip      = "compress the archive with gzip";
    private static final String help_interact  = "ask for confirmation for every action";
    private static final String help_keep_old  = "keep existing files; don't overwrite from archive";
    private static final String help_norecurse = "do not recurse into directories";
    private static final String help_paths     = "files and directories to include in archive";
    private static final String help_recurse   = "recurse into directories";
    private static final String help_remove    = "remove files after adding them to the archive";
    private static final String help_stdout    = "extract files to stdout";
    private static final String help_suffix    = "append <suffix> to backup files (default ~)";
    private static final String help_totals    = "display total bytes written after creating the archive";
    private static final String help_verbose   = "list files processed";
    private static final String help_verify    = "verify the archive after writing it";
    private static final String help_xfile     = "exclude files matching patterns in <file>";
    
    private static final int TAR_APPEND  = 0x01;
    private static final int TAR_CREATE  = 0x02;
    private static final int TAR_CONCAT  = 0x04;
    private static final int TAR_DELETE  = 0x08;
    private static final int TAR_UPDATE  = 0x10;
    private static final int TAR_LIST    = 0x20;
    private static final int TAR_DIFF    = 0x40;
    private static final int TAR_EXTRACT = 0x80;
    private static final int TAR_INSERT  = TAR_APPEND | TAR_CREATE;
    
    private final FlagArgument DoAppend  = new FlagArgument("doAppend", Argument.OPTIONAL, help_append);
    private final FlagArgument DoConcat  = new FlagArgument("doConcat", Argument.OPTIONAL, help_concat);
    private final FlagArgument DoCreate  = new FlagArgument("doCreate", Argument.OPTIONAL, help_create);
    private final FlagArgument DoDelete  = new FlagArgument("doDelete", Argument.OPTIONAL, help_delete);
    private final FlagArgument DoDiff    = new FlagArgument("doDiff", Argument.OPTIONAL, help_diff);
    private final FlagArgument DoExtract = new FlagArgument("doExtract", Argument.OPTIONAL, help_extract);
    private final FlagArgument DoList    = new FlagArgument("doList", Argument.OPTIONAL, help_list);
    private final FlagArgument DoUpdate  = new FlagArgument("doUpdate", Argument.OPTIONAL, help_update);
    
    private final FlagArgument Backup      = new FlagArgument("backup", Argument.OPTIONAL, help_backup);
    private final FlagArgument UseBzip     = new FlagArgument("bzip", Argument.OPTIONAL, help_bzip);
    private final FlagArgument Debug       = new FlagArgument("debug", Argument.OPTIONAL, " ");
    private final FileArgument ChangeDir   = new FileArgument("dir", Argument.OPTIONAL, help_dir);
    private final StringArgument Exclude   = new StringArgument("exclude", Argument.OPTIONAL, help_exclude);
    private final FileArgument Archive     = new FileArgument("file", Argument.OPTIONAL, help_file);
    private final FileArgument FileList    = new FileArgument("fileList", Argument.OPTIONAL, help_file_list);
    private final FlagArgument UseGzip     = new FlagArgument("gzip", Argument.OPTIONAL, help_gzip);
    private final FlagArgument Interact    = new FlagArgument("interact", Argument.OPTIONAL, help_interact);
    private final FlagArgument KeepFiles   = new FlagArgument("keepFiles", Argument.OPTIONAL, help_keep_old);
    private final FlagArgument NoRecurse   = new FlagArgument("noRecurse", Argument.OPTIONAL, help_norecurse);
    private final FlagArgument Recurse     = new FlagArgument("recurse", Argument.OPTIONAL, help_recurse);
    private final FlagArgument RemoveFiles = new FlagArgument("removeFiles", Argument.OPTIONAL, help_remove);
    private final FlagArgument ShowTotals  = new FlagArgument("showTotals", Argument.OPTIONAL, help_totals);
    private final StringArgument Suffix    = new StringArgument("suffix", Argument.OPTIONAL, help_suffix);
    private final FlagArgument UseStdout   = new FlagArgument("useStdout", Argument.OPTIONAL, help_stdout);
    private final FlagArgument Verbose     = new FlagArgument("verbose", Argument.OPTIONAL, help_verbose);
    private final FlagArgument Verify      = new FlagArgument("verify", Argument.OPTIONAL, help_verify);
    private final FileArgument ExcludeFile = new FileArgument("xfile", Argument.OPTIONAL, help_xfile);
    
    private final FileArgument Paths = new FileArgument("paths", Argument.OPTIONAL | Argument.MULTIPLE, help_paths);
    
    private File archive;
    private File excludeFile;
    private File fileList;
    private String suffix = "~";
    private String exclude = "";
    private int mode;
    private boolean recurse;
    private boolean pipeInOut;
    private boolean backup;
    private boolean bzip;
    private boolean gzip;
    private boolean interact;
    private boolean verify;
    private boolean use_stdout;
    private boolean showTotals;
    private boolean keepOldFiles;
    
    public TarCommand() {
        super("Create/Modify/Extract tape archives");
        registerArguments(DoAppend, DoConcat, DoCreate, DoDelete, DoDiff, DoExtract, DoList, DoUpdate,
                          Backup, UseBzip, Debug, ChangeDir, Exclude, Archive, FileList, UseGzip, Interact,
                          KeepFiles, NoRecurse, Recurse, RemoveFiles, ShowTotals, Suffix, UseStdout, Verbose,
                          Verify, Paths, ExcludeFile);
    }
    
    public void execute() {
        setup();
        if (!checkMode()) {
            error("required options -Acdtrux not found, or multiple options set");
            exit(1);
        }
        if (DoAppend.isSet())       mode = TAR_APPEND;
        else if (DoConcat.isSet())  mode = TAR_CONCAT;
        else if (DoCreate.isSet())  mode = TAR_CREATE;
        else if (DoDelete.isSet())  mode = TAR_DELETE;
        else if (DoDiff.isSet())    mode = TAR_DIFF;
        else if (DoExtract.isSet()) mode = TAR_EXTRACT;
        else if (DoList.isSet())    mode = TAR_LIST;
        else if (DoUpdate.isSet())  mode = TAR_UPDATE;
        
        if (Debug.isSet())   outMode |= OUT_DEBUG;
        if (Verbose.isSet()) outMode |= OUT_NOTICE;
        
        if (Suffix.isSet())      suffix      = Suffix.getValue();
        if (Exclude.isSet())     exclude     = Exclude.getValue();
        if (ExcludeFile.isSet()) excludeFile = ExcludeFile.getValue();
        if (FileList.isSet())    fileList    = FileList.getValue();
        
        backup       = Backup.isSet();
        bzip         = UseBzip.isSet();
        gzip         = UseGzip.isSet();
        interact     = Interact.isSet();
        verify       = Verify.isSet();
        use_stdout   = UseStdout.isSet();
        showTotals   = ShowTotals.isSet();
        keepOldFiles = KeepFiles.isSet();
        recurse      = !NoRecurse.isSet();
        if (Archive.isSet()) archive = Archive.getValue();
        else error("No archive given");
        //if (!(pipeInOut = !Archive.isSet())) archive = Archive.getValue();
        
        debug("Mode: " + mode);
        debug("Archive: " + archive);
        debug("Suffix: " + suffix);
        debug("Exclude: " + exclude);
        debug("Exclude File: " + excludeFile);
        debug("File List: " + fileList);
        debug("Backup: " + backup);
        debug("BZip: " + bzip);
        debug("GZip: " + gzip);
        debug("Interactive: " + interact);
        debug("Recurse: " + recurse);
        debug("Verify: " + verify);
        debug("Use StdOut: " + use_stdout);
        debug("Keep Old Files: " + keepOldFiles);
        debug("Show Totals: " + showTotals);
        debug("pipeInOut: " + pipeInOut);
        
        try {
            if ((mode & TAR_INSERT) != 0) {
                File[] files;
                files = processFiles(Paths.getValues(), recurse);
                insert(files);
                return;
            }
            if ((mode & TAR_EXTRACT) != 0) {
                extract();
            }
            if ((mode & TAR_LIST) != 0) {
                list();
            }
        } catch (Exception e) {
            e.printStackTrace();
            exit(2);
        }
    }
    
    private void insert(File[] files) throws IOException {
        debug("insert");
        InputStream in;
        OutputStream out;
        TarOutputStream tout;
        TarEntry entry;
        
        if (!use_stdout) {
            if (mode == TAR_CREATE || (mode == TAR_APPEND && !archive.exists())) createArchive();
            
            if ((out = openFileWrite(archive, false, false)) == null) {
                error("Could not open stream: " + archive);
                exit(1);
            }
        } else {
            debug("out=stdout");
            out = getOutput().getOutputStream();
        }
        
        tout = new TarOutputStream(out);
        debug("begin");
        for (File file : files) {
            notice(file.getName());
            entry = new TarEntry(file);
            tout.putNextEntry(entry);
            
            if (!file.isDirectory()) {
                if ((in = openFileRead(file)) == null) continue;
                processStream(in, tout);
                in.close();
            }
            tout.closeEntry();
        }
        tout.close();
        debug("end");
    }
    
    private void extract() throws IOException {
        TarEntry entry;
        InputStream in = null;
        OutputStream out;
        TarInputStream tin;
        File file;
        
        if (archive != null) {
            if (archive.exists()) {
                if ((in = openFileRead(archive)) == null) {
                    exit(1);
                }
            } else {
                error("File does not exist: " + archive);
                exit(1);
            }
        } else {
            in = getInput().getInputStream();
        }
        
        tin = new TarInputStream(in);
        
        if (use_stdout) {
            debug("out=stdout");
            out = getOutput().getOutputStream();
        }
        
        debug("begin");
        while ((entry = tin.getNextEntry()) != null) {
            notice(entry.getName());
            file = new File(entry.getName());
            if (entry.isDirectory()) {
                if (!file.exists()) {
                    file.mkdirs();
                }
                continue;
            }
            if ((out = openFileWrite(file, true, false)) == null) {
                continue;
            }
            tin.copyEntryContents(out);
            out.close();
        }
        tin.close();
        debug("end");
    }
    
    private void list() throws IOException {
        TarEntry entry;
        InputStream in = null;
        TarInputStream tin;
        
        if (archive == null || !archive.exists()) {
            error("Cannot find file: " + archive);
            exit(1);
        }
        if ((in = openFileRead(archive)) == null) {
            exit(1);
        }
        
        tin = new TarInputStream(in);
        
        while ((entry = tin.getNextEntry()) != null) {
            out(entry.getName());
        }
    }
    
    private void diff() throws IOException {
        TarEntry entry;
        InputStream in = null;
        TarInputStream tin;
        File file;
        
        if (archive == null || !archive.exists()) {
            error("Cannot find file: " + archive);
            exit(1);
        }
        if ((in = openFileRead(archive)) == null) {
            exit(1);
        }
        
        tin = new TarInputStream(in);
        
        while ((entry = tin.getNextEntry()) != null) {
            file = new File(entry.getName());
            
            if (!file.exists()) {
                out(file + ": Warning: No such file or directory");
            }
            
            if (file.lastModified() != entry.getModTime().getTime()) {
                out(file + ": Mod time differs");
            }
            
            if (file.length() != entry.getSize()) {
                out(file + ": Size differs");
            }
        }
    }
    
    private void createArchive() {
        try {
            if (archive.exists()) {
                archive.delete();
            }
            debug("creating archive: " + archive);
            archive.createNewFile();
        } catch (IOException e) {
            error("Could not create file: " + archive);
            exit(1);
        }
    }
    
    private File[] processFiles(File[] files , boolean recurse) {
        // FIXME object pollution
        ArrayList<File> _files = new ArrayList<File>();
        
        for (File file : files) {
            if (!file.exists()) {
                continue;
            }
            if (file.getName().equals(".") || file.getName().equals("..")) {
                continue;
            }
            
            if (file.isDirectory()) {
                if (recurse) {
                    _files.add(file);
                    Collections.addAll(_files, processFiles(file.listFiles(), recurse));
                }
                continue;
            }
            _files.add(file);
        }
        
        return _files.toArray(files);
    }
    
    private boolean checkMode() {
        int check = 0;
        if (DoAppend.isSet())  check++;
        if (DoCreate.isSet())  check++;
        if (DoConcat.isSet())  check++;
        if (DoDelete.isSet())  check++;
        if (DoDiff.isSet())    check++;
        if (DoExtract.isSet()) check++;
        if (DoList.isSet())    check++;
        if (DoUpdate.isSet())  check++;
        return check == 1;
    }
}
