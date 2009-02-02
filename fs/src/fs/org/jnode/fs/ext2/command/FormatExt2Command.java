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
 
package org.jnode.fs.ext2.command;

import java.util.HashMap;
import java.util.Map;
import org.jnode.fs.command.AbstractFormatCommand;
import org.jnode.fs.ext2.BlockSize;
import org.jnode.fs.ext2.Ext2FileSystem;
import org.jnode.fs.ext2.Ext2FileSystemFormatter;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.MappedArgument;

/**
 * @author gbin
 * @author crawley@jnode.org
 */
public class FormatExt2Command extends AbstractFormatCommand<Ext2FileSystem> {

    private static class BlockSizeArgument extends MappedArgument<BlockSize> {
        private static final Map<String, BlockSize> MAP = new HashMap<String, BlockSize>();

        static {
            MAP.put("1k", BlockSize._1Kb);
            MAP.put("2k", BlockSize._2Kb);
            MAP.put("4k", BlockSize._4Kb);
        }

        public BlockSizeArgument() {
            super("blockSize", Argument.OPTIONAL, new BlockSize[0],
                MAP, true, "block size for EXT2 filesystem (default 4k)");
        }

        @Override
        protected String argumentKind() {
            return "block size";
        }
    }

    private final BlockSizeArgument ARG_BLOCK_SIZE = new BlockSizeArgument();

    public FormatExt2Command() {
        super("Format a block device with ext2 filesystem");
        registerArguments(ARG_BLOCK_SIZE);
    }

    public static void main(String[] args) throws Exception {
        new FormatExt2Command().execute(args);
    }

    @Override
    protected Ext2FileSystemFormatter getFormatter() {
        if (ARG_BLOCK_SIZE.isSet()) {
            return new Ext2FileSystemFormatter(ARG_BLOCK_SIZE.getValue());
        } else {
            return new Ext2FileSystemFormatter(BlockSize._4Kb);
        }
    }
}
