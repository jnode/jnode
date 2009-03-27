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

package org.jnode.shell.command;

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * Outputs compressed files to standard out.
 */

public class ZCATCommand extends AbstractCommand {

    private static final String msg_file    = "the files to compress, use stdin if FILE is '-' or no files are listed";
    
    private final FileArgument ArgFile     = new FileArgument("file", Argument.OPTIONAL | Argument.MULTIPLE, msg_file);
    
    public ZCATCommand() {
        super("decompresses files to standard output");
        registerArguments(ArgFile);
    }
    
    public void execute() {
        GZIP gzip = new GZIP( ArgFile.getValues() , null , getInput() , getOutput() , getError() );
        try {
            gzip.decompress(false,true,false);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
