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
    
    private final FlagArgument FilesStdin;
    private final FlagArgument NoDirEntry;
    private final FlagArgument Recurse;
    private final FileArgument TmpDir;
    private final StringArgument NoCompress;
    private final StringArgument NewerThan;
    private final StringArgument OlderThan;
    
    public ZipCommand() {
        super("compress files into a zip archive");
        // from ArchiveCommand
        registerArguments(Verbose, Quiet, Debug);
        // from Zip
        registerArguments(Archive, Patterns, NoPath, Delete, Freshen, Move, Update);
        
        FilesStdin   = new FlagArgument("files-stdin", Argument.OPTIONAL, help_files_stdin);
        NoDirEntry   = new FlagArgument("no-dirs", Argument.OPTIONAL, help_no_dir);
        Recurse      = new FlagArgument("recurse", Argument.OPTIONAL, help_recurse);
        TmpDir       = new FileArgument("tmp-dir", Argument.OPTIONAL, help_tmpdir);
        NoCompress   = new StringArgument("no-compress", Argument.OPTIONAL, help_no_compress);
        NewerThan    = new StringArgument("newer", Argument.OPTIONAL, help_newer_than);
        OlderThan    = new StringArgument("older", Argument.OPTIONAL, help_older_than);
        registerArguments(FilesStdin, TmpDir, NoDirEntry, NoCompress, Recurse, NewerThan, OlderThan);
    }
    
    @Override
    public void execute() {
        recurse = Recurse.isSet();
        noDirEntry = NoDirEntry.isSet();
        filesStdin = FilesStdin.isSet();
        if (NoCompress.isSet()) noCompress = NoCompress.getValue();
        if (TmpDir.isSet()) tmpDir = TmpDir.getValue();
        
        super.execute("zip");
    }
}
