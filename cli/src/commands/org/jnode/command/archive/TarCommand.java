/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.tar.TarOutputStream;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 *
 * This version of tar has the ability to magically work with compressed archives without being
 * told with the -jz flags on the command line. The -jz flags are seen as an override to force tar
 * to work with the selected compression method, and fail if it can't do that.
 *
 * Reading :
 * 1) If the -j or -z flags are set, then tar will check for the magic bytes in the header for the
 *    given compression method. If they are not found, tar will fail.
 * 2) If the -j or -z flags are not set, then tar will check for the magic bytes in the header for
 *    both compression methods. If either are found, tar will wrap the FileInputStream given to TarInputStream
 *    with a decompression wrapper stream. If neither are found, then tar assumes the archive is an
 *    uncompressed archive.
 *    If the archive is being extracted from stdin, and the archive is compressed, then the -j or -z flags
 *    must be given, or tar will assume it is already uncompressed.
 *
 * Creating :
 * 1) If the -j or -z flags are set, then tar will output a new archive compressed with the given method.
 * 2) If the -j or -z flags are not set, then tar will check the file suffix of the new archive. If the suffix
 *    is either .tbz, .tbz2, .tar.bz or .tar.bz2, then tar will compress with bzip2. If the suffix is .tar.gz
 *    then tar will compress with gzip. Otherwise an uncompressed archive is created.
 *
 * Writing :
 * 1) If the -j or -z flags are set, then tar will output an archive compressed with the given method.
 * 2) If the -j or -z flags are not set, and the original archive was compressed, then the same method will
 *    be used to recompress the archive. Otherwise the archive will be output uncompressed.
 *
 * TODO Currently, in order to set the output stream to the last entry of an archive, in order to append new entries,
 *      we're moving the archive, creating a new archive, and copying the data via streams. A more effecient method
 *      would be a reverse search of the file looking for the last block to be written too. Blocks are written in
 *      groups of 20, so it will be relatively fast to find. Once we read the header, we'll know how big the entry is
 *      and we can position the OutputStream to the first available block. We can't simply append to the end of the
 *      file because the last block is not necessarily the end of the file.
 *      This might not be quite so simple though, especially when dealing with compressed archives. Which can't be
 *      randomly accessed.
 *
 * TODO Implement a way to check if a file being handled is actually a tar file. The tar input stream will gladly
 *      read a file that is not a tar archive. This can cause any sort of irrational behavior, especially if the
 *      data is binary in nature.
 * TODO Implement update/delete.
 * TODO Implement interactive (global)
 * TODO Implement display totals. (global)
 * TODO Implement verification (verify)
 * FIXME BUG - if extracting an archive given on stdin, and that archive overwrites files on the filesystem
 *             it will cause openFileWrite() to prompt the user and ask to overwrite, but reading from stdin
 *             wont come from the console since stdin is attached to a pipe. In this case we will either have
 *             to default to overwrite or skip.
 * TODO When extracting entries, tar needs to strip any leading slashes by default, turning absolute pathnames
 *      into relative ones. When inserting entries, leading slashes should also be stripped by default. Also
 *      warn about inserting entries with a '..' prefix, and refuse to extract them.
 * TODO Default behavior when extracting is to overwrite.
 * TODO Implement exclusion by date, --newer=<date> ignores files with a mod time older than the given date.
 *
 * TODO Options to implement
 * -P --absolute-names [Create, Append, Update] TODO
 *      Overrides the default behavior of stripping leading slashes while inserting or extracting entries. Will
 *      also force tar to extract entries prefixed with '..'
 * -N --newer [All] TODO
 *      Limit operating on files to only those that are newer than the given date. If the value starts with a
 *      / or ., the value is a file, and its mod time should be used.
 * --newer-mtime [All] TODO
 *      Similar to --newer, except it doesn't take into account status changes made to the file, only content
 *      changes (might not be a difference atm in jnode, so they'll both be the same).
 * --atime-preserve [Create, Append, Update, Extract]
 *      Preserve the access time of the file. (Not important)
 * -a --auto-compress [Create]
 *      When creating an archive, determine the compression type from the archive suffix. We already do this by
 *      default if no compress flags (j/z) were given.
 * --no-auto-compress [Create] TODO
 *      Turn off the automatic checking of archive suffix to determine compression type.
 * --backup [Extract] TODO
 *      Backup files instead of overwriting them. We currently implement simple backups using a given or default
 *      suffix. There is also the option of creating numbered backups. And a hybrid option that makes numbered
 *      backups if they exist already, simple backups if they do not. As well this option can also apply to the
 *      archive itself if it is being modified. This option gets its default from the VERSION_CONTROL env variable.
 *      If this is not set, the default is 'existing'.
 *      Options are 't' | 'numbered', 'nil' | 'existing' and 'never' | 'simple'.
 * --suffix [Extract/backup]
 *      Need to read the SIMPLE_BACKUP_SUFFIX env variable for the default, otherwise use ~
 * --checkpoint n
 *      Checkpoints are issued every nth recorded that is written or read to/from the archive.
 * --checkpoing-action <action>
 *      Perform one of the following actions every checkpoint
 *      - bell Produces an audible bell on the system speaker
 *      - dot prints a '.' to stdout
 *      - echo Display a message to stderr
 *      - echo=string Display string to stderr, string is subject to meta-character expansion
 *      - exec=command Execute the given command
 *      - sleep=time Sleep for <time> seconds
 *      - ttyout=string Output string to /dev/tty (the current console, if stderr is redirected)
 *      This argument may be given multiple times to perform different actions. They will be executed in the
 *      order found on the command line.
 * -l --check-links
 * --delay-directory-restore
 * --no-delay-directory-restore
 *      Restore directory timestamps and permissions after extraction has completed.
 * -h --dereference
 *      Store the file a link points to instead of the link itself
 * --anchored
 * --no-anchored
 * --ignore-case
 * --no-ignore-case
 * --wildcards
 * --no-wildcards
 * --wildcards-match-slash
 * --no-wildcards-match-slash
 *      Pattern matching modifier
 * --ignore-failed-read
 *      Do not exit just because a read of a file failed. (We do this by default atm)
 * --index-file=<file>
 *      Send verbose output to <file>
 * --overwrite
 * --overwrite-dir
 *      Overwrite existing files when extracting (default)
 *
 * --lzma
 * --lzop
 * -Z --compress
 *      Other compression methods, not supported atm.
 * 
 * --pax-option=<list>
 * --owner=<name>
 * -o --no-same-owner
 * --no-same-permissions
 * --no-unquote
 * --numberic-owner
 * --occurence=<number>
 * --one-file-system
 * --null
 * --no-null
 * --no-overwrite-dir
 * --no-ignore-command-error
 * -M --multi-volume
 * --no-check-device
 * -g --listed-incremental=<file>
 * -V --label=<string>
 * -F --info-script=<file>
 * -G --incremental
 * --group
 * --owner
 * --mode
 * --mtime
 * -H --format
 * --interactive
 * -R --block-number
 * -b --blocking-factor
 * -i --ignore-zeroes
 * -B --read-full-records
 * --check-device
 *      (ignore for now)
 *
 * @author chris boertien
 */
public class TarCommand extends ArchiveCommand {
    
    private static final String help_append  = "append entries to an archive";
    private static final String help_concat  = "concatenate multiple archives";
    private static final String help_create  = "create a new tar archive";
    private static final String help_delete  = "delete entries from an archive";
    private static final String help_diff    = "find differences between the archive and file system";
    private static final String help_extract = "extract the entries from an archive";
    private static final String help_list    = "list the contents of an archive";
    private static final String help_update  = "only append files that are newer than the copy in the archive";
    
    private static final String help_archive   = "use the given archive";
    private static final String help_backup    = "backup files instead of overwriting";
    private static final String help_bzip      = "compress the archive with bzip2";
    private static final String help_dir       = "change to directory";
    private static final String help_exclude   = "exclude files matching <pattern>";
    private static final String help_file_list = "get names to extract or archive from <file>";
    private static final String help_gzip      = "compress the archive with gzip";
    private static final String help_interact  = "ask for confirmation for every action";
    private static final String help_keep_old  = "keep existing files; don't overwrite from archive";
    private static final String help_keep_new  = "keep existing files if they are newer than the archive entry";
    private static final String help_norecurse = "do not recurse into directories";
    private static final String help_paths     = "files and directories to include in archive";
    private static final String help_recurse   = "recurse into directories";
    private static final String help_remove    = "remove files after adding them to the archive";
    @SuppressWarnings("unused")
    private static final String help_stdout    = "extract files to stdout";
    private static final String help_suffix    = "append <suffix> to backup files (default ~)";
    private static final String help_totals    = "display total bytes written after creating the archive";
    private static final String help_unlink    = "when extracting, delete files if they exist. This is the default" +
                                                 "action and is used to override other options if they were set";
    @SuppressWarnings("unused")
    private static final String help_verbose   = "list files processed";
    private static final String help_verify    = "verify the archive after writing it";
    private static final String help_xfile     = "exclude files matching patterns in <file>";
    
    private static final String err_options    = "required options -Acdtrux not found, or multiple options set";
    
    private static final int TAR_APPEND     = 0x01;
    private static final int TAR_CREATE     = 0x02;
    private static final int TAR_CONCAT     = 0x04;
    private static final int TAR_DELETE     = 0x08;
    private static final int TAR_UPDATE     = 0x10;
    private static final int TAR_LIST       = 0x20;
    private static final int TAR_DIFF       = 0x40;
    private static final int TAR_EXTRACT    = 0x80;
    private static final int TAR_REQ_ARCH   
        = TAR_APPEND | TAR_CREATE | TAR_CONCAT | TAR_DELETE | TAR_UPDATE | TAR_LIST | TAR_DIFF;
    private static final int TAR_INSERT     = TAR_APPEND | TAR_CREATE;
    private static final int TAR_VERIFY     = TAR_APPEND | TAR_CONCAT | TAR_DELETE | TAR_UPDATE;
    private static final int TAR_COMPRESS   = TAR_APPEND | TAR_CREATE | TAR_CONCAT | TAR_DELETE | TAR_UPDATE;
    private static final int TAR_DECOMPRESS = 
        TAR_APPEND | TAR_CONCAT | TAR_DELETE | TAR_DIFF | TAR_UPDATE | TAR_LIST | TAR_EXTRACT;
    
    private static final int USE_BZIP = 1;
    private static final int USE_GZIP = 2;
    
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
    private final FileArgument ChangeDir   = new FileArgument("dir", Argument.OPTIONAL, help_dir);
    private final StringArgument Exclude   = new StringArgument("exclude", Argument.OPTIONAL, help_exclude);
    private final FileArgument Archive     = new FileArgument("archive", Argument.OPTIONAL, help_archive);
    private final FileArgument FileList    = new FileArgument("fileList", Argument.OPTIONAL, help_file_list);
    private final FlagArgument UseGzip     = new FlagArgument("gzip", Argument.OPTIONAL, help_gzip);
    private final FlagArgument Interact    = new FlagArgument("interact", Argument.OPTIONAL, help_interact);
    private final FlagArgument KeepOld     = new FlagArgument("keep_old", Argument.OPTIONAL, help_keep_old);
    private final FlagArgument KeepNew     = new FlagArgument("keep_new", Argument.OPTIONAL, help_keep_new);
    private final FlagArgument NoRecurse   = new FlagArgument("noRecurse", Argument.OPTIONAL, help_norecurse);
    private final FlagArgument Recurse     = new FlagArgument("recurse", Argument.OPTIONAL, help_recurse);
    private final FlagArgument RemoveFiles = new FlagArgument("removeFiles", Argument.OPTIONAL, help_remove);
    private final FlagArgument ShowTotals  = new FlagArgument("showTotals", Argument.OPTIONAL, help_totals);
    private final StringArgument Suffix    = new StringArgument("suffix", Argument.OPTIONAL, help_suffix);
    private final FlagArgument Unlink      = new FlagArgument("unlink", Argument.OPTIONAL, help_unlink);
    private final FlagArgument Verify      = new FlagArgument("verify", Argument.OPTIONAL, help_verify);
    private final FileArgument ExcludeFile = new FileArgument("xfile", Argument.OPTIONAL, help_xfile);
    
    private final FileArgument Paths = new FileArgument("paths", Argument.OPTIONAL | Argument.MULTIPLE, help_paths);
    
    private File archive;
    @SuppressWarnings("unused")
    private File excludeFile;
    @SuppressWarnings("unused")
    private File fileList;
    private String suffix = "~";
    @SuppressWarnings("unused")
    private String exclude = "";
    private int mode;
    private int compress;
    private int decompress;
    private boolean recurse;
    @SuppressWarnings("unused")
    private boolean pipeInOut;
    private boolean backup;
    private boolean bzip;
    private boolean gzip;
    @SuppressWarnings("unused")
    private boolean interact;
    private boolean verify;
    @SuppressWarnings("unused")
    private boolean showTotals;
    private boolean keepOld;
    private boolean keepNew;
    @SuppressWarnings("unused")
    private boolean unlink;
    
    public TarCommand() {
        super("Create/Modify/Extract tape archives");
        // from ArchiveCommand
        registerArguments(Verbose, Debug, Stdout);
        
        // tar Operations
        registerArguments(DoAppend, DoConcat, DoCreate, DoDelete, DoDiff, DoExtract, DoList, DoUpdate);
        
        // tar Global Options
        registerArguments(Backup, Suffix, UseBzip, UseGzip, Archive, FileList, ExcludeFile, Interact, KeepNew, KeepOld,
                           Unlink, RemoveFiles, ShowTotals, Verify, Paths);
        // tar Parsing Options
        registerArguments(ChangeDir, Exclude, NoRecurse, Recurse);
    }
    
    // TODO Allow working directory to be changed
    public void execute() {
        super.execute("tar");
        if (!checkMode()) {
            fatal(err_options, 1);
        }
        
        if (Archive.isSet())     archive     = Archive.getValue();
        if (Suffix.isSet())      suffix      = Suffix.getValue();
        if (Exclude.isSet())     exclude     = Exclude.getValue();
        if (ExcludeFile.isSet()) excludeFile = ExcludeFile.getValue();
        if (FileList.isSet())    fileList    = FileList.getValue();
        
        backup     = Backup.isSet();
        bzip       = UseBzip.isSet();
        gzip       = UseGzip.isSet();
        interact   = Interact.isSet();
        verify     = Verify.isSet();
        showTotals = ShowTotals.isSet();
        keepOld    = KeepOld.isSet();
        keepNew    = KeepNew.isSet();
        recurse    = !NoRecurse.isSet();
        unlink     = Unlink.isSet();
        
        try {
            if ((mode & TAR_REQ_ARCH) != 0 && archive == null) {
                fatal("Archive required for -Acdtru", 1);
            }
            
            if ((mode & TAR_DECOMPRESS) != 0 && archive != null) {
                if (checkCompressed(archive) == -1) {
                    // happens when -j or -z were specified, but the archive is not
                    // in the given format.
                    if (bzip) {
                        fatal("Archive is not compressed with bzip2.", 1);
                    }
                    if (gzip) {
                        fatal("Archive is not compressed with gzip.", 1);
                    }
                    fatal("Internal Error: checkCompressed() returned -1", -1);
                }
            }
            
            if ((mode & TAR_COMPRESS) != 0) {
                if (bzip) {
                    compress = USE_BZIP;
                } else if (gzip) {
                    compress = USE_GZIP;
                } else {
                    compress = decompress;
                }
            }
            
            if ((mode & TAR_VERIFY) != 0 && verify) {
                // backup original archive
            }
            
            if ((mode & TAR_CREATE) != 0 && compress == 0) {
                compress = checkSuffix(archive);
            }
                
            if ((mode & TAR_INSERT) != 0) {
                insert(processFiles(Paths.getValues(), recurse));
            }
            
            if ((mode & TAR_UPDATE) != 0) {
                update(processFiles(Paths.getValues(), recurse));
            }
            
            if ((mode & TAR_CONCAT) != 0) {
                concat(processArchives(Paths.getValues()));
            }
            
            if ((mode & TAR_DELETE) != 0) {
                //delete();
            }
            
            if ((mode & TAR_EXTRACT) != 0) {
                if (decompress == 0 && archive == null) {
                    if (bzip) {
                        decompress = USE_BZIP;
                    } else if (gzip) {
                        decompress = USE_GZIP;
                    }
                }
                extract();
            }
            
            if ((mode & TAR_LIST) != 0) {
                list();
            }
            
            if ((mode & TAR_DIFF) != 0) {
                diff();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fatal(err_exception_uncaught, 1);
        }
    }
    
    /**
     * Concatenates a list of archives with this archive.
     *
     * TODO If the verify option is set, the original archive is backed up before operating
     *      on it, and verified before exiting. If the archive is bad, the original is restored.
     */
    private void concat(File[] archives) throws IOException {
        InputStream in;
        TarInputStream tin;
        TarOutputStream tout;
        
        // Setup archive for appending
        tout = appendTarOutputStream();
        
        // Concatenate new archives
        for (File arch : archives) {
            if ((in = openFileRead(arch)) == null) {
                continue;
            }
            bzip = gzip = false;
            decompress = checkCompressed(arch);
            if (decompress != 0) {
                in = wrapInputStream(in);
            }
            tin = new TarInputStream(in);
            copy(tin, tout);
        }
        tout.close();
    }
    
    /**
     * Insert a list of files into an archive.
     *
     * This is used by Create and Append to insert new entries into the archive.
     *
     * TODO Allow files to be delete from the filesystem after the archive has been written.
     *      If the verify option is set, then the archive must pass verification before the
     *      files are deleted.
     *
     * TODO If the verify option is set, the original archive is backed up before operating
     *      on it, and verified before exiting. If the archive is bad, the original is restored.
     */
    private void insert(File[] files) throws IOException {
        InputStream in;
        OutputStream out;
        TarOutputStream tout = null;
        TarEntry entry;
        
        if (mode == TAR_APPEND && archive.exists()) {
            tout = appendTarOutputStream();
        } else {
            createArchive();
            if ((out = openFileWrite(archive, false, false)) == null) {
                fatal(" ", 1);
            }
            if (compress != 0) {
                out = wrapOutputStream(out);
            }
            tout = new TarOutputStream(out);
        }
        
        // Insert new entries
        for (File file : files) {
            notice(file.getPath());
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
    }
    
    // TODO
    private void update(File[] files) throws IOException {
        InputStream in;
        TarInputStream tin;
        TarEntry entry;
        TreeMap<String, Long> entries = new TreeMap<String, Long>();
        
        if ((in = openFileRead(archive)) == null) {
            fatal(" ", 1);
        }
        if (decompress != 0) {
            in = wrapInputStream(in);
        }
        
        tin = new TarInputStream(in);
        
        while ((entry = tin.getNextEntry()) != null) {
            entries.put(entry.getName(), entry.getModTime().getTime());
        }
        tin.close();
        
        long etime, ftime;
        ArrayList<File> list = new ArrayList<File>();
        for (File file : files) {
            if (entries.containsKey(file.getPath())) {
                etime = entries.get(file.getPath());
                ftime = file.lastModified();
                if (etime >= ftime) {
                    continue;
                }
            }
            list.add(file);
        }
        
        insert(list.toArray(files));
    }
    
    // TODO
    @SuppressWarnings("unused")
    private void delete(String[] names) throws IOException {
    }
    
    /**
     * Extract entries from an archive.
     *
     * TODO Need to parse Path for choosing specific files/directories either by direct naming
     *      or by wildcard patterns.
     * TODO Read list of entries to extract from FileList if its set.
     */
    private void extract() throws IOException {
        TarEntry entry;
        InputStream in = null;
        OutputStream out;
        TarInputStream tin;
        File file;
        
        if (archive != null) {
            if ((in = openFileRead(archive)) == null) {
                fatal(" ", 1);
            }
        } else {
            in = stdin;
        }
        
        if (decompress != 0) {
            in = wrapInputStream(in);
        }
        tin = new TarInputStream(in);
        
        if (use_stdout) {
            out = stdout;
        }
        
        while ((entry = tin.getNextEntry()) != null) {
            notice(entry.getName());
            file = new File(entry.getName());
            if (entry.isDirectory()) {
                if (!file.exists()) {
                    file.mkdirs();
                }
                continue;
            } else {
                if (file.exists()) {
                    if (keepOld || (keepNew && (file.lastModified() >= entry.getModTime().getTime()))) {
                        continue;
                    }
                    if (backup) {
                        file.renameTo(new File(file.getPath() + suffix));
                    }
                }
            }
            if ((out = openFileWrite(file, true, true)) == null) {
                continue;
            }
            tin.copyEntryContents(out);
            out.close();
        }
        tin.close();
    }
    
    /**
     * List the contents of an archive.
     *
     * TODO Need to parse Path for choosing specific files/directories either by direct naming
     *      or by wildcard patterns.
     */
    private void list() throws IOException {
        TarEntry entry;
        InputStream in = null;
        TarInputStream tin;
        
        if ((in = openFileRead(archive)) == null) {
            fatal(" ", 1);
        }
        
        if (decompress != 0) {
            in = wrapInputStream(in);
        }
        tin = new TarInputStream(in);
        
        while ((entry = tin.getNextEntry()) != null) {
            out(entry.getName());
        }
    }
    
    /**
     * Outputs the differences found between the archive and the file system.
     */
    private void diff() throws IOException {
        TarEntry entry;
        InputStream in = null;
        TarInputStream tin;
        File file;
        
        if ((in = openFileRead(archive)) == null) {
            exit(1);
        }
        
        if (decompress != 0) {
            in = wrapInputStream(in);
        }
        tin = new TarInputStream(in);
        
        while ((entry = tin.getNextEntry()) != null) {
            file = new File(entry.getName());
            
            if (!file.exists()) {
                out(file + ": Warning: No such file or directory");
                continue;
            }
            
            if (file.lastModified() != entry.getModTime().getTime()) {
                out(file + ": Mod time is different");
            }
            
            if (file.length() != entry.getSize()) {
                out(file + ": Size is different");
            }
            
            // TODO check file mode
            // TODO check file ownership
        }
    }
    
    /**
     * Copies an archive to another archive.
     *
     * This is used to set an output stream into position for appending new entries,
     * and to copy entries from another archive into another archive.
     *
     * FIXME does not verify that tin is actually a tar archive (Concat)
     */
    private void copy(TarInputStream tin, TarOutputStream tout) throws IOException {
        TarEntry entry;
        while ((entry = tin.getNextEntry()) != null) {
            tout.putNextEntry(entry);
            tin.copyEntryContents(tout);
            tout.closeEntry();
        }
        tin.close();
    }
    
    /**
     * Sets up a TarOutputStream suitable for appending new entries.
     */
    private TarOutputStream appendTarOutputStream() throws IOException {
        // FIXME this isnt working.
        OutputStream out;
        InputStream in;
        TarOutputStream tout;
        TarInputStream tin;
        File tmpArchive;
        
        tmpArchive = archive.getAbsoluteFile();
        tmpArchive.renameTo(new File(archive.getName() + ".tmp"));
        
        createArchive();
        
        if ((out = openFileWrite(archive, false, false)) == null) {
            fatal(" ", 1);
        }
        if (compress != 0) {
            out = wrapOutputStream(out);
        }
        tout = new TarOutputStream(out);
        
        if ((in = openFileRead(tmpArchive)) == null) {
            fatal(" ", 1);
        }
        if (decompress != 0) {
            in = wrapInputStream(in);
        }
        tin = new TarInputStream(in);
        copy(tin, tout);
        tmpArchive.delete();
        
        return tout;
    }
        
    /**
     * Creates a file for an archive, deleting it first if it already exists.
     */
    private void createArchive() {
        try {
            if (archive.exists()) {
                archive.delete();
            }
            if (!archive.createNewFile()) {
                throw new IOException();
            }
        } catch (IOException e) {
            fatal(err_file_create + archive, 1);
        }
    }
    
    /**
     * Processes a list of files and directories given on the command line
     * in order to generate a list of files for Append, Create and Update.
     *
     * TODO Add parsing of FileList and exclusion filtering.
     */
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
    
    // TODO Need to check that the list of files are actually archives or compressed archives.
    private File[] processArchives(File[] files) {
        return files;
    }
    
    /**
     * Wraps an InputStream with a decompression stream for reading compressed archives.
     */
    private InputStream wrapInputStream(InputStream in) throws IOException {
        if (decompress == USE_BZIP) {
            return new CBZip2InputStream(in);
        }
        if (decompress == USE_GZIP) {
            return new GZIPInputStream(in, BUFFER_SIZE);
        }
        
        fatal("Internal Error: Unknown compress type", -1);
        return null;
    }
    
    /**
     * Wraps an OutputStream with a compression stream for writing compressed archives.
     */
    private OutputStream wrapOutputStream(OutputStream out) throws IOException {
        if (compress == USE_BZIP) {
            return new CBZip2OutputStream(out);
        }
        if (compress == USE_GZIP) {
            return new GZIPOutputStream(out);
        }
        
        fatal("Internal Error: Unknown decompress type", -1);
        return null;
    }
    
    /**
     * Used by create to determine if the archive should be compressed even if the -j or -z flags
     * were not given.
     */
    private int checkSuffix(File file) {
        String name = file.getName();
        if (name.endsWith(".tbz") || name.endsWith(".tbz2") || name.endsWith(".tar.bz") || name.endsWith(".tar.bz2")) {
            return USE_BZIP;
        }
        if (name.endsWith(".tar.gz")) {
            return USE_GZIP;
        }
        return 0;
    }
    
    /**
     * Check via the file header the type of compression used to create it.
     */
    private int checkCompressed(File file) throws IOException {
        if (bzip) {
            return checkBZipMagic(file) ? USE_BZIP : -1;
        }
        if (gzip) {
            return checkGZipMagic(file) ? USE_GZIP : -1;
        }
        /*
        if (checkBZipMagic(file)) {
            return USE_BZIP;
        }
        if (checkGZipMagic(file)) {
            return USE_GZIP;
        }
        */
        return 0;
    }
    
    // TODO
    private boolean checkBZipMagic(File file) throws IOException {
        return true;
    }
    
    // TODO
    private boolean checkGZipMagic(File file) throws IOException {
        return true;
    }
    
    /**
     * Checks which operational mode was selected.
     *
     * If no mode was selected, or more than one mode was selected, the return
     * value will be false, otherwise the return value is true, and this.mode will
     * be set with the selected mode.
     */
    private boolean checkMode() {
        int check = 0;
        if (DoAppend.isSet())  {
            mode = TAR_APPEND;
            check++;
        }
        if (DoCreate.isSet())  {
            mode = TAR_CREATE;
            check++;
        }
        if (DoConcat.isSet())  {
            mode = TAR_CONCAT;
            check++;
        }
        if (DoDelete.isSet())  {
            mode = TAR_DELETE;
            check++;
        }
        if (DoDiff.isSet())    {
            mode = TAR_DIFF;
            check++;
        }
        if (DoExtract.isSet()) {
            mode = TAR_EXTRACT;
            check++;
        }
        if (DoList.isSet())    {
            mode = TAR_LIST;
            check++;
        }
        if (DoUpdate.isSet())  {
            mode = TAR_UPDATE;
            check++;
        }
        return check == 1;
    }
}
