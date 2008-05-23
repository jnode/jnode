/*
 * $Id: FormatCommand.java 3585 2007-11-13 13:31:18Z galatnm $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.fs.fat.command;

import java.util.HashMap;
import java.util.Map;

import org.jnode.fs.command.AbstractFormatCommand;
import org.jnode.fs.fat.FatFileSystem;
import org.jnode.fs.fat.FatFileSystemFormatter;
import org.jnode.fs.fat.FatType;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.MappedArgument;

/**
 * This command formats a FAT12 or FAT16 filesystem.
 * 
 * @author gbin
 * @author crawley@jnode.org
 */
public class FormatFatCommand extends AbstractFormatCommand<FatFileSystem> {
    
    private static class FatTypeArgument extends MappedArgument<FatType> {
        private static final Map<String, FatType> MAP = new HashMap<String, FatType>();
        static {
            MAP.put("fat12", FatType.FAT12);
            MAP.put("fat16", FatType.FAT16);
        }
        
        public FatTypeArgument() {
            super("fsType", Argument.MANDATORY, new FatType[0], MAP, 
                    true, "the kind of FAT filesystem to create");
        }
        @Override
        protected String argumentKind() {
            return "fat type";
        }
    }
    
    private final FatTypeArgument ARG_FS_TYPE = new FatTypeArgument();

    public FormatFatCommand() {
        super("Format a block device with a FAT12 or FAT16 filesystem");
        registerArguments(ARG_FS_TYPE);
    }

    public static void main(String[] args) throws Exception {
    	new FormatFatCommand().execute(args);
    }

	@Override
	protected FatFileSystemFormatter getFormatter() {
        return new FatFileSystemFormatter(ARG_FS_TYPE.getValue());
	}
}
