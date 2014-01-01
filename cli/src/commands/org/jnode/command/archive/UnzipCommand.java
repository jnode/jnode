/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
public class UnzipCommand extends Zip {

    private static final String help_ignore_case = "Use case-insensitive matching for include/exclude";
    private static final String help_backup = "Backup existing files when extracting, appending a ~ to the name";
    private static final String help_keep = "Skip extracting entries if the file exists";
    private static final String help_overwrite = "Overwrite existing files if they exist";
    
    private final FlagArgument IgnoreCase;
    private final FlagArgument Backup;
    private final FlagArgument Keep;
    private final FlagArgument Overwrite;
    
    public UnzipCommand() {
        super("extracts entries from zip archives");
        // from ArchiveCommand
        registerArguments(Verbose, Quiet, Debug, Stdout);
        // from Zip
        registerArguments(Archive, Patterns, NoPath, Freshen, List, Test, Update);
        
        IgnoreCase = new FlagArgument("ignore-case", Argument.OPTIONAL, help_ignore_case);
        Backup     = new FlagArgument("backup", Argument.OPTIONAL, help_backup);
        Keep       = new FlagArgument("keep", Argument.OPTIONAL, help_keep);
        Overwrite  = new FlagArgument("overwrite", Argument.OPTIONAL, help_overwrite);
        registerArguments(IgnoreCase, Backup, Keep, Overwrite);
    }
    
    @Override
    public void execute() {
        ignore_case = IgnoreCase.isSet();
        backup      = Backup.isSet();
        keep        = Keep.isSet();
        overwrite   = Overwrite.isSet();
        
        super.execute("unzip");
    }
}
