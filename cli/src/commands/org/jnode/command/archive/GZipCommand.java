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
 
package org.jnode.command.archive;

import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FlagArgument;

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

    private static final String help_fast    = "compress faster";
    private static final String help_best    = "compress better";
    
    private final FlagArgument Decompress = new FlagArgument("decompress", Argument.OPTIONAL, help_decompress);
    private final FlagArgument C1         = new FlagArgument("c1", Argument.OPTIONAL, help_fast);
    private final FlagArgument C9         = new FlagArgument("c9", Argument.OPTIONAL, help_best);
    
    public GZipCommand() {
        super("compresses and decompresses files/data");
        // from ArchiveCommand
        registerArguments(Quiet, Verbose, Debug, Force, Stdout);
        // from GZip
        registerArguments(Files, List, NoName, Name, Recurse, Test, Suffix);
        registerArguments(Decompress, C1, C9);
    }
    
    public void execute() {
        compress = !Decompress.isSet();
        super.execute("gzip");
    }
}
