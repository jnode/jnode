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

import org.jnode.shell.AbstractCommand;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.FileArgument;
import org.jnode.shell.syntax.FlagArgument;
import org.jnode.shell.syntax.StringArgument;

/**
 * @author chris boertien
 */
public class ZipCommand extends Zip {
/*
    private static final String help_refresh = "freshen: only changed files";
    private static final String help_delete = "delete entries in zip file";
    private static final String help_move = "move into zipfile (delete files)";
    private static final String help_recurse = "recurse into directories";
    private static final String help_zfile = "zip file to oeprate on";
    private static final String help_patterns = "search patterns";
    
    private final FileArgument ArgZipfile = new FileArgument("zipFile" , Argument.OPTIONAL , help_zfile);
    private final StringArgument ArgPatterns 
        = new StringArgument("patterns" , Argument.OPTIONAL | Argument.MULTIPLE , help_patterns);
    private final FlagArgument ArgDelete = new FlagArgument("doDelete" , Argument.OPTIONAL , help_delete);
    private final FlagArgument ArgRefresh = new FlagArgument("doRefresh" , Argument.OPTIONAL , help_refresh);
    private final FlagArgument ArgMove = new FlagArgument("doMove" , Argument.OPTIONAL , help_move);
    private final FlagArgument ArgRecurse = new FlagArgument("recurse" , Argument.OPTIONAL , help_recurse);
    */
    public ZipCommand() {
        super("compress files into a zip archive");
        //registerArguments(ArgZipfile, ArgPatterns, ArgDelete, ArgRefresh, ArgMove, ArgRecurse);
    }
    
    public void execute() {
        setup();
    }
}
