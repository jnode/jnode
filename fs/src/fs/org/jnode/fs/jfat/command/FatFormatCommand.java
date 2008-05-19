/*
 * $Id: FatFormatCommand.java  2007-07-27 +0100 (s,27 JULY 2007) Tanmoy Deb $
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
package org.jnode.fs.jfat.command;

import org.apache.log4j.Logger;
import org.jnode.fs.command.AbstractFormatCommand;
import org.jnode.fs.ext2.BlockSize;
import org.jnode.fs.jfat.ClusterSize;
import org.jnode.fs.jfat.FatFileSystem;
import org.jnode.fs.jfat.FatFileSystemFormatter;
import org.jnode.shell.CommandLine;
import org.jnode.shell.help.Help;
import org.jnode.shell.help.Parameter;
import org.jnode.shell.help.ParsedArguments;
import org.jnode.shell.help.Syntax;
import org.jnode.shell.help.SyntaxErrorException;
import org.jnode.shell.help.argument.EnumOptionArgument;
import org.jnode.shell.help.argument.OptionArgument;

/**
 * @author Tango
 *         <p>
 *         The FAT32 formating command.
 * 
 */
public class FatFormatCommand extends AbstractFormatCommand<FatFileSystem> {
    private static final Logger log = Logger.getLogger(FatFormatCommand.class);

    static final OptionArgument TYPE =
            new OptionArgument("action", "Type parameter",
                    new OptionArgument.Option[] { new OptionArgument.Option(
                            "-c", "Specify Sector Per Cluster Value") });
    static final Parameter PARAM_TYPE = new Parameter(TYPE, Parameter.OPTIONAL);

    static final EnumOptionArgument<ClusterSize> BS_VAL =
            new EnumOptionArgument<ClusterSize>("clusterSize",
                    "cluster size for fat filesystem",
                    new EnumOptionArgument.EnumOption<ClusterSize>("1", "1Kb",
                            ClusterSize._1Kb),
                    new EnumOptionArgument.EnumOption<ClusterSize>("2", "2Kb",
                            ClusterSize._2Kb),
                    new EnumOptionArgument.EnumOption<ClusterSize>("4", "4Kb",
                            ClusterSize._4Kb),
                    new EnumOptionArgument.EnumOption<ClusterSize>("8", "8Kb",
                            ClusterSize._8Kb),
                    new EnumOptionArgument.EnumOption<ClusterSize>("16",
                            "16Kb", ClusterSize._16Kb),
                    new EnumOptionArgument.EnumOption<ClusterSize>("32",
                            "32Kb", ClusterSize._32Kb),
                    new EnumOptionArgument.EnumOption<ClusterSize>("32",
                            "64Kb", ClusterSize._64Kb));

    static final Parameter PARAM_BS_VAL =
            new Parameter(BS_VAL, Parameter.OPTIONAL);

    public static Help.Info HELP_INFO =
            new Help.Info(
                    "mkjfat",
                    new Syntax[] { new Syntax(
                            "Format a block device with a specified type.Enter the Cluster Size as 1 for 1KB. ",
                            new Parameter[] { PARAM_TYPE, PARAM_BS_VAL,
                                    PARAM_DEVICE }) });

    public static void main(String[] args) throws Exception {
        new FatFormatCommand().execute(args);
    }

    protected ParsedArguments parse(CommandLine commandLine)
            throws SyntaxErrorException {
        return HELP_INFO.parse(commandLine);
    }

    protected FatFileSystemFormatter getFormatter(ParsedArguments cmdLine) {
        ClusterSize bsize = BS_VAL.getEnum(cmdLine, ClusterSize.class);
        return new FatFileSystemFormatter(bsize);
    }
}



