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

/**
 * Decompresses data in the gzip format.
 *
 * If a list of files is given, the files will be decompressed. The original files will be deleted
 * and replaced with decompressed versions unless told to write to standard output, then the original
 * files are left as-is.
 *
 * If no files are given, then standard input is decompressed to standard output.
 *
 * @author Chris Boertien
 */
 
public class GUnzipCommand extends GZip {
    
    public GUnzipCommand() {
        super("decompresses files/data");
        // from ArchiveCommand
        registerArguments(Quiet, Verbose, Debug, Force, Stdout);
        // from GZip
        registerArguments(Files, List, NoName, Name, Recurse, Test, Suffix);
    }
    
    public void execute() {
        compress = false;
        super.execute("gunzip");
    }
}
