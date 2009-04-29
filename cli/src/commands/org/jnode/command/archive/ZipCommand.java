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

package org.jnode.command.archive;

//import java.text.DateFormat;
//import java.text.ParseException;
import java.util.ArrayList;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * @author chris boertien
 */
public class ZipCommand extends Zip {

    private static final String help_files_stdin = "Read files from stdin";
    private static final String help_tmpdir      = "Use this directory for storing the tmp archive";
    private static final String help_no_dir      = "Do not add entries for directories";
    private static final String help_no_compress = "Comma delimited list of suffixes that should be stored";
    private static final String help_recurse     = "recurse into directories";
    private static final String help_newer_than  = "only include files newer than the specified time";
    private static final String help_older_than  = "only include files older than the specified time";
    private static final String help_exclude     = "do not includes files matching a pattern";
    private static final String help_include     = "only includes files matching a pattern";
    
    private static final String fatal_bad_newer = "Invalid newer-than date: ";
    private static final String fatal_bad_older = "Invalid older-than date: ";
    
    private final FlagArgument FilesStdin;
    private final FlagArgument NoDirEntry;
    private final FlagArgument Recurse;
    private final FileArgument TmpDir;
    private final StringArgument NoCompress;
    private final StringArgument NewerThan;
    private final StringArgument OlderThan;
    private final StringArgument Exclude;
    private final StringArgument Include;
    
    public ZipCommand() {
        super("compress files into a zip archive");
        // from ArchiveCommand
        registerArguments(Verbose, Quiet, Debug);
        // from Zip
        registerArguments(Archive, Patterns, NoPath, Delete, Freshen, Move, Update);
        
        FilesStdin   = new FlagArgument("files-stdin", Argument.OPTIONAL, help_files_stdin);
        NoDirEntry   = new FlagArgument("no-dirs", Argument.OPTIONAL, help_no_dir);
        Recurse      = new FlagArgument("recurse", Argument.OPTIONAL, help_recurse);
        TmpDir       = new FileArgument("tmp-dir", Argument.OPTIONAL | Argument.EXISTING, help_tmpdir);
        NoCompress   = new StringArgument("no-compress", Argument.OPTIONAL, help_no_compress);
        NewerThan    = new StringArgument("newer", Argument.OPTIONAL, help_newer_than);
        OlderThan    = new StringArgument("older", Argument.OPTIONAL, help_older_than);
        Exclude      = new StringArgument("exclude", Argument.OPTIONAL | Argument.MULTIPLE, help_exclude);
        Include      = new StringArgument("include", Argument.OPTIONAL | Argument.MULTIPLE, help_include);
        registerArguments(FilesStdin, TmpDir, NoDirEntry, NoCompress, Recurse, NewerThan, OlderThan, Exclude, Include);
    }
    
    @Override
    public void execute() {
        recurse = Recurse.isSet();
        noDirEntry = NoDirEntry.isSet();
        filesStdin = FilesStdin.isSet();
        if (TmpDir.isSet()) tmpDir = TmpDir.getValue();
        /* FIXME
        if (NewerThan.isSet()) {
            try {
                newer = DateFormat.getInstance().parse(NewerThan.getValue()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                exit(1);
            }
        }
        if (OlderThan.isSet()) {
            try {
                older = DateFormat.getInstance().parse(OlderThan.getValue()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                exit(1);
            }
        }
        */
        if (NoCompress.isSet()) {
            noCompress = NoCompress.getValue().split(":");
        }
        if (Exclude.isSet()) {
            excludes = new ArrayList<String>(Exclude.getValues().length);
            for (String pattern : Exclude.getValues()) {
                excludes.add(pattern);
            }
        }
        if (Include.isSet()) {
            includes = new ArrayList<String>(Include.getValues().length);
            for (String pattern : Include.getValues()) {
                includes.add(pattern);
            }
        }
        super.execute("zip");
    }
}
