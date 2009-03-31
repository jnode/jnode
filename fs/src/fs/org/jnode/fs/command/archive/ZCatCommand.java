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

/**
 * Outputs compressed files to standard out.
 */

public class ZCatCommand extends GZip {

    private static final String msg_file    = "the files to compress, use stdin if FILE is '-' or no files are listed";
    
    private final FileArgument ArgFile = new FileArgument("file", Argument.OPTIONAL | Argument.MULTIPLE, msg_file);
    private final FlagArgument ArgDebug = new FlagArgument("debug", Argument.OPTIONAL, " ");
    private final FlagArgument ArgVerbose = new FlagArgument("verbose", Argument.OPTIONAL, " ");
    private final FlagArgument ArgQuiet = new FlagArgument("quiet", Argument.OPTIONAL, " ");
    
    public ZCatCommand() {
        super("decompresses files to standard output");
        registerArguments(ArgFile, ArgDebug, ArgVerbose, ArgQuiet);
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
        
        mode = GZIP_DECOMPRESS;
        
        try {
            execute(ArgFile.getValues(), false, true, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
