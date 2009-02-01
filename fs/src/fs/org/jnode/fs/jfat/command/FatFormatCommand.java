/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.fs.jfat.command;

import java.util.HashMap;
import java.util.Map;

import org.jnode.fs.command.AbstractFormatCommand;
import org.jnode.fs.jfat.ClusterSize;
import org.jnode.fs.jfat.FatFileSystem;
import org.jnode.fs.jfat.FatFileSystemFormatter;
import org.jnode.shell.syntax.Argument;
import org.jnode.shell.syntax.MappedArgument;

/**
 * This command formats a FAT32 file system.
 * 
 * @author Tango
 * @author crawley@jnode.org
 */
public class FatFormatCommand extends AbstractFormatCommand<FatFileSystem> {
    
    private static class ClusterSizeArgument extends MappedArgument<ClusterSize> {
        private static final Map<String, ClusterSize> MAP = new HashMap<String, ClusterSize>();
        static {
            MAP.put("1k", ClusterSize._1Kb);
            MAP.put("2k", ClusterSize._2Kb);
            MAP.put("4k", ClusterSize._4Kb);
            MAP.put("8k", ClusterSize._8Kb);
            MAP.put("16k", ClusterSize._16Kb);
            MAP.put("32k", ClusterSize._32Kb);
            // FIXME - should we enable this?
            // MAP.put("64k", ClusterSize._64Kb);
        }
        public ClusterSizeArgument() {
            super("clusterSize", Argument.OPTIONAL, new ClusterSize[0],
                    MAP, true, "cluster size for FAT32 filesystem (default 4k)");
        }

        @Override
        protected String argumentKind() {
            return "cluster size";
        }
    }

    private final ClusterSizeArgument ARG_CLUSTER_SIZE = new ClusterSizeArgument();
    
    public FatFormatCommand() {
        super("Format a FAT32 file system");
        registerArguments(ARG_CLUSTER_SIZE);
    }

    public static void main(String[] args) throws Exception {
        new FatFormatCommand().execute(args);
    }
    
    protected FatFileSystemFormatter getFormatter() {
        ClusterSize clusterSize = ARG_CLUSTER_SIZE.isSet() ? 
                ARG_CLUSTER_SIZE.getValue() : ClusterSize._4Kb;
        return new FatFileSystemFormatter(clusterSize);
    }
}



