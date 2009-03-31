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
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * Compresses or decompresses data in the gzip format.
 *
 * If a list of files is given, the files will be (de)compressed. The original files will be deleted
 * and replaced with (de)compressed versions unless told to write to standard output, then the original
 * files are left as-is.
 *
 * If no files are given, then standard input is (de)compressed to standard output.
 *
 * @author Chris Boertien
 */
public class GZipCommand extends GZip {

    private static final String msg_stdout  = "Write output on standard output, keep original files";
    private static final String msg_decomp  = "decompress";
    private static final String msg_force   = "force overwrite of output files and compress links";
    private static final String msg_list    = "list compressed file contents";
    private static final String msg_noname  = "do not save or restore the original name and time stamp";
    private static final String msg_name    = "save or restore the original name and time stamp";
    private static final String msg_quiet   = "suppress all warning";
    private static final String msg_recurse = "operate recursively on directories";
    private static final String msg_suffix  = "use suffix SUF on compressed files";
    private static final String msg_test    = "test compressed file integrity";
    private static final String msg_verbose = "verbose mode";
    private static final String msg_fast    = "compress faster";
    private static final String msg_best    = "compress better";
    private static final String msg_file    = "the files to compress, use stdin if FILE is '-' or no files are listed";
    
    private final FileArgument ArgFile     = new FileArgument("file", Argument.OPTIONAL | Argument.MULTIPLE, msg_file);
    private final StringArgument ArgSuffix = new StringArgument("suffix", Argument.OPTIONAL, msg_suffix);
    private final FlagArgument ArgStdout   = new FlagArgument("stdout", Argument.OPTIONAL, msg_stdout);
    private final FlagArgument ArgDecomp   = new FlagArgument("decompress", Argument.OPTIONAL, msg_decomp);
    private final FlagArgument ArgForce    = new FlagArgument("force", Argument.OPTIONAL, msg_force);
    private final FlagArgument ArgList     = new FlagArgument("list", Argument.OPTIONAL, msg_list);
    private final FlagArgument ArgNoname   = new FlagArgument("noname", Argument.OPTIONAL, msg_noname);
    private final FlagArgument ArgName     = new FlagArgument("name", Argument.OPTIONAL, msg_name);
    private final FlagArgument ArgQuiet    = new FlagArgument("quiet", Argument.OPTIONAL, msg_quiet);
    private final FlagArgument ArgRecurse  = new FlagArgument("recursive", Argument.OPTIONAL, msg_recurse);
    private final FlagArgument ArgTest     = new FlagArgument("test", Argument.OPTIONAL, msg_test);
    private final FlagArgument ArgVerbose  = new FlagArgument("verbose", Argument.OPTIONAL, msg_verbose);
    private final FlagArgument ArgLvl1     = new FlagArgument("lvl1", Argument.OPTIONAL, msg_fast);
    private final FlagArgument ArgLvl9     = new FlagArgument("lvl9", Argument.OPTIONAL, msg_best);
    private final FlagArgument ArgDebug    = new FlagArgument("debug", Argument.OPTIONAL, " ");
    
    public GZipCommand() {
        super("compresses and decompresses files/data");
        registerArguments(ArgFile, ArgSuffix, ArgDecomp, ArgNoname, ArgName, ArgStdout, ArgForce,
                          ArgQuiet, ArgVerbose, ArgLvl1, ArgLvl9, ArgRecurse, ArgTest, ArgList, ArgDebug);
    }
    
    public void execute() {
        if (ArgQuiet.isSet()) {
            outMode = 0;
        } else {
            if (ArgDebug.isSet()) {
                outMode |= OUT_DEBUG;
            }
            if (ArgVerbose.isSet()) {
                outMode |= OUT_NOTICE;
            }
        }
        
        if (ArgSuffix.isSet()) suffix = ArgSuffix.getValue();
        
        if (ArgList.isSet())        mode = GZIP_LIST;
        else if (ArgTest.isSet())   mode = GZIP_TEST;
        else if (ArgDecomp.isSet()) mode = GZIP_DECOMPRESS;
        else                        mode = GZIP_COMPRESS;
        
        try {
            execute(ArgFile.getValues(), ArgForce.isSet(), ArgStdout.isSet(), ArgRecurse.isSet());
        } catch (Exception e) {
            e.printStackTrace();
            exit(1);
        }
    }
    
    
}
