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

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

/**
 * @author chris boertien
 */
public class BZipCommand extends BZip {

    private static final String help_fast = "compress faster";
    private static final String help_best = "compress better";
    
    private final FlagArgument C1 = new FlagArgument("c1", Argument.OPTIONAL, help_fast);
    private final FlagArgument C2 = new FlagArgument("c2", Argument.OPTIONAL, " ");
    private final FlagArgument C3 = new FlagArgument("c3", Argument.OPTIONAL, " ");
    private final FlagArgument C4 = new FlagArgument("c4", Argument.OPTIONAL, " ");
    private final FlagArgument C5 = new FlagArgument("c5", Argument.OPTIONAL, " ");
    private final FlagArgument C6 = new FlagArgument("c6", Argument.OPTIONAL, " ");
    private final FlagArgument C7 = new FlagArgument("c7", Argument.OPTIONAL, " ");
    private final FlagArgument C8 = new FlagArgument("c8", Argument.OPTIONAL, " ");
    private final FlagArgument C9 = new FlagArgument("c9", Argument.OPTIONAL, help_best);

    public BZipCommand() {
        super("compresses data with bzip2");
        // from ArchiveCommand
        registerArguments(Quiet, Verbose, Stdout, Force, Debug);
        // from BZip
        registerArguments(Compress, Decompress, Files, Keep, Small, Test);
        registerArguments(C1, C2, C3, C4, C5, C6, C7, C8, C9);
    }
    
    public void execute() {
        compress = true;
        if (C1.isSet()) clevel = 1;
        if (C2.isSet()) clevel = 2;
        if (C3.isSet()) clevel = 3;
        if (C4.isSet()) clevel = 4;
        if (C5.isSet()) clevel = 5;
        if (C6.isSet()) clevel = 6;
        if (C7.isSet()) clevel = 7;
        if (C8.isSet()) clevel = 8;
        if (C9.isSet()) clevel = 9;
        else clevel = 6;
        
        super.execute("bzip2");
    }
}
